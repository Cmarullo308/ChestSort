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

	public void addNetwork(String networkName, Network network) {
		networks.put(networkName, network);
	}

	private boolean isAWallSign(Material type) {
		switch (type) {
		case OAK_WALL_SIGN:
		case DARK_OAK_WALL_SIGN:
		case JUNGLE_WALL_SIGN:
		case BIRCH_WALL_SIGN:
		case ACACIA_WALL_SIGN:
		case SPRUCE_WALL_SIGN:
			return true;
		default:
			return false;
		}
	}

	public int checkAndRemoveChest(Block brokenBlock, Player player) {
		Block chestBlock;
		boolean blockIsChest;
		if (isAWallSign(brokenBlock.getType())) {
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
					if (!network.isOwner(player) && !network.isMember(player) && !player.isOp()) {
						player.sendMessage(
								ChatColor.RED + "Must be the owner or a member of this network to modify its chests");
						return -1;
					}
					network.sortChests.remove(chestNum);
					if (blockIsChest) {
						try {
							Sign sign = (Sign) chestBlock.getLocation().add(0, 1, 0).getBlock().getState();
							sign.setLine(0, "");
							sign.setLine(1, "");
							sign.setLine(2, "");
							sign.setLine(3, "");
							sign.update();
						} catch (ClassCastException e) {
							plugin.debugMessage("Sign missing when block removed");
						}
					}
					removeSortChestFromNetwork(network, chestBlock);
					return 1;
				}
			}

			if (network.depositChests.containsKey(chestBlock)) {
				if (!network.isOwner(player) && !network.isMember(player) && !player.isOp()) {
					player.sendMessage(
							ChatColor.RED + "Must be the owner or a member of this network to modify its chests");
					return -1;
				}
				network.depositChests.remove(chestBlock);
				if (blockIsChest) {
					try {
						Sign sign = (Sign) chestBlock.getLocation().add(0, 1, 0).getBlock().getState();
						sign.setLine(0, "");
						sign.setLine(1, "");
						sign.setLine(2, "");
						sign.setLine(3, "");
						sign.update();
					} catch (ClassCastException e) {
						plugin.debugMessage("Sign missing when block removed");
					}
				}
				removeDepositChestFromNetwork(network, chestBlock);
				return 1;
			}
		}

		return 0;
	}

	public void createNewNetwork(Player player, String newNetworkName) {
		Network newNetwork = new Network(player.getUniqueId(), newNetworkName, plugin);

		networks.put(newNetworkName, newNetwork);
		getNetworks().saveToString();
		saveNetwork(networks.get(newNetwork.networkName), true); //1.8 Fix
		saveNetworkData();
	}

	public void disableAllChestsWithGroup(String groupName) {

		for (Network network : networks.values()) {
			ArrayList<SortChest> chestsToDelete = new ArrayList<SortChest>();
			for (SortChest chest : network.sortChests) {
				if (chest.group.equals(groupName)) {
					Sign sign = (Sign) chest.sign.getState();
					sign.setLine(3, ChatColor.RED + "(DISABLED)");
					sign.update();
					chestsToDelete.add(chest);
				}
			}
			for (SortChest chest : chestsToDelete) {
				network.sortChests.remove(chest);
			}
			saveNetwork(network, false);
		}

		saveNetworkData();
	}

	public NetworkItem getDepositChestBySign(Block signBlock, String networkName) {
		Network network = networks.get(networkName);

		if (network == null) {
			return null;
		}

		for (NetworkItem chest : network.depositChests.values()) { // For each SortChest
			if (chest.sign.getLocation().equals(signBlock.getLocation())) {
				return chest;
			}
		}

		return null;
	}

	public Network getDepositChestNetwork(Block chest) {
		for (String networkName : networks.keySet()) { // for each network
			if (networks.get(networkName).hasDepositChest(chest)) { // fix func
				return networks.get(networkName);
			}
		}

		return null;
	}

	public Network getNetwork(String networkName) {
		return networks.get(networkName);
	}

	public FileConfiguration getNetworks() {
		return networksFileCongif;
	}

	public SortChest getSortChestByChestBlock(Block chestBlock) {
		if (!isAWallSign(chestBlock.getLocation().clone().add(0, 1, 0).getBlock().getType())) {
			return null;
		}

		Sign sign = (Sign) chestBlock.getLocation().clone().add(0, 1, 0).getBlock().getState();
		String networkName = sign.getLine(0).substring(3);
		Network network = getNetwork(networkName);

		if (network == null) {
			return null;
		}

		for (SortChest chest : network.sortChests) {
			if (chest.block.equals(chestBlock)) {
				return chest;
			}
		}

		return null;
	}

	public SortChest getSortChestBySign(Block signBlock, String networkName) {
		Network network = networks.get(networkName);

		if (network == null) {
			return null;
		}

		for (SortChest chest : network.sortChests) { // For each SortChest
			if (chest.sign.getLocation().equals(signBlock.getLocation())) {
				return chest;
			}
		}

		return null;
	}

	public SortChest getSortChestBySign(Sign sign) {
		if (!sign.getLine(0).contains("*")) {
			return null;
		}

		int lastStarIndex = sign.getLine(0).lastIndexOf("*");
		if (sign.getLine(0).length() - 1 == lastStarIndex) {
			return null;
		}
		String networkName = sign.getLine(0).substring(lastStarIndex + 1);

		if (!networkExists(networkName)) {
			return null;
		}

		Network network = getNetwork(networkName);

		Block chestBlock = sign.getLocation().clone().add(0, -1, 0).getBlock();

		for (SortChest chest : network.sortChests) {
			if (chest.block.equals(chestBlock)) {
				return chest;
			}
		}

		return null;
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
				Network newNetwork = new Network(uuid, networkName, plugin);

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
						int priority = Integer.parseInt(getNetworks().getString("Owners." + uuidString
								+ ".NetworkNames." + networkName + ".Chests." + chest + ".Priority"));

						newNetwork.addSortChest(newChestBlock, newSignBlock, groupName, priority);
					}
				}

				// ---------------

				addNetwork(networkName, newNetwork);
			} // (end) For each network
		}
	}

	public boolean networkExists(String networkName) {
		if (networks.get(networkName) != null) {
			return true;
		}

		return false;
	}

