package com.Manxlatic.arbiter.commands;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class SpeedCommand implements CommandExecutor {
    @Override
    public boolean onCommand(CommandSender sender, Command command, String s, String[] args) {

        if (!(sender instanceof Player)) {
            System.err.println("This Command can only ber used by a player.");
        }

        Player player = (Player) sender;
        if (args.length != 1) {
            player.sendMessage("Usage: /speed <speed>");
        }


        if (Float.parseFloat(args[0]) > 10 || Float.parseFloat(args[0]) < -1) {
            player.sendMessage("Please use a value between -1 and 1");
        }


        player.setWalkSpeed(Float.parseFloat(args[0]));
        player.setFlySpeed(Float.parseFloat(args[0]));
        player.sendMessage(ChatColor.GREEN + "Speed set to" + args[0]);
        return false;
    }
}
