package com.Manxlatic.arbiter.commands;

import com.Manxlatic.arbiter.Arbiter;
import com.Manxlatic.arbiter.Managers.NameTagManager;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class FlyCommand implements CommandExecutor {

    private final Arbiter arbiter;

    public FlyCommand(Arbiter arbiter) {
        this.arbiter = arbiter;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String s, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("This command can only be used by a player.");
            return true;
        }


        Player player = (Player) sender;

        if (args.length != 0) {
            sender.sendMessage("Usage: /fly");
            return true;
        }

        NameTagManager nameTagManager = new NameTagManager(arbiter);



        if (!player.getAllowFlight()) {
            player.setAllowFlight(true);
            player.sendMessage(ChatColor.GREEN + "Flying enabled");
        } else {
            player.setAllowFlight(false);
            player.sendMessage(ChatColor.RED + "Flying disabled");
        }
        nameTagManager.setNameTag(player);
        return true;
    }

    public boolean canFly(Player player) {
        return player.getAllowFlight();
    }
}
