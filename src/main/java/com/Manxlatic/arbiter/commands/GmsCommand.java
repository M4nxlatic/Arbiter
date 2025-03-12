package com.Manxlatic.arbiter.commands;

import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class GmsCommand implements CommandExecutor {
    @Override
    public boolean onCommand(CommandSender sender, Command command, String s, String[] args) {
        if (!(sender instanceof Player)) {
            System.err.println("This Command can only ber used by a player.");
        }
        Player player = (Player) sender;
        player.setGameMode(GameMode.SURVIVAL);
        player.sendMessage(ChatColor.GREEN + "Gamemode changed to Survival");
        return false;
    }
}
