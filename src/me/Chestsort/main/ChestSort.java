package me.Chestsort.main;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.java.JavaPlugin;

public class ChestSort extends JavaPlugin {
	boolean debugMessages = true;
	boolean debug = true;
	NetworkData networkData = new NetworkData(this);
	ChestGroupsData groupData = new ChestGroupsData(this);

	CommandHandler commandHandler = new CommandHandler(this, networkData, groupData);

	@Override
	public void onEnable() {
		networkData.loadNetworkData();
		groupData.setup();


		this.getServer().getPluginManager().registerEvents(new ChestSortListener(this, networkData, groupData), this);
		getLogger().info("ChestSort Loaded");
		super.onEnable();
		tempStuff();
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

}
