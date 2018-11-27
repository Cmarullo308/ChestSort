package me.Chestsort.main;

import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Map;
import java.util.UUID;

import org.bukkit.block.Block;
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
		plugin.debugMessage("saveNetwork");
		UUID uuid = network.owner;

		String path = "Owners." + uuid + ".NetworkNames." + network.networkName;

		// Members
		ArrayList<String> UUIDStrings = new ArrayList<String>();
		for (UUID member : network.members) {
			UUIDStrings.add(member.toString());
		}
		if (UUIDStrings != null) {
			getNetworks().set(path + ".Members", UUIDStrings);
		}

		// SortChests
		if (network.sortChests.isEmpty()) {
			getNetworks().set(path + ".Chests", new ArrayList<String>());
		} else {
			for (int i = 0; i < network.sortChests.size(); i++) {
				SortChest sortChest = network.sortChests.get(i);
				Block chest = network.sortChests.get(i).block;
				int x = new BigDecimal(chest.getLocation().getX()).intValue();
				int z = new BigDecimal(chest.getLocation().getZ()).intValue();
				Sign sign = (Sign) chest.getState();
				int signX = new BigDecimal(sign.getLocation().getX()).intValue();
				int signZ = new BigDecimal(sign.getLocation().getZ()).intValue();

				String chestPath = path + ".Chests." + chest.getWorld().toString() + "," + x + ","
						+ (int) chest.getLocation().getY() + "," + z;
				// Sign location
				getNetworks().set(chestPath + ".Sign",
						chest.getWorld().toString() + "," + signX + "," + (int) sign.getY() + "," + signZ);
				getNetworks().set(chestPath + ".SignText", sign.getLine(1));
				getNetworks().set(chestPath + ".Priority", sortChest.priority);
			}
		}

		plugin.debugMessage(network.depositChests.size() + "DCSAVE");

		// Deposit Chests
		for (Map.Entry<Block, NetworkItem> depositChest : network.depositChests.entrySet()) {
			Block chest = depositChest.getValue().chest;
			Block sign = depositChest.getValue().sign;
			int x = new BigDecimal(chest.getLocation().getX()).intValue();
			int z = new BigDecimal(chest.getLocation().getZ()).intValue();
			int signX = new BigDecimal(sign.getLocation().getX()).intValue();
			int signZ = new BigDecimal(sign.getLocation().getZ()).intValue();

			String chestPath = path + ".DepositChests." + chest.getWorld().getName() + "," + x + ","
					+ (int) chest.getLocation().getY() + "," + z;
			plugin.debugMessage(chestPath);
			getNetworks().set(chestPath + ".Sign",
					sign.getWorld().getName() + "," + signX + "," + (int) sign.getLocation().getY() + "," + signZ);
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
