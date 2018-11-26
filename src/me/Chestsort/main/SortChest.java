package me.Chestsort.main;

import org.bukkit.block.Block;

public class SortChest {
	public Block block;
	public Block sign;
	public String signText = "";
	public int priority;
	public boolean inUse;

	public SortChest(Block block, Block sign, String signText, int priority, boolean disregardDamage) {
		this.block = block;
		this.sign = sign;
		this.priority = priority;
		this.signText = signText;
		inUse = false;
	}
}
