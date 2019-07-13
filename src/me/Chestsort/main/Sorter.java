package me.Chestsort.main;

import java.util.ArrayList;
import java.util.HashMap;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.Chest;
import org.bukkit.block.Sign;
import org.bukkit.block.data.Directional;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import com.sun.webkit.plugin.Plugin;

public class Sorter {
	public static void AutoSort(Block fromBlock, Inventory inventory, HumanEntity whoClicked, ChestSort plugin,
			NetworkData networkData, ChestGroupsData groupsData) {
		fromBlock = checkChest(fromBlock, plugin, networkData);

		Network network = networkData.getDepositChestNetwork(fromBlock);
		if (network == null) { // If the chest isnt a deposit chest
			return;
		}

		boolean contentsStartedEmpty = true;

		NetworkItem depositChest = network.getDepositChest(fromBlock);

		// Waits untill the deposit chest is done being in use OR if 1.5 seconds pass
		Timer timer = new Timer();
		timer.start();
		while (depositChest.inUse) {
			if (timer.getTime() > 1500) {
				depositChest.inUse = false;
				timer.stop();
				plugin.debugMessage("Deposit chest wait timed out");
			}
		}

		depositChest.inUse = true;

		ItemStack[] contents = new ItemStack[inventory.getContents().length];
		for (int i = 0; i < inventory.getContents().length; i++) {
			if (inventory.getContents()[i] != null) {
				contents[i] = inventory.getContents()[i].clone();
			}
		}

		ItemStack[] originalContents = new ItemStack[contents.length];
		for (int i = 0; i < contents.length; i++) {
			if (contents[i] != null) {
				originalContents[i] = new ItemStack(contents[i]);
			}
		}

		int[] contentAmounts = new int[contents.length];
		for (int i = 0; i < contentAmounts.length; i++) {
			if (contents[i] != null) {
				contentAmounts[i] = contents[i].getAmount();
			}
		}

		// Moves all the items in the chest to the sortchests
		for (int slotNum = 0; slotNum < contents.length; slotNum++) { // For each item in chest
			if (contents[slotNum] != null) { // If item isnt null
				contentsStartedEmpty = false;
				String group = groupsData.getGroupName(contents[slotNum].getType());
				if (groupsData.isValidGroup(group)) { // If the item is in a group
					// Get lists of sortChests with that group && misc and sorts them
					ArrayList<SortChest> sortChests = network.getSortChestsOfGroup(group);
					ArrayList<SortChest> sortChestsMisc = network.getSortChestsOfGroup("Misc");
					Network.sortChestsByPriority(sortChests, 0, sortChests.size() - 1);
					Network.sortChestsByPriority(sortChestsMisc, 0, sortChestsMisc.size() - 1);
					sortChests.addAll(sortChestsMisc);
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

		for (int i = 0; i < contents.length; i++) {
			if (contents[i] != null) {
				int amount = (contentAmounts[i] - contents[i].getAmount());
				contents[i].setAmount(amount);
				inventory.removeItem(contents[i]);
			} else {
				if (originalContents[i] != null) {
					inventory.removeItem(originalContents[i]);
				}
			}
		}

//		inventory.setContents(contents);

		// Checks if there wasn't enough space to move all the chest items
		boolean notEnoughSpace = false;
		for (ItemStack item : contents) {
			if (item != null) {
				notEnoughSpace = true;
			}
		}

		depositChest.inUse = false;

		// Sends sound to player
		if (whoClicked instanceof Player) {
			if (!contentsStartedEmpty) {
				if (notEnoughSpace) {
					if (plugin.notEnoughSpaceSoundEnabled) {
						((Player) whoClicked).playSound(whoClicked.getLocation(), plugin.notEnoughSpaceSound, 2f, 1f);
					}
				} else {
					if (plugin.sortSoundEnabled) {
						((Player) whoClicked).playSound(whoClicked.getLocation(), plugin.sortSound, 2f, 1f);
					}
				}
			}
		}
	}

	private static Block checkChest(Block fromBlock, ChestSort plugin, NetworkData networkData) {
		if (!(((Chest) fromBlock.getState()).getInventory().getSize() > 30)) {
			return fromBlock;
		}

		Directional dir = (Directional) fromBlock.getBlockData();
		Location loc = new Location(fromBlock.getWorld(), fromBlock.getLocation().getX(),
				fromBlock.getLocation().getY(), fromBlock.getLocation().getZ());

		if ((dir.getFacing() == BlockFace.SOUTH || dir.getFacing() == BlockFace.NORTH)
				&& checkSigns(loc, dir.getFacing(), networkData, plugin)) {
			return fromBlock.getLocation().add(1, 0, 0).getBlock();
		} else if ((dir.getFacing() == BlockFace.EAST || dir.getFacing() == BlockFace.WEST)
				&& checkSigns(loc, dir.getFacing(), networkData, plugin)) {
			return fromBlock.getLocation().add(0, 0, 1).getBlock();
		}

		return fromBlock;
	}

	private static boolean isAWallSign(Material type) {
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

	private static boolean checkSigns(Location loc, BlockFace facing, NetworkData networkData, ChestSort plugin) {
		if (!isAWallSign(loc.clone().add(0, 1, 0).getBlock().getType())) {
			return true;
		}

		if (facing == BlockFace.EAST || facing == BlockFace.WEST) {
			if (isAWallSign(loc.clone().add(0, 1, 1).getBlock().getType())) {
				Sign sign = (Sign) loc.clone().add(0, 1, 1).getBlock().getState();
				plugin.debugMessage(sign.getLine(0));
				if (sign.getLine(0).contains("*")
						&& networkData.networkExists(sign.getLine(0).substring(sign.getLine(0).indexOf("*") + 1))) {
					plugin.debugMessage("True 1");
					return true;
				}
			}
		} else {
			if (isAWallSign(loc.clone().add(1, 1, 0).getBlock().getType())) {
				Sign sign = (Sign) loc.clone().add(1, 1, 0).getBlock().getState();
				if (sign.getLine(0).contains("*")
						&& networkData.networkExists(sign.getLine(0).substring(sign.getLine(0).indexOf("*") + 1))) {
					plugin.debugMessage("True 2");
					return true;
				}
			}
		}
		return false;
	}

	private static ItemStack moveItemStacksToChests(ItemStack itemstack, ArrayList<SortChest> toChests) {
		if (toChests.size() == 0) {
			return itemstack;
		}

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
}
