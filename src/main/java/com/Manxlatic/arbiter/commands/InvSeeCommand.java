package com.Manxlatic.arbiter.commands;

import com.Manxlatic.arbiter.CustomInventoryBuilder;
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

        Player player = (Player) sender;

        // Create the custom inventory
        Inventory customInventory = CustomInventoryBuilder.createPlayerInventoryView(player);

        // Open the custom inventory for the player
        player.openInventory(customInventory);

        return true;
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (event.getView().getTitle().endsWith("'s Inventory")) { // Custom inventory check
            event.setCancelled(true); // Cancel clicks in this inventory
        }
    }
}

