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
            return false;
        }
        Player player = (Player) sender;
        Player target = sender.getServer().getPlayer(args[0]);

        if (!arbiter.getFrozenPlayers().contains(target.getUniqueId())) {
            player.sendMessage(ChatColor.RED  + "Player is not frozen");
            return false;
        }

        player.sendMessage(ChatColor.GREEN + "Player has been unfrozen");
        arbiter.getFrozenPlayers().remove(target.getUniqueId());
        return true;
    }
}
