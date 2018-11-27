package me.Chestsort.main;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;

public class ChestGroupsData {
	ChestSort plugin;

	HashMap<String, ArrayList<Material>> groups = new HashMap<String, ArrayList<Material>>();

	// Files and FileConfigs
	public FileConfiguration networks;
	public File networksFile;

	public ChestGroupsData(ChestSort plugin) {
		this.plugin = plugin;
	}
	
	public void setup() {
		
	}

	public String getGroupName(Material material) {
		for (String group : groups.keySet()) {
			if (groups.get(group).contains(material)) {
				return group;
			}
		}

		return null;
	}

	public void addToGroup(String groupName, Material material) {
		if (groups.get(groupName) == null) {
			ArrayList<Material> newList = new ArrayList<Material>();
			newList.add(material);
			groups.put(groupName, newList);
		} else {
			if (!groups.get(groupName).contains(material)) {
				groups.get(groupName).add(material);
			}
		}
	}

	public void removeFromGroup(Material material) {
		for (String group : groups.keySet()) {
			if (groups.get(group).contains(material)) {
				groups.get(group).remove(material);
			}
		}
	}
}
