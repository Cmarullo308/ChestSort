package me.Chestsort.main;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import org.bukkit.block.Block;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

public class ChestSort extends JavaPlugin {
	boolean debugMessages = true;
	boolean debug = true;
	NetworkData networkdata;
	ChestGroupsData groupData = new ChestGroupsData(this);

	CommandHandler commandHandler = new CommandHandler();

	ConcurrentHashMap<String, Network> networks = new ConcurrentHashMap<String, Network>();

	@Override
	public void onEnable() {
		loadNetworkData();
		groupData.setup();

		tempStuff();

		this.getServer().getPluginManager().registerEvents(new ChestSortListener(this), this);
		super.onEnable();
	}

	private void tempStuff() {
//		groupData.addToGroup("Wood", Material.ACACIA_FENCE);
//		groupData.addToGroup("Wood", Material.OAK_BUTTON);
//		groupData.saveGroups();
//		debugMessage(groupData.groups.get("Wood").get(0) + "");
		
//		for(String key : groupData.groups.keySet()) {
//			for(String m : groupData.groups.get(key)) {
//				debugMessage(m.toString() + "\n");
//			}
//		}

	}

	private void loadNetworkData() {
		networkdata = new NetworkData(this);
		networkdata.setup();

		// From file to code
		Set<String> uuidStrings;
		try {
			uuidStrings = networkdata.getNetworks().getConfigurationSection("Owners").getKeys(false);
		} catch (NullPointerException e) {
			return;
		}

		// For each owner
		for (String uuidString : uuidStrings) {
			Set<String> networkNames = networkdata.getNetworks()
					.getConfigurationSection("Owners." + uuidString + ".NetworkNames").getKeys(false);
			// For each network
			for (String networkName : networkNames) {
				UUID uuid = UUID.fromString(uuidString);
				Network newNetwork = new Network(uuid, networkName);

				// --ADD MEMBERS--
				List<String> memberUuidStrings = networkdata.getNetworks()
						.getStringList("Owners." + uuidString + ".NetworkNames." + networkName + ".Members");
				ArrayList<UUID> members = new ArrayList<UUID>();
				// for each member
				for (String newUiidStr : memberUuidStrings) {
					members.add(UUID.fromString(newUiidStr));
				}

				newNetwork.members = members;
				// --------------

				// ADD DEPOSIT CHESTS
				Set<String> depositChests = networkdata.getNetworks()
						.getConfigurationSection(
								"Owners." + uuidString + ".NetworkNames." + networkName + ".DepositChests")
						.getKeys(false);

				// -for each deposit chest
				for (String chest : depositChests) {
					String[] chestAndLoc = chest.split(",");
					Block newChestBlock = getServer().getWorld(chestAndLoc[0]).getBlockAt(
							Integer.parseInt(chestAndLoc[1]), Integer.parseInt(chestAndLoc[2]),
							Integer.parseInt(chestAndLoc[3]));
					String[] sign = networkdata.getNetworks().getString("Owners." + uuidString + ".NetworkNames."
							+ networkName + ".DepositChests." + chest + ".Sign").split(",");
					Block newSignBlock = getServer().getWorld(sign[0]).getBlockAt(Integer.parseInt(sign[1]),
							Integer.parseInt(sign[2]), Integer.parseInt(sign[3]));

					newNetwork.addDepositChest(newNetwork, newChestBlock, newSignBlock);
				}
				debugMessage(newNetwork.members.size() + " AAAAAAA");
				// ------------------

				// ADD SORT CHESTS

				// ---------------

			}
		}
	}

	@Override
	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
		commandHandler.command(sender, command, label, args);
		return true;
	}

	@Override
	public void onDisable() {

	}

	public void createNewNetwork(Player player, String newNetworkName) {
		Network newNetwork = new Network(player.getUniqueId(), newNetworkName);

		networks.put(newNetworkName, newNetwork);
		networkdata.getNetworks().saveToString();
		networkdata.saveNetworkData();
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
