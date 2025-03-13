package com.Manxlatic.arbiter.commands;

import com.Manxlatic.arbiter.Managers.CustomInventoryBuilder;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

public class EditInventoryCommand implements CommandExecutor, Listener {
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        // Check if the sender is a player
        if (!(sender instanceof Player)) {
            sender.sendMessage("This command can only be used by a player.");
            return true;
        }

        Player commandSender = (Player) sender;

        // Ensure the command has an argument (target player's name)
        if (args.length < 1) {
            commandSender.sendMessage(ChatColor.RED + "Usage: /editinventory <player>");
            return true;
        }

        // Get the target player from the command argument
        String targetName = args[0];
        Player targetPlayer = Bukkit.getPlayer(targetName);

        // Check if the target player is online
        if (targetPlayer == null) {
            commandSender.sendMessage(ChatColor.RED + "Player " + targetName + " is not online or doesn't exist.");
            return true;
        }

        // Create a custom inventory to edit the target player's inventory
        Inventory customInventory = CustomInventoryBuilder.createPlayerInventoryToEdit(targetPlayer);

        // Open the custom inventory for the command sender
        commandSender.openInventory(customInventory);

        commandSender.sendMessage(ChatColor.GREEN + "You are now editing " + targetName + "'s inventory!");

        return true;
    }

    @EventHandler
    public void onInventoryClose(InventoryCloseEvent event) {
        // Check if this is a custom inventory (identified by its title)
        if (event.getView().getTitle().startsWith("Editing ")) {
            // Extract the player being edited from the inventory title
            String targetName = event.getView().getTitle().replace("Editing ", "").replace("'s Inventory", "");
            Player targetPlayer = Bukkit.getPlayer(targetName);

            // If the target player is offline or null, stop processing
            if (targetPlayer == null) {
                return;
            }

            // Get the custom inventory being edited
            Inventory customInventory = event.getInventory();

            // Sync the custom inventory back to the target player's real inventory
            ItemStack[] contents = new ItemStack[36]; // Main inventory (0–35)
            ItemStack[] armorContents = new ItemStack[4]; // Armor (boots->helmet)
            ItemStack mainHand = customInventory.getItem(50); // Main hand
            ItemStack offHand = customInventory.getItem(52); // Off-hand

            // Extract main inventory contents
            for (int i = 0; i < 36; i++) {
                contents[i] = customInventory.getItem(i);
            }

            // Extract armor contents
            armorContents[3] = customInventory.getItem(45); // Helmet
            armorContents[2] = customInventory.getItem(46); // Chestplate
            armorContents[1] = customInventory.getItem(47); // Leggings
            armorContents[0] = customInventory.getItem(48); // Boots

            // Update the target player's inventory
            targetPlayer.getInventory().setContents(contents);
            targetPlayer.getInventory().setArmorContents(armorContents);
            targetPlayer.getInventory().setItemInMainHand(mainHand);
            targetPlayer.getInventory().setItemInOffHand(offHand);

            // Send a confirmation message to the command sender (optional)
            Player sender = (Player) event.getPlayer();
            if (sender != null) {
                sender.sendMessage(ChatColor.GREEN + "Changes saved to " + targetName + "'s inventory.");
            }
        }
    }
}
