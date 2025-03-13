package com.Manxlatic.arbiter.Managers;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class CustomInventoryBuilder {

    public static Inventory createPlayerInventoryView(Player player) {
        // Create a 9x6 inventory (54 slots)
        Inventory customInventory = Bukkit.createInventory(null, 54, player.getName() + "'s Inventory");


        ItemStack spacer = createPlaceholder();

        // Get the player's inventory data
        ItemStack[] armorContents = player.getInventory().getArmorContents();
        ItemStack mainHand = player.getInventory().getItemInMainHand();
        ItemStack offHand = player.getInventory().getItemInOffHand();
        ItemStack[] playerContents = player.getInventory().getContents();

        // 1. Place the armor/tools row at the very bottom (slots 45–53 in the custom inventory)
        customInventory.setItem(45, armorContents[3]); // Helmet
        customInventory.setItem(46, armorContents[2]); // Chestplate
        customInventory.setItem(47, armorContents[1]); // Leggings
        customInventory.setItem(48, armorContents[0]); // Boots
        customInventory.setItem(49, new ItemStack(spacer)); // Spacing
        customInventory.setItem(50, mainHand); // Main hand
        customInventory.setItem(51, new ItemStack(spacer)); // Spacing
        customInventory.setItem(52, offHand); // Off-hand
        customInventory.setItem(53, new ItemStack(spacer)); // Spacing

        // 2. Add a spacer row above the armor/tools row (slots 36–44)

        for (int i = 36; i < 45; i++) {
            customInventory.setItem(i, spacer);
        }

        // 3. Place the hotbar directly above the spacer (slots 27–35 in the custom inventory)
        for (int i = 0; i < 9; i++) {
            customInventory.setItem(27 + i, playerContents[i]); // Hotbar is slots 0–8 in the player inventory
        }

        // 4. Reorder and populate the main inventory rows (slots 0–26 in custom inventory)
        // Start with the bottom row of the player inventory (Row 2): Place it closest to the hotbar
        int customIndex = 18; // Start placing at 18 (just above the hotbar)
        for (int col = 0; col < 9; col++) { // Row 2
            int playerInventoryIndex = 9 + (2 * 9) + col; // Bottom row (Row 2)
            customInventory.setItem(customIndex++, playerContents[playerInventoryIndex]);
        }

        // Then the middle row of the player inventory (Row 1)
        customIndex = 9; // Start placing at 9 (above Row 2)
        for (int col = 0; col < 9; col++) { // Row 1
            int playerInventoryIndex = 9 + (1 * 9) + col; // Middle row (Row 1)
            customInventory.setItem(customIndex++, playerContents[playerInventoryIndex]);
        }

        // Finally, the top row of the player inventory (Row 0)
        customIndex = 0; // Start placing at 0 (topmost row)
        for (int col = 0; col < 9; col++) { // Row 0
            int playerInventoryIndex = 9 + (0 * 9) + col; // Top row (Row 0)
            customInventory.setItem(customIndex++, playerContents[playerInventoryIndex]);
        }

        return customInventory;
    }

    public static Inventory createPlayerInventoryToEdit(Player player) {
        // Create a 9x6 inventory (54 slots)
        Inventory customInventory = Bukkit.createInventory(null, 54, "Editing " + player.getName() + "'s Inventory");


        ItemStack spacer = createPlaceholder();

        // Get the player's inventory data
        ItemStack[] armorContents = player.getInventory().getArmorContents();
        ItemStack mainHand = player.getInventory().getItemInMainHand();
        ItemStack offHand = player.getInventory().getItemInOffHand();
        ItemStack[] playerContents = player.getInventory().getContents();

        // 1. Place the armor/tools row at the very bottom (slots 45–53 in the custom inventory)
        customInventory.setItem(45, armorContents[3]); // Helmet
        customInventory.setItem(46, armorContents[2]); // Chestplate
        customInventory.setItem(47, armorContents[1]); // Leggings
        customInventory.setItem(48, armorContents[0]); // Boots
        customInventory.setItem(49, new ItemStack(spacer)); // Spacing
        customInventory.setItem(50, mainHand); // Main hand
        customInventory.setItem(51, new ItemStack(spacer)); // Spacing
        customInventory.setItem(52, offHand); // Off-hand
        customInventory.setItem(53, new ItemStack(spacer)); // Spacing

        // 2. Add a spacer row above the armor/tools row (slots 36–44)

        for (int i = 36; i < 45; i++) {
            customInventory.setItem(i, spacer);
        }

        // 3. Place the hotbar directly above the spacer (slots 27–35 in the custom inventory)
        for (int i = 0; i < 9; i++) {
            customInventory.setItem(27 + i, playerContents[i]); // Hotbar is slots 0–8 in the player inventory
        }

        // 4. Reorder and populate the main inventory rows (slots 0–26 in custom inventory)
        // Start with the bottom row of the player inventory (Row 2): Place it closest to the hotbar
        int customIndex = 18; // Start placing at 18 (just above the hotbar)
        for (int col = 0; col < 9; col++) { // Row 2
            int playerInventoryIndex = 9 + (2 * 9) + col; // Bottom row (Row 2)
            customInventory.setItem(customIndex++, playerContents[playerInventoryIndex]);
        }

        // Then the middle row of the player inventory (Row 1)
        customIndex = 9; // Start placing at 9 (above Row 2)
        for (int col = 0; col < 9; col++) { // Row 1
            int playerInventoryIndex = 9 + (1 * 9) + col; // Middle row (Row 1)
            customInventory.setItem(customIndex++, playerContents[playerInventoryIndex]);
        }

        // Finally, the top row of the player inventory (Row 0)
        customIndex = 0; // Start placing at 0 (topmost row)
        for (int col = 0; col < 9; col++) { // Row 0
            int playerInventoryIndex = 9 + (0 * 9) + col; // Top row (Row 0)
            customInventory.setItem(customIndex++, playerContents[playerInventoryIndex]);
        }

        return customInventory;
    }

    private static ItemStack createPlaceholder() {
        // Create a dark gray stained glass block
        ItemStack placeholder = new ItemStack(Material.GRAY_STAINED_GLASS_PANE);
        ItemMeta meta = placeholder.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(" "); // Set an empty name to hide it
            placeholder.setItemMeta(meta);
        }
        return placeholder;
    }
}