//	public SortChest getSortChestByChestBlock(Block chestBlock) {
//		if (chestBlock.getLocation().getBlock().getType() == Material.WALL_SIGN) {
//			Sign sign = (Sign) chestBlock.getLocation().getBlock().getState();
//			return getSortChestBySign(sign);
//		}
//
//		return null;
//	}

	public void reloadNetworks() {
		networksFileCongif = YamlConfiguration.loadConfiguration(networksFile);
	}

	public void removeDepositChestFromNetwork(Network network, Block chestBlock) {
		int x = new BigDecimal(chestBlock.getLocation().getX()).intValue();
		int y = (int) chestBlock.getLocation().getY();
		int z = new BigDecimal(chestBlock.getLocation().getZ()).intValue();

		String chestName = chestBlock.getWorld().getName() + "," + x + "," + y + "," + z;
		getNetworks().set(
				"Owners." + network.owner + ".NetworkNames." + network.networkName + ".DepositChests." + chestName,
				null);
		saveNetwork(network, true);
	}

	public void removeNetwork(Network network) {
		getNetworks().set("Owners." + network.owner + ".NetworkNames." + network.networkName, null);
		networks.remove(network.networkName);
		saveNetworkData();
	}

	public void removeSortChestFromNetwork(Network network, Block chestBlock) {
		int x = new BigDecimal(chestBlock.getLocation().getX()).intValue();
		int y = (int) chestBlock.getLocation().getY();
		int z = new BigDecimal(chestBlock.getLocation().getZ()).intValue();

		String chestName = chestBlock.getWorld().getName() + "," + x + "," + y + "," + z;

		getNetworks().set("Owners." + network.owner + ".NetworkNames." + network.networkName + ".Chests." + chestName,
				null);
		saveNetwork(network, true);
	}

	public void saveNetwork(Network network, boolean saveToFile) {
		plugin.debugMessage("jjjj\n\n\n\n\n\n\n\n\n");
		
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

				String group = sortChest.group;
				int priority = sortChest.priority;

				plugin.debugMessage("!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!\n\n\n\n\n");
				String chestPath = "Owners." + uuid + ".NetworkNames." + network.networkName + ".Chests."
						+ chestBlock.getWorld().getName() + "," + chestBlockX + "," + chestBlockY + "," + chestBlockZ;

				getNetworks().set(chestPath + ".Sign",
						signBlock.getWorld().getName() + "," + signX + "," + signY + "," + signZ);
				getNetworks().set(chestPath + ".SignText", group);
				getNetworks().set(chestPath + ".Priority", priority);
			}
		}

		// Deposit Chests
		if (network.depositChests.isEmpty()) {
			getNetworks().set(path + ".DepositChests", new ArrayList<String>());
		} else {
			for (Map.Entry<Block, NetworkItem> depositChest : network.depositChests.entrySet()) {
				Block chest = depositChest.getValue().chest;
				Block sign = depositChest.getValue().sign;
				int x = new BigDecimal(chest.getLocation().getX()).intValue();
				int z = new BigDecimal(chest.getLocation().getZ()).intValue();
				int signX = new BigDecimal(sign.getLocation().getX()).intValue();
				int signZ = new BigDecimal(sign.getLocation().getZ()).intValue();

				String chestPath = path + ".DepositChests." + chest.getWorld().getName() + "," + x + ","
						+ (int) chest.getLocation().getY() + "," + z;
				getNetworks().set(chestPath + ".Sign",
						sign.getWorld().getName() + "," + signX + "," + (int) sign.getLocation().getY() + "," + signZ);
			}
		}

		// remove later maybe?
		if (saveToFile) {
//			if(network.members.isEmpty() && network.sortChests.isEmpty() && )

			saveNetworkData();
		}
	}

	public void saveNetworkData() {
		plugin.debugMessage((networksFileCongif == null) + "MMMMMM");

		Set<String> owners = networksFileCongif.getConfigurationSection("Owners").getKeys(false);

		for (String owner : owners) {
			if (networksFileCongif.getConfigurationSection("Owners." + owner + ".NetworkNames").getKeys(false)
					.isEmpty()) {
				networksFileCongif.set("Owners." + owner, null);
			}
		}

		try {
			networksFileCongif.save(networksFile);
		} catch (IOException e) {
			plugin.getServer().getLogger().info(ChatColor.RED + "Could not save networks.yml file");
		}
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
}
