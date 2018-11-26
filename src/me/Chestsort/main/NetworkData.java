package me.Chestsort.main;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Map;
import java.util.Map.Entry;
import java.util.UUID;

import org.bukkit.block.Block;
import org.bukkit.block.Chest;
import org.bukkit.block.Sign;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import net.md_5.bungee.api.ChatColor;

public class NetworkData {
	ChestSort plugin;
	// Files and FileConfigs
	public FileConfiguration networks;
	public File networksFile;

	public NetworkData(ChestSort plugin) {
		this.plugin = plugin;
	}

	public void setup() {
		if (!plugin.getDataFolder().exists()) {
			plugin.getDataFolder().mkdir();
		}

		networksFile = new File(plugin.getDataFolder(), "networks.yml");

		if (!networksFile.exists()) {
			try {
				networksFile.createNewFile();
			} catch (IOException e) {
				plugin.getServer().getLogger().info(ChatColor.RED + "Could not create networks.yml file");
			}

		}

		networks = YamlConfiguration.loadConfiguration(networksFile);
	}

	public FileConfiguration getNetworks() {
		return networks;
	}

	public void saveNetwork(Network network) {
		UUID uuid = network.owner;

		String path = "Owners." + uuid + ".NetworkNames." + network.netwrokName;

		// Members
		ArrayList<String> UUIDStrings = new ArrayList<String>();
		for (UUID member : network.members) {
			UUIDStrings.add(member.toString());
		}
		if (UUIDStrings != null) {
			getNetworks().set(path + ".Members", UUIDStrings);
		}

		// SortChests
		for (SortChest chest : network.sortChests) {
			plugin.debugMessage("POOOP");
			String chestPath = path + ".Chests." + chest.block.getWorld().toString() + ","
					+ chest.block.getLocation().getX() + "," + chest.block.getLocation().getY() + ","
					+ chest.block.getLocation().getZ();
			// Sign location
			getNetworks().set(chestPath + ".Sign",
					chest.sign.getWorld().toString() + "," + chest.sign.getLocation().getX() + ","
							+ chest.sign.getLocation().getY() + "," + chest.sign.getLocation().getZ());
			Sign sign = (Sign) chest.sign.getState();
			getNetworks().set(chestPath + ".SignText", sign.getLine(1));
			getNetworks().set(chestPath + ".Priority", chest.priority);
		}


		for (Entry<Block, NetworkItem> depositChest : network.depositChests.entrySet()) {
			Block chest = depositChest.getValue().chest;
			Block sign = depositChest.getValue().sign;

			String chestPath = path + ".DepositChests." + chest.getWorld().toString() + "," + chest.getLocation().getX()
					+ "," + chest.getLocation().getY() + "," + chest.getLocation().getZ();
			getNetworks().set(chestPath + ".Sign", sign.getWorld() + "," + sign.getLocation().getX() + ","
					+ sign.getLocation().getY() + "," + sign.getLocation().getZ());
		}

		if (plugin.debug) {
			saveNetworkData();
		}
	}

	public void saveNetworkData() {
		try {
			networks.save(networksFile);
		} catch (IOException e) {
			plugin.getServer().getLogger().info(ChatColor.RED + "Could not save networks.yml file");
		}
	}

	public void reloadNetworks() {
		networks = YamlConfiguration.loadConfiguration(networksFile);
	}
}
