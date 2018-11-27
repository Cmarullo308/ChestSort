package me.Chestsort.main;

import org.bukkit.block.Block;

public class NetworkItem {
	public Network network = null;
	public Block chest;
	public Block sign;

	public NetworkItem(Network network, Block chest, Block sign) {
		this.network = network;
		this.chest = chest;
		this.sign = sign;
	}

	public String getWorldString() {
		return chest.getWorld().toString();
	}
}
