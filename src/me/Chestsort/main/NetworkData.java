package me.Chestsort.main;

import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.material.Chest;

import net.md_5.bungee.api.ChatColor;

public class NetworkData {
	ChestSort plugin;
	// Files and FileConfigs
	public FileConfiguration networksFileCongif;
	public File networksFile;

	ConcurrentHashMap<String, Network> networks = new ConcurrentHashMap<String, Network>();

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

		networksFileCongif = YamlConfiguration.loadConfiguration(networksFile);
	}

	public boolean networkExists(String networkName) {
		if (networks.get(networkName) != null) {
			return true;
		}

		return false;
	}

	public FileConfiguration getNetworks() {
		return networksFileCongif;
	}

	public void saveNetwork(Network network) {
		// plugin.debugMessage("saveNetwork");
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
			getNetworks().set(path + ".Chests", new ArrayList<String>());
			// plugin.debugMessage("\n\nSort chests empty for\n\n");
		} else {
			for (SortChest sortChest : network.sortChests) {
				// Chest
				Block chestBlock = sortChest.block;
				int chestBlockX = new BigDecimal(chestBlock.getLocation().getX()).intValue();
				int chestBlockY = (int) chestBlock.getLocation().getY();
				int chestBlockZ = new BigDecimal(chestBlock.getLocation().getZ()).intValue();

				// Sign
				Block signBlock = sortChest.sign;
				int signX = new BigDecimal(signBlock.getLocation().getX()).intValue();
				int signY = (int) signBlock.getLocation().getY();
				int signZ = new BigDecimal(signBlock.getLocation().getZ()).intValue();
//				String groupName = ((Sign) signBlock.getState()).getLine(1); //if fail

				String group = sortChest.group;
				int priority = sortChest.priority;

				String chestPath = "Owners." + uuid + ".NetworkNames." + network.networkName + ".Chests."
						+ chestBlock.getWorld().getName() + "," + chestBlockX + "," + chestBlockY + "," + chestBlockZ;

				getNetworks().set(chestPath + ".Sign",
						signBlock.getWorld().getName() + "," + signX + "," + signY + "," + signZ);
				getNetworks().set(chestPath + ".SignText", group);
				getNetworks().set(chestPath + ".Priority", priority);
			}
		}

		// plugin.debugMessage(network.depositChests.size() + "DCSAVE");

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
//			plugin.debugMessage(chestPath);
			getNetworks().set(chestPath + ".Sign",
					sign.getWorld().getName() + "," + signX + "," + (int) sign.getLocation().getY() + "," + signZ);
		}

		// remove later maybe?
		saveNetworkData();
	}

	public void createNewNetwork(Player player, String newNetworkName) {
		Network newNetwork = new Network(player.getUniqueId(), newNetworkName);

		networks.put(newNetworkName, newNetwork);
		getNetworks().saveToString();
		saveNetworkData();
	}

	public void saveNetworkData() {
		try {
			networksFileCongif.save(networksFile);
		} catch (IOException e) {
			plugin.getServer().getLogger().info(ChatColor.RED + "Could not save networks.yml file");
		}
	}

	public void reloadNetworks() {
		networksFileCongif = YamlConfiguration.loadConfiguration(networksFile);
	}

	public void loadNetworkData() {
		setup();

		// From file to code
		Set<String> uuidStrings;
		try {
			uuidStrings = getNetworks().getConfigurationSection("Owners").getKeys(false);
		} catch (NullPointerException e) {
			return;
		}

		// For each owner
		for (String uuidString : uuidStrings) {
			Set<String> networkNames = getNetworks().getConfigurationSection("Owners." + uuidString + ".NetworkNames")
					.getKeys(false);
			// For each network
			for (String networkName : networkNames) {
				UUID uuid = UUID.fromString(uuidString);
				Network newNetwork = new Network(uuid, networkName);

				// --ADD MEMBERS--
				List<String> memberUuidStrings = getNetworks()
						.getStringList("Owners." + uuidString + ".NetworkNames." + networkName + ".Members");
				ArrayList<UUID> members = new ArrayList<UUID>();
				// for each member
				for (String newUiidStr : memberUuidStrings) {
					members.add(UUID.fromString(newUiidStr));
				}

				newNetwork.members = members;
				// --------------

				// ADD DEPOSIT CHESTS
				Set<String> depositChests = null;
				boolean hasDepositChests = true;

				try {
					depositChests = getNetworks()
							.getConfigurationSection(
									"Owners." + uuidString + ".NetworkNames." + networkName + ".DepositChests")
							.getKeys(false);
				} catch (NullPointerException e) {
					hasDepositChests = false;
				}

				// -for each deposit chest
				if (hasDepositChests) {
					for (String chest : depositChests) {
						String[] chestAndLoc = chest.split(",");
						Block newChestBlock = plugin.getServer().getWorld(chestAndLoc[0]).getBlockAt(
								Integer.parseInt(chestAndLoc[1]), Integer.parseInt(chestAndLoc[2]),
								Integer.parseInt(chestAndLoc[3]));
						String[] sign = getNetworks().getString("Owners." + uuidString + ".NetworkNames." + networkName
								+ ".DepositChests." + chest + ".Sign").split(",");
						Block newSignBlock = plugin.getServer().getWorld(sign[0]).getBlockAt(Integer.parseInt(sign[1]),
								Integer.parseInt(sign[2]), Integer.parseInt(sign[3]));

						newNetwork.addDepositChest(newNetwork, newChestBlock, newSignBlock);
					}
				}
				// ------------------

				// ADD SORT CHESTS
				Set<String> sortChests = null;
				boolean hasSortChests = true;

				try {
					sortChests = getNetworks().getConfigurationSection(
							"Owners." + uuidString + ".NetworkNames." + networkName + ".Chests").getKeys(false);
				} catch (NullPointerException e) {
					hasSortChests = false;
				}

				// -for each sort chest
				if (hasSortChests) {
					for (String chest : sortChests) {
						String[] chestAndLoc = chest.split(",");
						Block newChestBlock = plugin.getServer().getWorld(chestAndLoc[0]).getBlockAt(
								Integer.parseInt(chestAndLoc[1]), Integer.parseInt(chestAndLoc[2]),
								Integer.parseInt(chestAndLoc[3]));
						String[] sign = getNetworks().getString(
								"Owners." + uuidString + ".NetworkNames." + networkName + ".Chests." + chest + ".Sign")
								.split(",");

						Block newSignBlock = plugin.getServer().getWorld(sign[0]).getBlockAt(Integer.parseInt(sign[1]),
								Integer.parseInt(sign[2]), Integer.parseInt(sign[3]));
						String groupName = getNetworks().getString("Owners." + uuidString + ".NetworkNames."
								+ networkName + ".Chests." + chest + ".SignText");

						newNetwork.addSortChest(newChestBlock, newSignBlock, groupName, 2);
					}
				}

				// ---------------

				addNetwork(networkName, newNetwork);
			} // (end) For each network
		}
	}

	public void addNetwork(String networkName, Network network) {
		networks.put(networkName, network);
	}

	public void removeSortChestFromNetwork(Network network, Block chestBlock) {
		int x = new BigDecimal(chestBlock.getLocation().getX()).intValue();
		int y = (int) chestBlock.getLocation().getY();
		int z = new BigDecimal(chestBlock.getLocation().getZ()).intValue();

		String chestName = chestBlock.getWorld().getName() + "," + x + "," + y + "," + z;

		getNetworks().set("Owners." + network.owner + ".NetworkNames." + network.networkName + ".Chests." + chestName,
				null);
		saveNetwork(network);
	}

	public void removeDepositChestFromNetwork(Network network, Block chestBlock) {
		int x = new BigDecimal(chestBlock.getLocation().getX()).intValue();
		int y = (int) chestBlock.getLocation().getY();
		int z = new BigDecimal(chestBlock.getLocation().getZ()).intValue();

		String chestName = chestBlock.getWorld().getName() + "," + x + "," + y + "," + z;
		getNetworks().set(
				"Owners." + network.owner + ".NetworkNames." + network.networkName + ".DepositChests." + chestName,
				null);
		saveNetwork(network);
	}

	public void checkAndRemoveChest(Block brokenBlock) {
		Block chestBlock;
		boolean blockIsChest;
		if (brokenBlock.getType() == Material.WALL_SIGN) {
			chestBlock = brokenBlock.getLocation().add(0, -1, 0).getBlock();
			blockIsChest = false;
		} else {
			chestBlock = brokenBlock;
			blockIsChest = true;
		}

		// If broken block is a chest
		for (String netName : networks.keySet()) { // For each network
			Network network = networks.get(netName);

			for (int chestNum = 0; chestNum < network.sortChests.size(); chestNum++) { // For each sort chest in
																						// network
				if (network.sortChests.get(chestNum).block.equals(chestBlock)) {
					plugin.debugMessage("TRUE");
					network.sortChests.remove(chestNum);
					if (blockIsChest) {
						Sign sign = (Sign) chestBlock.getLocation().add(0, 1, 0).getBlock().getState();
						sign.setLine(0, "");
						sign.setLine(1, "");
						sign.update();
					}
					removeSortChestFromNetwork(network, chestBlock);
					return;
				}
			}

			if (network.depositChests.containsKey(chestBlock)) {
				plugin.debugMessage("TRUE 2");
				network.depositChests.remove(chestBlock);
				if (blockIsChest) {
					Sign sign = (Sign) chestBlock.getLocation().add(0, 1, 0).getBlock().getState();
					sign.setLine(0, "");
					sign.setLine(1, "");
					sign.setLine(2, "");
					sign.update();
				}
				removeDepositChestFromNetwork(network, chestBlock);
				return;
			}
		}
	}
}
