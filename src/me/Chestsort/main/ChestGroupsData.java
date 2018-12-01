package me.Chestsort.main;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
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
		groupsFileConfig.set("Groups." + groupName, groups.get(groupName));

		saveGroupData();
	}

	public void loadGroups() {
		Set<String> groupNames;
		try {
			groupNames = groupsFileConfig.getConfigurationSection("Groups").getKeys(false);
		} catch (NullPointerException e) {
			return;
		}

		// For each group
		for (String group : groupNames) {
			List<String> newMaterialList = groupsFileConfig.getStringList("Groups." + group);
			for (int i = 0; i < newMaterialList.size(); i++) {
				try {
					Material.valueOf(newMaterialList.get(i));
				} catch (IllegalArgumentException e) {
					plugin.getLogger().info("Invalid material: \"" + newMaterialList.get(i) + "\", Ignoring");
					newMaterialList.remove(i);
				}
			}
			groups.put(group, newMaterialList);
		}
	}

	public boolean itemIsInAGroup(Material item) {
		for (List<String> group : groups.values()) {
			if (group.contains(item.toString())) {
				return true;
			}
		}

		return false;
	}

	public boolean isValidGroup(String groupName) {
		return groups.get(groupName) != null ? true : false;
	}

	/**
	 * 
	 * @param groupName
	 * @param item
	 * @return 0 if successful, -2 if the items already in a group, -1 if the group doesn't exist
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

		return false;
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

	public void addToGroup(String groupName, String material) {
		if (groups.get(groupName) == null) {
			ArrayList<String> newList = new ArrayList<String>();
			newList.add(material);
			groups.put(groupName, newList);
		} else {
			if (!groups.get(groupName).contains(material)) {
				groups.get(groupName).add(material);
			}
		}
	}

	public void addToGroup(String groupName, Material material) {
		addToGroup(groupName, material.toString());
	}

	public void removeFromGroup(Material material) {
		removeFromGroup(material.toString());
	}

	public void removeFromGroup(String material) {
		for (String group : groups.keySet()) {
			if (groups.get(group).contains(material)) {
				groups.get(group).remove(material);
			}
		}
	}
}
