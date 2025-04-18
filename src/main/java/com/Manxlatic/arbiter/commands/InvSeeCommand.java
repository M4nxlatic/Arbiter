package com.Manxlatic.arbiter.commands;

import com.Manxlatic.arbiter.Managers.CustomInventoryBuilder;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;

public class InvSeeCommand implements CommandExecutor, Listener {

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("This command can only be used by a player.");
            return true;
        }

        if (args.length != 1) {
            sender.sendMessage("Usage: /invsee <player>");
            return true;
        }

        Player player = (Player) sender;

        Player target = Bukkit.getPlayer(args[0]);

        if (target == null) {
            sender.sendMessage("Player not found.");
        }


        // Create the custom inventory
        Inventory customInventory = CustomInventoryBuilder.createPlayerInventoryView(target);

        // Open the custom inventory for the player
        player.openInventory(customInventory);

        return true;
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (event.getView().getTitle().endsWith("'s Inventory") && !event.getView().getTitle().startsWith("Editing")) { // Custom inventory check
            event.setCancelled(true); // Cancel clicks in this inventory
        }
    }
}

