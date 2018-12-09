package me.Chestsort.main;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.Chest;
import org.bukkit.block.Sign;
import org.bukkit.block.data.Directional;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.SignChangeEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryMoveItemEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.Listener;

import me.Chestsort.main.ChestSort;
import net.md_5.bungee.api.ChatColor;

public class ChestSortListener implements Listener {

	ChestSort plugin;
	NetworkData networkData;
	ChestGroupsData groupsData;

	long timeOfLastInventoryMoveEvent;

	public ChestSortListener(ChestSort plugin, NetworkData networkData, ChestGroupsData groupsData) {
		this.plugin = plugin;
		this.networkData = networkData;
		this.groupsData = groupsData;
		timeOfLastInventoryMoveEvent = System.currentTimeMillis();
	}

	@EventHandler
	public void SignChangeEvent(SignChangeEvent e) {
		if (!e.getLine(0).startsWith("*")) {
			return;
		}
		// If block under sign isn't a chest
		if (e.getBlock().getLocation().add(0, -1, 0).getBlock().getType() != Material.CHEST) {
			return;
		}

		Player player = e.getPlayer();
		String tempNetworkName = e.getLine(0).substring(1);

		if (activeChestNextToChest(e.getBlock())) {
			player.sendMessage(ChatColor.RED + "This chest is already part of a network");
			return;
		}

		// If deposit chest
		if (e.getLine(1).equals("") && e.getLine(0).startsWith("*") && e.getLine(0).length() > 1) {
			Network network;

			// If network doesn't exist
			if (!networkData.networkExists(tempNetworkName)) {
				networkData.createNewNetwork(player, tempNetworkName);
				player.sendMessage("Network " + tempNetworkName + " created");
				network = networkData.networks.get(tempNetworkName);
			} else {
				network = networkData.networks.get(tempNetworkName);
				if (!network.isOwner(player) && !network.isMember(player)) {
					player.sendMessage(ChatColor.RED + "You do not have permission to add to this network");
					return;
				}
			}

			e.setLine(0, ChatColor.DARK_BLUE + e.getLine(0));
			e.setLine(1, ChatColor.GRAY + "Open Chest");
			e.setLine(2, ChatColor.GRAY + "To Deposit");
			network.addDepositChest(network, e.getBlock().getLocation().add(0, -1, 0).getBlock(), e.getBlock());
			plugin.networkData.saveNetwork(network);

			player.sendMessage("Deposit chest created for network " + ChatColor.YELLOW + tempNetworkName);
		}
		// Sort Chest
		else if (e.getLine(0).startsWith("*") && e.getLine(0).length() > 1) {
			if (!networkData.networkExists(tempNetworkName)) {
				player.sendMessage(ChatColor.RED + "No network named \"" + tempNetworkName
						+ "\" (Networks names are case sensitive)");
				return;
			}

			Network network = networkData.networks.get(tempNetworkName);

			if (!network.isOwner(player) && !network.isMember(player)) {
				player.sendMessage(ChatColor.RED + "You do not have permission to add to this network");
				return;
			}

			int newChestPriority;

			if (!e.getLine(2).equals("")) {
				try {
					newChestPriority = Integer.parseInt(e.getLine(2));
				} catch (NumberFormatException ex) {
					newChestPriority = plugin.defaultChestPriority;
					player.sendMessage(ChatColor.RED + "Invalid priority number, resetting to default " + ChatColor.BLUE
							+ plugin.defaultChestPriority);
				}
			} else {
				newChestPriority = plugin.defaultChestPriority;
			}

			String groupName = e.getLine(1);
			if (groupName.equalsIgnoreCase("Misc")) {
				groupName = "Misc";
			}
			if (!groupsData.isValidGroup(groupName) && !groupName.equals("Misc")) {
				player.sendMessage(
						ChatColor.RED + "No group named \"" + groupName + "\" (Group names are case sensitive)");
				return;
			}

			e.setLine(0, ChatColor.DARK_BLUE + e.getLine(0));
			e.setLine(2, ChatColor.GRAY + "Priority: " + newChestPriority);

			Block chest = e.getBlock().getLocation().add(0, -1, 0).getBlock();
			Block sign = e.getBlock();

			network.addSortChest(chest, sign, groupName, newChestPriority);
			networkData.saveNetwork(network);

			player.sendMessage("Chest created for group " + ChatColor.YELLOW + groupName + ChatColor.WHITE
					+ " in network " + ChatColor.YELLOW + tempNetworkName);
		}

		plugin.saveNetworksToFile();
	}

