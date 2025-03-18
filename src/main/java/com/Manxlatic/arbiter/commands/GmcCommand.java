package com.Manxlatic.arbiter.commands;

import com.Manxlatic.arbiter.Arbiter;
import com.Manxlatic.arbiter.Managers.NameTagManager;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class GmcCommand implements CommandExecutor {

    private final Arbiter arbiter;

    public GmcCommand(Arbiter arbiter) {
        this.arbiter = arbiter;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String s, String[] args) {
        if (!(sender instanceof Player)) {
            System.err.println("This Command can only ber used by a player.");
        }
        NameTagManager nameTagManager = new NameTagManager(arbiter);
        Player player = (Player) sender;
        player.setGameMode(GameMode.CREATIVE);
        nameTagManager.setNameTag(player);
        player.sendMessage(ChatColor.GREEN + "Gamemode changed to Creative");
        return false;
    }

}
