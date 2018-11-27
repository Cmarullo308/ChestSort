package me.Chestsort.main;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;

public class CommandHandler {

	public boolean command(CommandSender sender, Command command, String label, String[] args) {

		switch (args[0].toLowerCase()) {
		case "test":
			testCommand(sender, args);
			break;
		default:
			break;
		}

		return true;
	}

	private void testCommand(CommandSender sender, String[] args) {
		
	}

}
