package me.Chestsort.main;

import java.util.ArrayList;
import java.util.HashMap;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.Chest;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.SignChangeEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
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
			plugin.debugMessage("Not chest");
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
			} else {
				plugin.debugMessage("Network already exists");
			}

			e.setLine(1, ChatColor.GRAY + "Open Chest");
			e.setLine(2, ChatColor.GRAY + "To Deposit");
			Network newNetwork = networkData.networks.get(tempNetworkName);
			newNetwork.addDepositChest(newNetwork, e.getBlock().getLocation().add(0, -1, 0).getBlock(), e.getBlock());
			plugin.networkData.saveNetwork(newNetwork);
		}
		// Sort Chest
		else {
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
		Block inventoryBlock = event.getInventory().getLocation().getBlock();
		Network network = networkData.getDepositChestNetwork(inventoryBlock);
		if (network == null) { // Not a deposit chest
			return;
		}
		NetworkItem depositChest = network.getDepositChest(inventoryBlock);
		depositChest.inUse = true;

		Chest chest = (Chest) inventoryBlock.getState();
		ItemStack[] contents = chest.getInventory().getContents();

		for (int slotNum = 0; slotNum < contents.length; slotNum++) { // For each item in chest
			if (contents[slotNum] != null) { // If item isnt null
				String group = groupsData.getGroupName(contents[slotNum].getType());
				if (groupsData.isValidGroup(group)) { // If the item is in a group
					// Get lists of sortChests with that group && misc and sorts them
					ArrayList<SortChest> sortChests = network.getSortChestsOfGroup(group);
					ArrayList<SortChest> sortChestMisc = network.getSortChestsOfGroup("Misc");
					Network.sortChestsByPriority(sortChests, 0, sortChests.size() - 1);
					Network.sortChestsByPriority(sortChestMisc, 0, sortChestMisc.size() - 1);
					sortChests.addAll(sortChestMisc);
					// -----------------------------------------------------------------
					contents[slotNum] = moveItemStacksToChests(contents[slotNum], sortChests);
					//

				} else { // If item is Misc
					// Get lists of sortChests with the group "Misc" and sorts them
					ArrayList<SortChest> sortChests = network.getSortChestsOfGroup("Misc");
					Network.sortChestsByPriority(sortChests, 0, sortChests.size() - 1);

					contents[slotNum] = moveItemStacksToChests(contents[slotNum], sortChests);
				}
			}
		}

		Chest depChest = (Chest) inventoryBlock.getState();
		depChest.getInventory().setContents(contents);

		plugin.debugMessage("-------");
		depositChest.inUse = false;
	}

	private ItemStack moveItemStacksToChests(ItemStack itemstack, ArrayList<SortChest> toChests) {
		int chestNum = 0;

		Chest chest = (Chest) toChests.get(chestNum).block.getState();
		HashMap<Integer, ItemStack> excessItems = chest.getInventory().addItem(itemstack);
		chestNum++;

		while (excessItems.size() != 0 && chestNum < toChests.size()) {
			chest = (Chest) toChests.get(chestNum).block.getState();
			excessItems = chest.getInventory().addItem(excessItems.get(0));
			chestNum++;
		}

		return excessItems.get(0);
	}

	@EventHandler
	public void blockBreak(BlockBreakEvent event) {
		Block brokenBlock = event.getBlock();
		if (brokenBlock.getType() != Material.CHEST && brokenBlock.getType() != Material.WALL_SIGN) {
			return;
		}

		networkData.checkAndRemoveChest(brokenBlock);
	}
}
