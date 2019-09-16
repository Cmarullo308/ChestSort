package me.Chestsort.main;

import java.math.BigDecimal;

import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.java.JavaPlugin;

import net.md_5.bungee.api.ChatColor;

public class ChestSort extends JavaPlugin {
	boolean debugMessages = false;
	boolean debug = false;
	NetworkData networkData = new NetworkData(this);
	ChestGroupsData groupData = new ChestGroupsData(this);

	// Sounds
	Sound sortSound;
	boolean sortSoundEnabled;
	Sound notEnoughSpaceSound;
	boolean notEnoughSpaceSoundEnabled;
	// ------

	String helpMenuMessage;

	int defaultChestPriority;

	final ChatColor signNetworkColor = ChatColor.DARK_BLUE;

	CommandHandler commandHandler = new CommandHandler(this, networkData, groupData);

	@Override
	public void onEnable() {
		getConfig().options().copyDefaults(true);
		saveConfig();

		loadAndCheckConfigData();

		networkData.loadNetworkData();
		groupData.setup();

		createHelpMenuMessage();

		this.getServer().getPluginManager().registerEvents(new ChestSortListener(this, networkData, groupData), this);
		getLogger().info("ChestSort Loaded");
		super.onEnable();
		tempStuff();
	}

	private void createHelpMenuMessage() {
		helpMenuMessage = ChatColor.BOLD + "Commands\n";

		helpMenuMessage += ChatColor.WHITE
				+ "  /chestsort group <groupname> <additem | removeitem | create | remove | list> <item>\n";
		helpMenuMessage += ChatColor.GRAY + "  /chestsort groupof <itemName>\n";
		helpMenuMessage += ChatColor.WHITE + " /chestsort listgroups <all>\n";
		helpMenuMessage += ChatColor.GRAY + "  /chestsort network <networkName> <remove | create | info>\n";
		helpMenuMessage += ChatColor.WHITE + "  /chestsort network list\n";
		helpMenuMessage += ChatColor.GRAY
				+ "  /chestsort network <networkName> <members> <add | remove> <playerName>\n";
		helpMenuMessage += ChatColor.WHITE + "  /chestsort priority get\n";
		helpMenuMessage += ChatColor.GRAY + "  /chestsort priority set <number>\n";
		helpMenuMessage += ChatColor.WHITE + "  /chestsort sound list\n";
		helpMenuMessage += ChatColor.GRAY + "  /chestsort sound <whichSound> <set | get | enable | disable> [sound]\n";
	}

	private void loadAndCheckConfigData() {
		defaultChestPriority = getConfig().getInt("default_chest_priority");
		getConfig().set("default_chest_priority", defaultChestPriority);

		try {
			sortSound = Sound.valueOf(getConfig().getString("sort_sound"));
		} catch (IllegalArgumentException e) {
			getLogger().info("Invalid sort_sound, resetting to default");
			getConfig().set("sort_sound", "UI_TOAST_IN");
			sortSound = Sound.UI_TOAST_IN;
		}

		try {
			notEnoughSpaceSound = Sound.valueOf(getConfig().getString("not_enough_space_sound"));
		} catch (IllegalArgumentException e) {
			getLogger().info("Invalid not_enough_space_sound, resetting to default");
			getConfig().set("not_enough_space_sound", "ENTITY_BAT_TAKEOFF");
			sortSound = Sound.ENTITY_BAT_TAKEOFF;
		}

		sortSoundEnabled = getConfig().getBoolean("sort_sound_enabled");
		notEnoughSpaceSoundEnabled = getConfig().getBoolean("not_enough_space_sound_enabled");

		saveConfig();
	}

	public boolean setSoundEnabled(String soundName, boolean enabled) {
		boolean worked = false;
		if (soundName.equalsIgnoreCase("sort_sound")) {
			sortSoundEnabled = enabled;
			worked = true;
		} else if (soundName.equalsIgnoreCase("not_enough_space_sound")) {
			notEnoughSpaceSoundEnabled = enabled;
			worked = true;
		}

		if (worked) {
			getConfig().set(soundName + "_enabled", enabled);
			saveConfig();
			return true;
		}

		return worked;
	}

	private void tempStuff() {

	}

	@Override
	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
		commandHandler.command(sender, command, label, args);
		return true;
	}

	@Override
	public void onDisable() {

	}

	public void debugMessage(String str) {
		if (debugMessages) {
			getLogger().info(str);
		}
	}

	public void saveNetworksToFile() {
		networkData.saveNetworkData();
	}

	public void printLocation(Location loc) {
		double x = new BigDecimal(loc.getX()).doubleValue();
		double y = loc.getY();
		double z = new BigDecimal(loc.getZ()).doubleValue();
		getLogger().info(loc.getWorld().getName() + ": " + x + ", " + y + ", " + z);
	}
}
