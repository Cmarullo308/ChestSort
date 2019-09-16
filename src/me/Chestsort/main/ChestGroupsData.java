package me.Chestsort.main;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import net.md_5.bungee.api.ChatColor;

public class ChestGroupsData {
	ChestSort plugin;

	HashMap<String, List<String>> groups = new HashMap<String, List<String>>();
	ArrayList<String> itemsInGroups = new ArrayList<String>();

	// Files and FileConfigs
	public FileConfiguration groupsFileConfig;
	public File groupsFile;

	public ChestGroupsData(ChestSort plugin) {
		this.plugin = plugin;
	}

	public void setup() {
		if (!plugin.getDataFolder().exists()) {
			plugin.getDataFolder().mkdir();
		}

		groupsFile = new File(plugin.getDataFolder(), "groups.yml");

		if (!groupsFile.exists()) {
			try {
				groupsFile.createNewFile();
			} catch (IOException e) {
				plugin.getServer().getLogger().info(ChatColor.RED + "Could not create groups.yml file");
			}

		}

		groupsFileConfig = YamlConfiguration.loadConfiguration(groupsFile);
		loadGroups();
	}

	public void saveGroups() {

		for (String group : groups.keySet()) {
			groupsFileConfig.set("Groups." + group, groups.get(group));
		}

		saveGroupData();
	}

	public void saveGroup(String groupName) {
		if (isValidGroup(groupName)) {
			groupsFileConfig.set("Groups." + groupName, groups.get(groupName));
		} else {
			groupsFileConfig.set("Groups." + groupName, null);
		}

		saveGroupData();
	}

	public void loadGroups() {
		Set<String> groupNames;
		try {
			groupNames = groupsFileConfig.getConfigurationSection("Groups").getKeys(false);
		} catch (NullPointerException e) {
			return;
		}

		for (String group : groupNames) { // For each group
			if (!group.equalsIgnoreCase("Misc")) {
				List<String> newMaterialList = groupsFileConfig.getStringList("Groups." + group);
				for (int i = 0; i < newMaterialList.size(); i++) { // for each item in group
					// Test if valid item
					try {
						Material.valueOf(newMaterialList.get(i));
					} catch (IllegalArgumentException e) {
						plugin.getLogger().info("Invalid material: \"" + newMaterialList.get(i) + "\", Ignoring");
						newMaterialList.remove(i);
					}
					// checks if items in a group already
					if (itemsInGroups.contains(newMaterialList.get(i))) {
						plugin.getLogger()
								.info("--" + newMaterialList.get(i) + " alread belongs to a group. Removing--");
						newMaterialList.remove(newMaterialList.get(i));
					} else {
						itemsInGroups.add(newMaterialList.get(i));
					}
				}
				groups.put(group, newMaterialList);
			} else {
				groupsFileConfig.set("Groups." + group, null);
			}
		}

		saveGroups();
	}

	public boolean itemIsInAGroup(Material item) {
		for (List<String> group : groups.values()) {
			if (group.contains(item.toString())) {
				return true;
			}
		}

		return false;
	}

	public List<String> getGroup(String groupName) {
		return groups.get(groupName);
	}

	public boolean isValidGroup(String groupName) {
		return groups.get(groupName) != null ? true : false;
	}

	/**
	 * 
	 * @param groupName
	 * @param item
	 * @return 0 if successful, -2 if the items already in a group, -1 if the group
	 *         doesn't exist
	 */
	public int addItemToGroup(String groupName, Material item) {
		if (itemIsInAGroup(item)) {
			return -2;
		}

		if (!isValidGroup(groupName)) {
			return -1;
		}
		
		groups.get(groupName).add(item.toString());
		saveGroup(groupName);
		return 0;
	}

	public boolean addGroup(String groupName) {
		if (isValidGroup(groupName)) {
			return false;
		}

		List<String> newList = Arrays.asList();
		groups.put(groupName, newList);
		saveGroup(groupName);

		loadGroups(); //reload groups file
		return true;
	}

	/**
	 * 
	 * @param groupName
	 * @return false if the group doesn't exist already
	 */
	public boolean removeGroup(String groupName) {
		if (!isValidGroup(groupName)) {
			return false;
		}

		groups.remove(groupName);
		plugin.networkData.disableAllChestsWithGroup(groupName);
		saveGroup(groupName);

		return true;
	}

	public FileConfiguration getGroups() {
		return groupsFileConfig;
	}

	public void saveGroupData() {
		try {
			groupsFileConfig.save(groupsFile);
		} catch (IOException e) {
			plugin.getServer().getLogger().info(ChatColor.RED + "Could not save groups.yml file");
		}
	}

	public String getGroupName(Material material) {
		return getGroupName(material.toString());
	}

	public String getGroupName(String material) {
		for (String group : groups.keySet()) {
			if (groups.get(group).contains(material)) {
				return group;
			}
		}

		return null;
	}

	public void removeFromGroup(Material material) {
		removeFromGroup(material.toString());
	}

	public void removeFromGroup(String material) {
		for (String group : groups.keySet()) {
			if (groups.get(group).contains(material)) {
				groups.get(group).remove(material);
				saveGroup(group);
			}
		}
	}
}
