package com.Manxlatic.arbiter.commands;

import com.Manxlatic.arbiter.Arbiter;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class FreezeCommand implements CommandExecutor, Listener {

    private Arbiter arbiter;

    public FreezeCommand(Arbiter arbiter) {
        this.arbiter = arbiter;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String s, String[] args) {
        if (args.length != 1) {
            sender.sendMessage("Usage: /freeze <player>");
            return false;
        }
        Player player = (Player) sender;
        player.sendMessage(ChatColor.GREEN + "Player has been frozen");
        arbiter.getFrozenPlayers().add(player.getUniqueId());
        System.out.println(arbiter.getFrozenPlayers());
        return true;
    }

    @EventHandler
    public void onPlayerMove(PlayerMoveEvent event) {
        if (arbiter.getFrozenPlayers().contains(event.getPlayer().getUniqueId())) {
            event.setCancelled(true);
        }
    }
}