	private boolean activeChestNextToChest(Block block) {
		Block chestBlock = block.getLocation().clone().add(0, -1, 0).getBlock();
		Directional dir = (Directional) chestBlock.getBlockData();

		Sign sign = null;

		if (dir.getFacing() == BlockFace.EAST || dir.getFacing() == BlockFace.WEST) {
			if (block.getLocation().clone().add(0, 0, 1).getBlock().getType() == Material.WALL_SIGN) {
				sign = (Sign) block.getLocation().clone().add(0, 0, 1).getBlock().getState();
			} else if (block.getLocation().clone().add(0, 0, -1).getBlock().getType() == Material.WALL_SIGN) {
				sign = (Sign) block.getLocation().clone().add(0, 0, -1).getBlock().getState();
			}
		} else {
			if (block.getLocation().clone().add(1, 0, 0).getBlock().getType() == Material.WALL_SIGN) {
				sign = (Sign) block.getLocation().clone().add(1, 0, 0).getBlock().getState();
			} else if (block.getLocation().clone().add(-1, 0, 0).getBlock().getType() == Material.WALL_SIGN) {
				sign = (Sign) block.getLocation().clone().add(-1, 0, 0).getBlock().getState();
			}
		}

		if (sign == null) {
			return false;
		}

		String networkName = sign.getLine(0).substring(3);

		if (networkData.networkExists(networkName)) {
			SortChest otherSortChest = networkData.getSortChestBySign(sign.getBlock(), networkName);
			if (otherSortChest == null) { // No sort chest
				NetworkItem otherDepositChest = networkData.getDepositChestBySign(sign.getBlock(), networkName);

				if (otherDepositChest == null) { // No deposit chest
					return false;
				} else { // Is deposit chest
					Chest c1 = (Chest) otherDepositChest.chest.getState();
					Chest c2 = (Chest) chestBlock.getState();
					if (c1.getInventory().getLocation().equals(c2.getInventory().getLocation())) {
						return true;
					} else {
						return false;
					}
				}
			} else { // Is sort chest
				Chest c1 = (Chest) otherSortChest.block.getState();
				Chest c2 = (Chest) chestBlock.getState();
				if (c1.getInventory().getLocation().equals(c2.getInventory().getLocation())) {
					return true;
				} else {
					return false;
				}
			}
		}

		return false;
	}

	@EventHandler
	public void onInventoryClickEvent(InventoryClickEvent event) {
		if (event.getInventory().getType() == InventoryType.CHEST) {
			Block inventoryBlock = event.getInventory().getLocation().getBlock();

			Thread autoSortThread = new Thread() {
				@Override
				public void run() {
					Sorter.AutoSort(inventoryBlock, event.getInventory(), event.getWhoClicked(), plugin, networkData,
							groupsData);
				}
			};

			autoSortThread.start();
		}
	}

//Probably won't need again
//	@EventHandler
//	public final void onInventoryMove(InventoryClickEvent event) {
//		if (event.getInventory().getType() == InventoryType.CHEST) {
//			Block inventoryBlock = event.getInventory().getLocation().getBlock();
//
//			Thread autoSortThread = new Thread() {
//				@Override
//				public void run() {
//					Sorter.AutoSort(inventoryBlock, event.getInventory(), event.getWhoClicked(), plugin, networkData,
//							groupsData);
//				}
//			};
//
//			autoSortThread.start();
//		}
//	}

	@EventHandler
	public void onInventoryMoveItemEvent(InventoryMoveItemEvent event) {
		Block blockMovedTo = event.getDestination().getLocation().getBlock();
		if (blockMovedTo.getType() != Material.CHEST) {
			return;
		}

		Chest chest = (Chest) blockMovedTo.getState();

		Thread autoSortThread = new Thread() {
			@Override
			public void run() {
				Sorter.AutoSort(blockMovedTo, chest.getInventory(), null, plugin, networkData, groupsData);
			}
		};

		autoSortThread.start();
	}

	@EventHandler
	public void blockBreak(BlockBreakEvent event) {
		Block brokenBlock = event.getBlock();

		switch (brokenBlock.getType()) {
		case CHEST:
		case WALL_SIGN:
			if (!networkData.checkAndRemoveChest(brokenBlock, event.getPlayer())) {
				event.setCancelled(true);
			}
			break;
		default:
			break;
		}
	}
}
