package me.Chestsort.main;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.bukkit.block.Block;

public class Network {
	UUID owner;
	String netwrokName;
	String world;

	List<SortChest> sortChests = new ArrayList<SortChest>();
	Map<Block, NetworkItem> depositChests = new HashMap<Block, NetworkItem>();
	ArrayList<UUID> members = new ArrayList<UUID>();

	public Network(UUID owner, String netwrokName, String world) {
		this.owner = owner;
		this.netwrokName = netwrokName;
		this.world = world;
	}

	public void addDepositChest(Network network, Block chestBlock, Block signBlock) {
		depositChests.put(chestBlock, new NetworkItem(network, chestBlock, signBlock));
	}
}
