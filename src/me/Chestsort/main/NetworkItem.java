package me.Chestsort.main;

import org.bukkit.block.Block;

public class NetworkItem {
	public Network network = null;
	public Block chest;
	public Block sign;
	public boolean inUse;

	public NetworkItem(Network network, Block chest, Block sign) {
		this.network = network;
		this.chest = chest;
		this.sign = sign;
		this.inUse = false;
	}

	public String getWorldString() {
		return chest.getWorld().toString();
	}
}
