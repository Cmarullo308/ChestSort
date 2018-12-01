package me.Chestsort.main;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.UUID;

import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.bukkit.entity.Player;

import net.md_5.bungee.api.ChatColor;

public class Network {
	ChestSort plugin;

	UUID owner;
	String networkName;

	ArrayList<SortChest> sortChests = new ArrayList<SortChest>();
	HashMap<Block, NetworkItem> depositChests = new HashMap<Block, NetworkItem>();
	ArrayList<UUID> members = new ArrayList<UUID>();

	public Network(UUID owner, String networkName, ChestSort plugin) {
		this.owner = owner;
		this.networkName = networkName;
		this.plugin = plugin;
	}

	public String getMembersString() {
		String ids = "";
		for (UUID id : members) {
			ids += id.toString() + ", ";
		}

		return ids.substring(0, ids.length() - 1);
	}

	public ArrayList<SortChest> getSortChestsOfGroup(String groupName) {
		ArrayList<SortChest> chests = new ArrayList<SortChest>();
		for (SortChest chest : sortChests) {
			if (chest.group.equals(groupName)) {
				chests.add(chest);
			}
		}

		return chests;
	}

	public static void sortChestsByPriority(ArrayList<SortChest> list, int lowerIndex, int higherIndex) {
		if (list.size() < 2) {
			return;
		}

		int i = lowerIndex;
		int j = higherIndex;
		SortChest pivot = list.get(lowerIndex + (higherIndex - lowerIndex) / 2);
		while (i <= j) {
			while (list.get(i).priority < pivot.priority) {
				i++;
			}
			while (list.get(j).priority > pivot.priority) {
				j--;
			}
			if (i <= j) {
				SortChest temp = list.get(i);
				list.set(i, list.get(j));
				list.set(j, temp);
				i++;
				j--;
			}
		}
		if (lowerIndex < j)
			sortChestsByPriority(list, lowerIndex, j);
		if (i < higherIndex)
			sortChestsByPriority(list, i, higherIndex);
	}

	public boolean isOwner(Player player) {
		return owner.equals(player.getUniqueId());
	}

	public boolean isMember(Player player) {
		return isMember(player.getUniqueId());
	}

	public boolean isMember(UUID playerID) {
		for (UUID id : members) {
			if (playerID.equals(id)) {
				return true;
			}
		}

		return false;
	}

	public void disableAllDepositChests() {
		Sign sign;
		for (NetworkItem chest : depositChests.values()) {
			sign = (Sign) chest.sign.getState();
			plugin.debugMessage(sign.getLine(0));
			sign.setLine(3, sign.getLine(3) + ChatColor.RED + "(DISABLED)");
			sign.update();
		}

		depositChests = new HashMap<Block, NetworkItem>();
	}

	public void disableAllSortChests() {
		Sign sign;
		for (SortChest chest : sortChests) {
			sign = (Sign) chest.sign.getState();
			sign.setLine(3, sign.getLine(3) + ChatColor.RED + "(DISABLED)");
			sign.update();
		}

		sortChests = new ArrayList<SortChest>();
	}

	public NetworkItem getDepositChest(Block chest) {
		return depositChests.get(chest);
	}

	public boolean hasDepositChest(Block chest) {
		return depositChests.containsKey(chest);
	}

	public void addSortChest(Block chest, Block sign, String groupName, int priority) {
		sortChests.add(new SortChest(chest, sign, groupName, priority, this));
	}

	public void addDepositChest(Network network, Block chestBlock, Block signBlock) {
		depositChests.put(chestBlock, new NetworkItem(network, chestBlock, signBlock));
	}
}
