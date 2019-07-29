package me.Chestsort.main;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Scanner;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;
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
		case "priority":
			priorityCommands(sender, args);
			break;
		case "group":
			groupCommands(sender, args);
			break;
		case "groupof":
			groupOfCommand(sender, args);
			break;
		case "listgroups":
			listGroupNames(sender, args);
			break;
		case "sound":
			soundCommands(sender, args);
			break;
		case "help":
			helpMenu(sender, args);
			break;
		case "test":
			testCommand(sender, args);
			break;
		default:
			sender.sendMessage(ChatColor.RED + "Invalid arguements");
			break;
		}

		return true;
	}

	private void listGroupNames(CommandSender sender, String[] args) {
		String message = "";
		if (args.length == 2 && args[1].equalsIgnoreCase("all")) {
			if (plugin.groupData.groups.keySet().size() == 0) {
				sender.sendMessage("There are no groups");
				return;
			}

			for (String groupName : plugin.groupData.groups.keySet()) {
				message += ChatColor.GREEN + "" + groupName;
				if (plugin.groupData.groups.get(groupName).size() == 0) {
					message += ChatColor.WHITE + "[]" + "\n\n";
				} else {
					Collections.sort(plugin.groupData.groups.get(groupName));
					for (String item : plugin.groupData.groups.get(groupName)) {
						message += "\n" + ChatColor.WHITE + "- " + item + "\n";
					}
				}
			}

			sender.sendMessage(message);
		} else if (args.length == 1) {
			if (plugin.groupData.groups.keySet().size() == 0) {
				sender.sendMessage("There are no groups");
				return;
			}

			message += "Groups:";
			
			List<String> list = new ArrayList<String>(plugin.groupData.groups.keySet());
			Collections.sort(list);
			
			for (String groupName : list) {
				message += "\n" + "- " + groupName;
			}
			sender.sendMessage(message);
		} else if (args.length > 2) {
			sender.sendMessage(ChatColor.RED + "Invalid number of arguements");
		} else {
			sender.sendMessage(ChatColor.RED + "Invalid arguements");
		}

	}

	private void helpMenu(CommandSender sender, String[] args) {
		sender.sendMessage(plugin.helpMenuMessage);
	}

	private void groupOfCommand(CommandSender sender, String[] args) {
		if (args.length != 2) {
			sender.sendMessage(ChatColor.RED + "Invalid number of arguements");
			return;
		}

		if (!sender.hasPermission("chestsort.groupof")) {
			noPermission(sender);
			return;
		}

		Material material;
		try {
			material = Material.valueOf(args[1].toUpperCase());
		} catch (IllegalArgumentException e) {
			sender.sendMessage(ChatColor.RED + "Invalid item name");
			return;
		}

		String groupName = groupData.getGroupName(material);
		if (groupName == null) {
			sender.sendMessage(ChatColor.YELLOW + material.toString() + ChatColor.GREEN + " is not in a group");
			return;
		} else {
			sender.sendMessage(ChatColor.YELLOW + material.toString() + ChatColor.GREEN + " is in the group "
					+ ChatColor.YELLOW + groupName);
			return;
		}
	}

	private void noPermission(CommandSender sender) {
		sender.sendMessage(ChatColor.RED + "You do not have permission to run this command");
	}

	private void groupCommands(CommandSender sender, String[] args) {
		if (args.length > 4 || args.length < 3) {
			sender.sendMessage(ChatColor.RED + "Invalid number of arguements");
			return;
		}

		String groupName = args[1];
		String action = args[2];

		if (args.length == 4) {
			if (action.equalsIgnoreCase("additem")) {
				addToGroup(sender, args);
			} else if (action.equalsIgnoreCase("removeitem")) {
				removeFromGroup(sender, args);
			} else {
				sender.sendMessage(ChatColor.RED + "Invalid arguement");
				return;
			}
		} else if (args.length == 3) {
			if (action.equalsIgnoreCase("create")) {
				createGroup(sender, groupName);
			} else if (action.equalsIgnoreCase("remove")) {
				removeGroup(sender, groupName);
			} else if (action.equalsIgnoreCase("list")) {
				listGroupItems(sender, groupName);
			} else {
				sender.sendMessage(ChatColor.RED + "Invalid arguements");
				return;
			}
		}
	}

	private void listGroupItems(CommandSender sender, String groupName) {
		List<String> groupItems = groupData.getGroup(groupName);

		if (groupItems == null) {
			sender.sendMessage(
					ChatColor.RED + "The group " + ChatColor.YELLOW + groupName + ChatColor.RED + " does not exist");
			return;
		}

		String message = ChatColor.YELLOW + groupName + ": " + ChatColor.GREEN + "[";
		for (String itemName : groupItems) {
			message += ChatColor.YELLOW + itemName + ChatColor.GREEN + ", ";
		}

		if (groupItems.size() > 0) {
			message = message.substring(0, message.length() - 2);
		}

		message += ChatColor.GREEN + "]";

		sender.sendMessage(message);
	}

	private void removeGroup(CommandSender sender, String groupName) {
		if (!sender.hasPermission("chestsort.group.modifygroups")) {
			noPermission(sender);
			return;
		}

		if (!groupData.removeGroup(groupName)) {
			sender.sendMessage(ChatColor.RED + "No group named " + ChatColor.YELLOW + groupName);
			return;
		}

		sender.sendMessage(ChatColor.GREEN + "Removed the group: " + ChatColor.YELLOW + groupName);
	}

	private void createGroup(CommandSender sender, String groupName) {
		if (!sender.hasPermission("chestsort.group.modifygroups")) {
			noPermission(sender);
			return;
		}

		if (groupName.equalsIgnoreCase("misc")) {
			sender.sendMessage(ChatColor.RED + "The group cannot be named " + ChatColor.YELLOW + "Misc");
			return;
		}

		if (!groupData.addGroup(groupName)) {
			sender.sendMessage(
					ChatColor.RED + "The group " + ChatColor.YELLOW + groupName + ChatColor.RED + " already exists");
			return;
		}

		sender.sendMessage(ChatColor.GREEN + "Created new group: " + ChatColor.YELLOW + groupName);
	}

	private void removeFromGroup(CommandSender sender, String[] args) {
		if (!sender.hasPermission("chestsort.group.modifygroupitems")) {
			noPermission(sender);
			return;
		}

		Material material;
		String groupName = args[1];
		String itemName = args[3].toUpperCase();

		try {
			material = Material.valueOf(itemName);
		} catch (IllegalArgumentException e) {
			sender.sendMessage(ChatColor.RED + "Invalid item name " + ChatColor.YELLOW + itemName);
			return;
		}

		if (!groupData.itemIsInAGroup(material)) {
			sender.sendMessage(ChatColor.YELLOW + material.toString() + ChatColor.RED + " is not in any group");
			return;
		}

		groupData.removeFromGroup(material);

		sender.sendMessage(ChatColor.GREEN + "The item " + ChatColor.YELLOW + material.toString() + ChatColor.GREEN
				+ " was removed from the group " + ChatColor.YELLOW + groupName);
	}

	private void addToGroup(CommandSender sender, String[] args) {
		if (!sender.hasPermission("chestsort.group.modifygroupitems")) {
			noPermission(sender);
			return;
		}

		Material material;
		String groupName = args[1];
		String itemName = args[3].toUpperCase();

		try {
			material = Material.valueOf(itemName);
		} catch (IllegalArgumentException e) {
			sender.sendMessage(ChatColor.RED + "Invalid item name " + ChatColor.YELLOW + itemName);
			return;
		}

		int result = groupData.addItemToGroup(groupName, material);
		if (result == -1) {
			sender.sendMessage(ChatColor.RED + "No group named " + ChatColor.YELLOW + groupName);
			return;
		} else if (result == -2) {
			sender.sendMessage(ChatColor.RED + "The item " + ChatColor.YELLOW + itemName + ChatColor.RED
					+ " is already in a group");
			return;
		}

		sender.sendMessage(ChatColor.GREEN + "Added the item " + ChatColor.YELLOW + itemName + ChatColor.GREEN
				+ " to the group " + ChatColor.YELLOW + groupName);
	}

	private void soundCommands(CommandSender sender, String[] args) {
		if (args.length == 2) {
			if (args[1].equalsIgnoreCase("list")) {
				listSounds(sender, args);
			} else {
				sender.sendMessage(ChatColor.RED + "Invalid arguements");
				return;
			}
		} else if (args.length == 4) {
			if (args[2].equalsIgnoreCase("set")) {
				setSound(sender, args);
				return;
			} else {
				sender.sendMessage(ChatColor.RED + "Invalid arguements");
				return;
			}
		} else if (args.length == 3) {
			if (args[2].equalsIgnoreCase("get")) {
				getSound(sender, args);
				return;
			} else if (args[2].equalsIgnoreCase("enable") || args[2].equalsIgnoreCase("disable")) {
				enableDisableSound(sender, args);
				return;
			} else {
				sender.sendMessage(ChatColor.RED + "Invalid arguements");
				return;
			}
		} else {
			sender.sendMessage(ChatColor.RED
					+ "Invalid number of arguements. /ChestSort sound <whichSound> <set | get | enable | disable> [sound]");
			return;
		}
	}

	private void enableDisableSound(CommandSender sender, String[] args) {
		if (!sender.hasPermission("chestsort.sound.toggle")) {
			noPermission(sender);
			return;
		}

		String soundName = args[1];
		boolean enabled;
		if (args[2].equalsIgnoreCase("enable")) {
			enabled = true;
		} else if (args[2].equalsIgnoreCase("disable")) {
			enabled = false;
		} else {
			sender.sendMessage(ChatColor.RED + "Invalid arguement");
			return;
		}

		if (!plugin.setSoundEnabled(soundName, enabled)) {
			sender.sendMessage(ChatColor.RED + "Invalid sound name");
			return;
		}

		if (enabled) {
			sender.sendMessage(ChatColor.GREEN + soundName + " enabled");
		} else {
			sender.sendMessage(ChatColor.GREEN + soundName + " disabled");
		}
	}

	private void listSounds(CommandSender sender, String[] args) {
		if (!sender.hasPermission("chestsort.sound.set")) {
			noPermission(sender);
			return;
		}

		String message = ChatColor.DARK_BLUE + "---------------------------\n";

		message += ChatColor.GREEN + "Sort_sound: " + ChatColor.YELLOW + plugin.sortSound.toString() + "\n";
		message += ChatColor.GREEN + "Not_enough_space_sound: " + ChatColor.YELLOW
				+ plugin.notEnoughSpaceSound.toString() + "\n";
		message += ChatColor.DARK_BLUE + "---------------------------";

		sender.sendMessage(message);
	}

	private void getSound(CommandSender sender, String[] args) {
		if (!sender.hasPermission("chestsort.sound.get")) {
			noPermission(sender);
			return;
		}

		String soundName = args[1];
		if (soundName.equalsIgnoreCase("sort_sound")) {
			sender.sendMessage(ChatColor.GREEN + "Sort_sound: " + ChatColor.YELLOW + plugin.sortSound.toString());
			return;
		} else if (soundName.equalsIgnoreCase("not_enough_space_sound")) {
			sender.sendMessage(ChatColor.GREEN + "not_enough_space_sound: " + ChatColor.YELLOW
					+ plugin.notEnoughSpaceSound.toString());
			return;
		} else {
			sender.sendMessage(ChatColor.RED + "Invalud sound name");
			return;
		}
	}

	private void setSound(CommandSender sender, String[] args) {
		if (!sender.hasPermission("chestsort.sound.set")) {
			noPermission(sender);
			return;
		}

		String soundName = args[1];
		String sound = args[3];

		if (soundName.equalsIgnoreCase("sort_sound")) {
			try {
				plugin.sortSound = Sound.valueOf(sound);
				plugin.getConfig().set("sort_sound", plugin.sortSound.toString());
			} catch (IllegalArgumentException e) {
				sender.sendMessage(ChatColor.RED + "Invalid sound " + ChatColor.RED + sound);
				return;
			}
		} else if (soundName.equalsIgnoreCase("not_enough_space_sound")) {
			try {
				plugin.notEnoughSpaceSound = Sound.valueOf(sound);
				plugin.getConfig().set("sort_sound", plugin.notEnoughSpaceSound.toString());
			} catch (IllegalArgumentException e) {
				sender.sendMessage(ChatColor.RED + "Invalid sound " + ChatColor.RED + sound);
				return;
			}
		} else {
			sender.sendMessage(ChatColor.RED + "Invalid sound name " + ChatColor.YELLOW + soundName);
			return;
		}

		plugin.saveConfig();
		sender.sendMessage(ChatColor.YELLOW + soundName + ChatColor.GREEN + " set to " + ChatColor.YELLOW + sound);
	}

	private void networkCommands(CommandSender sender, String[] args) {
		if (args.length == 2) {
			if (args[1].equalsIgnoreCase("list")) {
				listNetworks(sender, args);
			}
		} else if (args.length == 3) {
			if (args[2].equalsIgnoreCase("create")) {
				createNewNetwork(sender, args[1]);
			} else if (args[2].equalsIgnoreCase("remove")) {
				removeNetwork(sender, args[1]);
			} else if (args[2].equalsIgnoreCase("info")) {
				printNetworkInfo(sender, args[1]);
			} else {
				sender.sendMessage(ChatColor.RED + "Invalid arguement");
				return;
			}
		} else if (args.length == 5) {
			if (args[2].equalsIgnoreCase("members")) {
				membersCommands(sender, args);
			} else {
				sender.sendMessage(ChatColor.RED + "Invalid arguement");
				return;
			}
		} else {
			sender.sendMessage(ChatColor.RED + "Invalid number of arguements");
		}
	}

	private void listNetworks(CommandSender sender, String[] args) {
		if (!sender.hasPermission("chestsort.network.list")) {
			noPermission(sender);
			return;
		}

		String message = ChatColor.GREEN + "Networks: [";
		for (Network network : networkData.networks.values()) {
			message += ChatColor.WHITE + network.networkName + ChatColor.GREEN + ", ";
		}

		if (networkData.networks.size() > 0) {
			message = message.substring(0, message.length() - 2);
		}

		message += "]";

		sender.sendMessage(message);
	}

	private void membersCommands(CommandSender sender, String[] args) {
		if (args[3].equalsIgnoreCase("add")) {
			Thread thread = new Thread() {
				@Override
				public void run() {
					addMember(sender, args);
				}
			};
			thread.start();
		} else if (args[3].equalsIgnoreCase("remove")) {
			Thread thread = new Thread() {
				@Override
				public void run() {
					removeMember(sender, args);
				}
			};
			thread.start();
		}
	}

	private void removeMember(CommandSender sender, String[] args) {
		String networkName = args[1];
		String memberName = args[4];

		if (!networkData.networkExists(networkName)) {
			sender.sendMessage(ChatColor.RED + "No network named " + ChatColor.YELLOW + networkName);
			return;
		}

		Network network = networkData.getNetwork(networkName);

		if (!(sender instanceof Player) && !network.isOwner((Player) sender) && !sender.isOp()) {
			sender.sendMessage(ChatColor.RED + "Must be the owner or a memebr of the network " + ChatColor.YELLOW
					+ network.networkName + ChatColor.RED + " to modify its members");
			return;
		}

		UUID member;
		try {
			member = Bukkit.getPlayer(memberName).getUniqueId();
		} catch (NullPointerException e) {
			member = null;
		}
		if (member == null) {
			member = getOfflinePlayer(sender, memberName);
			if (member == null) {
				sender.sendMessage(ChatColor.RED + "No player named " + ChatColor.YELLOW + memberName);
				return;
			}
		}

		if (network.isMember(member)) {
			network.removeMember(member);
			networkData.saveNetwork(network, true);
			sender.sendMessage(ChatColor.GREEN + "You removed " + ChatColor.YELLOW + memberName + ChatColor.GREEN
					+ " as a member from the network " + ChatColor.YELLOW + networkName);
		} else {
			sender.sendMessage(ChatColor.YELLOW + memberName + ChatColor.RED + " is not a member of the network "
					+ ChatColor.YELLOW + networkName);
		}
	}

	private void addMember(CommandSender sender, String[] args) {
		String networkName = args[1];
		String newMemberName = args[4];

		if (!networkData.networkExists(networkName)) {
			sender.sendMessage(ChatColor.RED + "No network named " + ChatColor.YELLOW + networkName);
			return;
		}

		Network network = networkData.getNetwork(networkName);

		if (!(sender instanceof Player) && !network.isOwner((Player) sender) && !sender.isOp()) {
			sender.sendMessage(ChatColor.RED + "Must be the owner or a memebr of the network " + ChatColor.YELLOW
					+ network.networkName + ChatColor.RED + " to modify its members");
			return;
		}
		UUID newMember;
		try {
			newMember = Bukkit.getPlayer(newMemberName).getUniqueId();
		} catch (NullPointerException e) {
			newMember = null;
		}
		if (newMember == null) {
			newMember = getOfflinePlayer(sender, newMemberName);
			if (newMember == null) {
				sender.sendMessage(ChatColor.RED + "No player named " + ChatColor.YELLOW + newMemberName);
				return;
			}
		}

		if (!network.isMember(newMember)) {
			network.addMember(newMember);
			networkData.saveNetwork(network, true);
			sender.sendMessage(ChatColor.GREEN + "You added " + ChatColor.YELLOW + newMemberName + ChatColor.GREEN
					+ " as a member to the network " + ChatColor.YELLOW + networkName);
		} else {
			sender.sendMessage(ChatColor.YELLOW + newMemberName + ChatColor.RED + " is already a member of the network "
					+ ChatColor.YELLOW + networkName);
		}

	}

	private UUID getOfflinePlayer(CommandSender sender, String newMemberName) {
		URL api;
		try {
			api = new URL("https://api.mojang.com/users/profiles/minecraft/" + newMemberName);
		} catch (MalformedURLException e) {
			e.printStackTrace();
			sender.sendMessage(ChatColor.RED + "Could not get user from mojang api");
			return null;
		}
		Scanner scanner = null;
		try {
			scanner = new Scanner(api.openStream());
		} catch (IOException e) {
			e.printStackTrace();
			sender.sendMessage(ChatColor.RED + "Error getting user");
			return null;
		}

		String idString = scanner.nextLine();

		UUID id = null;
		try {
			id = UUID.fromString(idString.substring(7, 15) + "-" + idString.substring(15, 19) + "-"
					+ idString.substring(19, 23) + "-" + idString.substring(23, 27) + "-" + idString.substring(27, 39));
		} catch (IllegalArgumentException e) {
			sender.sendMessage(ChatColor.RED + "Invalid username");
			scanner.close();
			return null;
		} catch (Exception e) {
			scanner.close();
			e.printStackTrace();
		}

		scanner.close();
		return id;
	}

	private void priorityCommands(CommandSender sender, String[] args) {
		if (args.length != 2 && args.length != 3) {
			sender.sendMessage(ChatColor.RED + "Invalid number of arguements. /SortChest <priority> <set | get>");
			return;
		} else if (!(sender instanceof Player)) {
			mustBeAPlayerMessage(sender);
			return;
		}

		if (args[1].equalsIgnoreCase("set")) {
			setPriority(sender, args);
		} else if (args[1].equalsIgnoreCase("get")) {
			getPriority(sender, args);
		}
	}

	private void getPriority(CommandSender sender, String[] args) {
		if (!sender.hasPermission("chestsort.priority.get")) {
			noPermission(sender);
			return;
		}

		Player player = (Player) sender;
		Block lookingAt = player.getTargetBlockExact(20);
		SortChest sortChest = null;

		if (lookingAt.getType() == Material.CHEST) {
			Block signBlock = lookingAt.getLocation().clone().add(0, 1, 0).getBlock();
			if (isAWallSign(signBlock.getType())) {
				Sign sign = (Sign) signBlock.getState();
				try {
					sortChest = networkData.getSortChestBySign(signBlock, sign.getLine(0).substring(3));
				} catch (StringIndexOutOfBoundsException e) {
					sortChest = null;
				}
			}
		} else if (isAWallSign(lookingAt.getType())) {
			if (isAWallSign(lookingAt.getType())) {
				Sign sign = (Sign) lookingAt.getState();
				try {
					sortChest = networkData.getSortChestBySign(lookingAt, sign.getLine(0).substring(3));
				} catch (StringIndexOutOfBoundsException e) {
					sortChest = null;
				}
			}
		} else {
			sender.sendMessage(ChatColor.RED + "Must be looking at a chest or sign");
			return;
		}

		// Chest is not part of a network
		if (sortChest == null) {
			player.sendMessage(ChatColor.RED + "This chest is not part of a network or is a deposit chest");
			return;
		}

		player.sendMessage(ChatColor.GREEN + "Priority: " + ChatColor.YELLOW + sortChest.priority);
	}

	private boolean isAWallSign(Material type) {
		switch (type) {
		case OAK_WALL_SIGN:
		case DARK_OAK_WALL_SIGN:
		case JUNGLE_WALL_SIGN:
		case BIRCH_WALL_SIGN:
		case ACACIA_WALL_SIGN:
		case SPRUCE_WALL_SIGN:
			return true;
		default:
			return false;
		}
	}

	private void setPriority(CommandSender sender, String[] args) {
		if (!sender.hasPermission("chestsort.priority.set")) {
			noPermission(sender);
			return;
		}

		Player player = (Player) sender;
		Block lookingAt = player.getTargetBlockExact(20);
		SortChest sortChest = null;

		if (lookingAt.getType() == Material.CHEST) {
			Block signBlock = lookingAt.getLocation().clone().add(0, 1, 0).getBlock();
			if (isAWallSign(signBlock.getType())) {
				Sign sign = (Sign) signBlock.getState();
				try {
					sortChest = networkData.getSortChestBySign(signBlock, sign.getLine(0).substring(3));
				} catch (StringIndexOutOfBoundsException e) {
					sortChest = null;
				}
			}
		} else if (isAWallSign(lookingAt.getType())) {
			if (isAWallSign(lookingAt.getType())) {
				Sign sign = (Sign) lookingAt.getState();
				try {
					sortChest = networkData.getSortChestBySign(lookingAt, sign.getLine(0).substring(3));
				} catch (StringIndexOutOfBoundsException e) {
					sortChest = null;
				}
			}
		} else {
			sender.sendMessage(ChatColor.RED + "Must be looking at a chest or sign");
			return;
		}

		// Chest is not part of a network
		if (sortChest == null) {
			player.sendMessage(ChatColor.RED + "This chest is not part of a network or is a deposit chest");
			return;
		}

		Network network = sortChest.getNetwork();
		if (!network.isOwner(player) && !network.isMember(player)) {
			player.sendMessage(ChatColor.RED + "Must be the owner or a memebr of the network " + ChatColor.YELLOW
					+ network.networkName + ChatColor.RED + " to modify its chests");
			return;
		}

		try {
			sortChest.priority = Integer.parseInt(args[2]);
		} catch (NumberFormatException e) {
			player.sendMessage(ChatColor.RED + "Invalid priority number. Must be an integer");
			return;
		}

		sortChest.updateSign();

		networkData.saveNetwork(network, true);

		player.sendMessage(ChatColor.GREEN + "Chest priority set to " + ChatColor.YELLOW + sortChest.priority);
	}

	private void printNetworkInfo(CommandSender sender, String networkName) {
		if (!sender.hasPermission("chestsort.network.info")) {
			noPermission(sender);
			return;
		}

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
		if (network.members.size() > 0) {
			networkInfoMessage = networkInfoMessage.substring(0, networkInfoMessage.length() - 1);
		}
		networkInfoMessage += "]";
		networkInfoMessage += ChatColor.DARK_BLUE + "\n---------------------------";

		sender.sendMessage(networkInfoMessage);
	}

	private void removeNetwork(CommandSender sender, String networkName) {
		if (!sender.hasPermission("chestsort.network.remove")) {
			noPermission(sender);
			return;
		}

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
		sender.sendMessage(
				ChatColor.GREEN + "Network " + ChatColor.YELLOW + networkName + ChatColor.GREEN + " removed");
	}

	private void createNewNetwork(CommandSender sender, String newNetworkName) {
		if (!sender.hasPermission("chestsort.network.create")) {
			noPermission(sender);
			return;
		}

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
		networkData.saveNetwork(newNetwork, true);

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
