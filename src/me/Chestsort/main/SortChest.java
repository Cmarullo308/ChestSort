package me.Chestsort.main;

import org.bukkit.block.Block;
import org.bukkit.block.Sign;

import net.md_5.bungee.api.ChatColor;

public class SortChest {
	public Block block;
	public Block sign;
	public String group;
	public int priority;
	public boolean inUse;
	public Network network;

	public SortChest(Block block, Block sign, String group, int priority, Network network) {
		this.block = block;
		this.sign = sign;
		this.priority = priority;
		this.group = group;
		inUse = false;
		this.network = network;
	}

	public void updateSign() {
		Sign signData = (Sign) sign.getState();
		signData.setLine(0, network.plugin.signNetworkColor + "*" + network.networkName);
		signData.setLine(1, group);
		signData.setLine(2, ChatColor.GRAY + "Priority: " + priority);
		signData.update();
	}

	public Network getNetwork() {
		return this.network;
	}
}
