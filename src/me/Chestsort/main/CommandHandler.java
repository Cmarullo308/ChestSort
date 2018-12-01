package me.Chestsort.main;

import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import net.md_5.bungee.api.ChatColor;

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
		case "network":
			networkCommands(sender, args);
			break;
		case "test":
			testCommand(sender, args);
			break;
		default:
			break;
		}

		return true;
	}

	private void networkCommands(CommandSender sender, String[] args) {
		if (args.length != 3) {
			sender.sendMessage(ChatColor.RED + "Invalid number of arguements." + ChatColor.GREEN
					+ " /SortChest <network> <networkName> <Create | Remove | Info>");
			return;
		}

		if (args[2].equalsIgnoreCase("create")) {
			createNewNetwork(sender, args[2]);
		} else if (args[2].equalsIgnoreCase("remove")) {
			removeNetwork(sender, args[1]);
		} else if (args[2].equalsIgnoreCase("info")) {
			printNetworkInfo(sender, args[1]);
		} else {
			sender.sendMessage(
					ChatColor.RED + "Invalid arguements. /SortChest <network> <networkName> <Create | Remove | Info>");
		}
	}

	private void printNetworkInfo(CommandSender sender, String networkName) {
		if (!networkData.networkExists(networkName)) {
			sender.sendMessage(ChatColor.RED + "There's no network named " + ChatColor.YELLOW + networkName);
			return;
		}
		Network network = networkData.getNetwork(networkName);

		String networkInfoMessage = ChatColor.DARK_BLUE + "---------------------------\n";
		networkInfoMessage += ChatColor.WHITE + "Name: " + ChatColor.YELLOW + network.networkName + "\n";
		networkInfoMessage += ChatColor.WHITE + "Number of Deposit Chests: " + ChatColor.YELLOW
				+ network.depositChests.size() + "\n";
		networkInfoMessage += ChatColor.WHITE + "Number of Sort Chests: " + ChatColor.YELLOW + network.sortChests.size()
				+ "\n";
		networkInfoMessage += ChatColor.WHITE + "Members: [";
		for (UUID memberId : network.members) {
			networkInfoMessage += ChatColor.YELLOW + Bukkit.getOfflinePlayer(memberId).getName() + ChatColor.WHITE
					+ ",";
		}
		networkInfoMessage.substring(0, networkInfoMessage.length() - 1);
		networkInfoMessage += "]";
		networkInfoMessage += ChatColor.DARK_BLUE + "\n---------------------------";
		
		sender.sendMessage(networkInfoMessage);
	}

	private void removeNetwork(CommandSender sender, String networkName) {
		Network network;
		if (!networkData.networkExists(networkName)) {
			sender.sendMessage(ChatColor.RED + "There's no network named " + ChatColor.YELLOW + networkName);
			return;
		}

		network = networkData.getNetwork(networkName);

		if (sender instanceof Player && !network.isOwner((Player) sender) && !sender.isOp()) {
			sender.sendMessage(ChatColor.RED + "You must be the owner of a network to remove it");
			return;
		}

		network.disableAllDepositChests();
		network.disableAllSortChests();
		networkData.removeNetwork(network);
	}

	private void createNewNetwork(CommandSender sender, String newNetworkName) {
		if (!(sender instanceof Player)) {
			mustBeAPlayerMessage(sender);
			return;
		} else if (networkData.networkExists(newNetworkName)) {
			sender.sendMessage(ChatColor.RED + "A network with that name already exists");
			return;
		}

		Player player = (Player) sender;

		Network newNetwork = new Network(player.getUniqueId(), newNetworkName, plugin);
		networkData.addNetwork(newNetworkName, newNetwork);
		networkData.saveNetwork(newNetwork);

		sender.sendMessage("Network " + ChatColor.YELLOW + newNetworkName + ChatColor.WHITE + " created");
	}

	private void mustBeAPlayerMessage(CommandSender sender) {
		sender.sendMessage(ChatColor.RED + "Must be a player to run this command");
	}

	private void testCommand(CommandSender sender, String[] args) {
		for (SortChest chest : networkData.networks.get(args[1]).sortChests) {
			plugin.debugMessage(chest.group);
		}
	}

}
