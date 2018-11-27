package me.Chestsort.main;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.UUID;

import org.bukkit.block.Block;

public class Network {
	UUID owner;
	String networkName;

	ArrayList<SortChest> sortChests = new ArrayList<SortChest>();
	HashMap<Block, NetworkItem> depositChests = new HashMap<Block, NetworkItem>();
	ArrayList<UUID> members = new ArrayList<UUID>();

	public Network(UUID owner, String networkName) {
		this.owner = owner;
		this.networkName = networkName;
	}

	public String getMembersString() {
		String ids = "";
		for (UUID id : members) {
			ids += id.toString() + ", ";
		}

		return ids.substring(0, ids.length() - 1);
	}

	public void addSortChest(Block chest, Block sign, String groupName, int priority) {
		sortChests.add(new SortChest(chest, sign, groupName, priority));
	}

	public void addDepositChest(Network network, Block chestBlock, Block signBlock) {
		depositChests.put(chestBlock, new NetworkItem(network, chestBlock, signBlock));
	}
}
