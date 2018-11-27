package me.Chestsort.main;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.java.JavaPlugin;

public class ChestSort extends JavaPlugin {
	boolean debugMessages = true;
	boolean debug = true;
	NetworkData networkdata = new NetworkData(this);
	ChestGroupsData groupData = new ChestGroupsData(this);

	CommandHandler commandHandler = new CommandHandler();

	@Override
	public void onEnable() {
		networkdata.loadNetworkData();
		groupData.setup();


		this.getServer().getPluginManager().registerEvents(new ChestSortListener(this, networkdata, groupData), this);
		getLogger().info("ChestSort Loaded");
		super.onEnable();
		tempStuff();
	}

	private void tempStuff() {
		debugMessage(networkdata.networks.get("Fard").sortChests.get(0).group + "\n\n\n\n\n\n\n");

//		groupData.saveGroupData();
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
		networkdata.saveNetworkData();
	}

}
