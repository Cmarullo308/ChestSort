package me.Chestsort.main;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;

public class CommandHandler {
	ChestSort plugin;
	NetworkData networkData;
	ChestGroupsData groupData;

	public CommandHandler(ChestSort plugin, NetworkData networkData, ChestGroupsData groupData) {
		this.plugin = plugin;
		this.networkData = networkData;
		this.groupData = groupData;
	}

	public boolean command(CommandSender sender, Command command, String label, String[] args) {

		switch (args[0].toLowerCase()) {
		case "test":
			testCommand(sender, args);
			break;
		default:
			break;
		}

		return true;
	}

	private void testCommand(CommandSender sender, String[] args) {
		for(SortChest chest : networkData.networks.get(args[1]).sortChests) {
			plugin.debugMessage(chest.group);
		}
	}

}
