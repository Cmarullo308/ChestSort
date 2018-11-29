package me.Chestsort.main;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.Event.Result;
import org.bukkit.event.EventHandler;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.SignChangeEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.Listener;

import me.Chestsort.main.ChestSort;
import net.md_5.bungee.api.ChatColor;

public class ChestSortListener implements Listener {

	ChestSort plugin;
	NetworkData networkData;
	ChestGroupsData groupsData;

	public ChestSortListener(ChestSort plugin, NetworkData networkData, ChestGroupsData groupsData) {
		this.plugin = plugin;
		this.networkData = networkData;
		this.groupsData = groupsData;
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

		// If deposit chest
		if (e.getLine(1).equals("")) {
			// If network doesn't exist
			if (!networkData.networkExists(tempNetworkName)) {
				networkData.createNewNetwork(player, tempNetworkName);
				player.sendMessage("Network " + tempNetworkName + " created");
			}

			e.setLine(1, ChatColor.GRAY + "Open Chest");
			e.setLine(2, ChatColor.GRAY + "To Deposit");
			Network newNetwork = networkData.networks.get(tempNetworkName);
			newNetwork.addDepositChest(newNetwork, e.getBlock().getLocation().add(0, -1, 0).getBlock(), e.getBlock());
			plugin.networkData.saveNetwork(newNetwork);
		}
		// Sort Chest
		else {
			if (tempNetworkName.equalsIgnoreCase("Misc")) {
				tempNetworkName = "Misc";
			}

			if (!networkData.networkExists(tempNetworkName)) {
				player.sendMessage(ChatColor.RED + "No network named \"" + tempNetworkName
						+ "\" (Networks names are case sensitive)");
				return;
			}

			String groupName = e.getLine(1);
			if (!groupsData.isValidGroup(groupName)) {
				player.sendMessage(
						ChatColor.RED + "No group named \"" + groupName + "\" (Group names are case sensitive)");
				return;
			}

			Block chest = e.getBlock().getLocation().add(0, -1, 0).getBlock();
			Block sign = e.getBlock();

			networkData.networks.get(tempNetworkName).addSortChest(chest, sign, groupName, 2);
			networkData.saveNetwork(networkData.networks.get(tempNetworkName));
		}

		if (plugin.debug) {
			plugin.saveNetworksToFile();
		}
	}

	@EventHandler
	public final void onInventoryMove(InventoryClickEvent event) {
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

	@EventHandler
	public void blockBreak(BlockBreakEvent event) {
		Block brokenBlock = event.getBlock();

		switch (brokenBlock.getType()) {
		case CHEST:
		case WALL_SIGN:
			networkData.checkAndRemoveChest(brokenBlock);
			break;
		default:
			return;
		}

//		if (brokenBlock.getType() != Material.CHEST && brokenBlock.getType() != Material.WALL_SIGN) {
//			return;
//		}
//
//		networkData.checkAndRemoveChest(brokenBlock);
	}
}
