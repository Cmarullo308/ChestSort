package me.Chestsort.main;

import java.util.ArrayList;
import java.util.HashMap;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.Chest;
import org.bukkit.block.data.Directional;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import com.sun.javafx.scene.traversal.Direction;

public class Sorter {
	public static void AutoSort(Block fromBlock, Inventory inventory, ChestSort plugin, NetworkData networkData,
			ChestGroupsData groupsData) {

		Block originalFromBlock = fromBlock;
		fromBlock = checkChest(fromBlock, plugin);

		Network network = networkData.getDepositChestNetwork(fromBlock);
		if (network == null) { // Not a deposit chest
			return;
		}

		NetworkItem depositChest = network.getDepositChest(fromBlock);

		ItemStack[] contents = inventory.getContents();

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

//		Chest depChest = (Chest) fromBlock.getState();
		inventory.setContents(contents);

		depositChest.inUse = false;
	}

	private static Block checkChest(Block fromBlock, ChestSort plugin) {
		Directional dir = (Directional) fromBlock.getBlockData();
		Location loc = new Location(fromBlock.getWorld(), fromBlock.getLocation().getX(),
				fromBlock.getLocation().getY(), fromBlock.getLocation().getZ());

		if ((dir.getFacing() == BlockFace.SOUTH || dir.getFacing() == BlockFace.NORTH)
				&& ((Chest) fromBlock.getState()).getInventory().getSize() > 30
				&& loc.add(0, 1, 0).getBlock().getType() != Material.WALL_SIGN) {
			return fromBlock.getLocation().add(1, 0, 0).getBlock();
		} else if ((dir.getFacing() == BlockFace.EAST || dir.getFacing() == BlockFace.WEST)
				&& ((Chest) fromBlock.getState()).getInventory().getSize() > 30
				&& loc.add(0, 1, 0).getBlock().getType() != Material.WALL_SIGN) {
			return fromBlock.getLocation().add(0, 0, 1).getBlock();
		}

		return fromBlock;
	}

	private static ItemStack moveItemStacksToChests(ItemStack itemstack, ArrayList<SortChest> toChests) {
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
