package me.Chestsort.main;

import java.math.BigDecimal;

import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.java.JavaPlugin;

public class ChestSort extends JavaPlugin {
	boolean debugMessages = true;
	boolean debug = true;
	NetworkData networkData = new NetworkData(this);
	ChestGroupsData groupData = new ChestGroupsData(this);

	Sound sortSound;
	Sound notEnoughSpaceSound;

	CommandHandler commandHandler = new CommandHandler(this, networkData, groupData);

	@Override
	public void onEnable() {
		getConfig().options().copyDefaults(true);
		saveConfig();

		checkConfigData();

		networkData.loadNetworkData();
		groupData.setup();

		this.getServer().getPluginManager().registerEvents(new ChestSortListener(this, networkData, groupData), this);
		getLogger().info("ChestSort Loaded");
		super.onEnable();
		tempStuff();
	}

	private void checkConfigData() {
		boolean error = false;

		try {
			sortSound = Sound.valueOf(getConfig().getString("sort_sound"));
		} catch (IllegalArgumentException e) {
			getLogger().info("Invalid sort_sound, resetting to default");
			getConfig().set("sort_sound", "UI_TOAST_IN");
			sortSound = Sound.UI_TOAST_IN;
			error = true;
		}
		
		try {
			notEnoughSpaceSound = Sound.valueOf(getConfig().getString("not_enough_space_sound"));
		} catch (IllegalArgumentException e) {
			getLogger().info("Invalid not_enough_space_sound, resetting to default");
			getConfig().set("not_enough_space_sound", "ENTITY_BAT_TAKEOFF");
			sortSound = Sound.ENTITY_BAT_TAKEOFF;
			error = true;
		}

		if (error) {
			saveConfig();
		}
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
//		networkData.saveNetworkData();
//		groupData.saveGroupData();
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
