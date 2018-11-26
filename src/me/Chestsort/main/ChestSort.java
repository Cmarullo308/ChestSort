package me.Chestsort.main;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.concurrent.ConcurrentHashMap;

import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

public class ChestSort extends JavaPlugin {
	boolean debugMessages = true;
	boolean debug = true;
	NetworkData networkdata;

	CommandHandler commandHandler = new CommandHandler();

	ConcurrentHashMap<String, Network> networks = new ConcurrentHashMap<String, Network>();

	@Override
	public void onEnable() {
		loadNetworkData();

		this.getServer().getPluginManager().registerEvents(new ChestSortListener(this), this);
		super.onEnable();
	}

	private void loadNetworkData() {
		networkdata = new NetworkData(this);
		networkdata.setup();
	}

	@Override
	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
		commandHandler.command(sender, command, label, args);
		return true;
	}

	@Override
	public void onDisable() {

	}

	public void createNewNetwork(Player player, String newNetworkName, World world) {
		Network newNetwork = new Network(player.getUniqueId(), newNetworkName, player.getWorld().toString());
		
		networks.put(newNetworkName, newNetwork);
		networkdata.saveNetwork(newNetwork);
	}

	public void debugMessage(String str) {
		if (debugMessages) {
			getLogger().info(str);
		}
	}
	
	public void saveNetworks() {
		networkdata.saveNetworkData();
	}

}
