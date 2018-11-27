package me.Chestsort.main;

import org.bukkit.block.Block;

public class SortChest {
	public Block block;
	public Block sign;
	public String group;
	public int priority;
	public boolean inUse;

	public SortChest(Block block, Block sign, String group, int priority) {
		this.block = block;
		this.sign = sign;
		this.priority = priority;
		this.group = group;
		inUse = false;
	}
}
