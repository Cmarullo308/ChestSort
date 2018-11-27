package me.Chestsort.main;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.block.SignChangeEvent;
import org.bukkit.event.Listener;

import me.Chestsort.main.ChestSort;
import net.md_5.bungee.api.ChatColor;

public class ChestSortListener implements Listener {

	ChestSort plugin;

	public ChestSortListener(ChestSort plugin) {
		this.plugin = plugin;
	}

	@EventHandler
	public void SignChangeEvent(SignChangeEvent e) {
		if (!e.getLine(0).startsWith("*")) {
			return;
		}
		// If block under sign isn't a chest
		if (e.getBlock().getLocation().add(0, -1, 0).getBlock().getType() != Material.CHEST) {
			plugin.debugMessage("Not chest");
			return;
		}

		Player player = e.getPlayer();

		String newNetworkName = e.getLine(0).substring(1);

		// If deposit chest
		if (e.getLine(1).equals("")) {
			// If network doesn't exist
			if (plugin.networks.get(newNetworkName) == null) {
				plugin.createNewNetwork(player, newNetworkName);
				player.sendMessage("Network " + newNetworkName + " created");
			} else {
				plugin.debugMessage("Network already exists");
			}

			e.setLine(1, ChatColor.GRAY + "Open Chest");
			e.setLine(2, ChatColor.GRAY + "To Deposit");
			Network newNetwork = plugin.networks.get(newNetworkName);
			newNetwork.addDepositChest(newNetwork, e.getBlock().getLocation().add(0, -1, 0).getBlock(), e.getBlock());
			plugin.networkdata.saveNetwork(newNetwork);
		}

		plugin.debugMessage(plugin.networks.get(newNetworkName).depositChests.size() + " DC");

		if (plugin.debug) {
			plugin.saveNetworksToFile();
		}
	}
}
