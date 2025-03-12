package com.Manxlatic.arbiter.commands;

import com.Manxlatic.arbiter.Arbiter;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class UnFreezeCommand implements CommandExecutor {

    private Arbiter arbiter;

    public UnFreezeCommand(Arbiter arbiter) {
        this.arbiter = arbiter;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String s, String[] args) {
        if (args.length != 1) {
            sender.sendMessage("Usage: /unfreeze <player>");
        }
        Player player = (Player) sender;
        player.sendMessage(ChatColor.GREEN + "Player has been frozen");
        arbiter.getFrozenPlayers().add(player);
        return false;
    }
}
