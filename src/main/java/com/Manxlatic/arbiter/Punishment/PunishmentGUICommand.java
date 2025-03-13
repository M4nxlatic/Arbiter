package com.Manxlatic.arbiter.Punishment;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;

public class PunishmentGUICommand implements CommandExecutor {
    @Override
    public boolean onCommand(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, @NotNull String[] strings) {

        if (!(commandSender instanceof Player)) {
            return false;
        }
        Player player = (Player) commandSender;

        Inventory GUI = Bukkit.createInventory(player, 54, ChatColor.DARK_RED.toString() + ChatColor.BOLD + "Punishments");

        ItemStack close = new ItemStack(Material.BARRIER);
        ItemMeta closeMeta = close.getItemMeta();
        closeMeta.setDisplayName("");
        closeMeta.setDisplayName("Close GUI");
        close.setItemMeta(closeMeta);

        GUI.setItem(45, close);


        ItemStack filler = new ItemStack(Material.GRAY_STAINED_GLASS_PANE);
        ItemMeta meta = filler.getItemMeta();
        meta.setDisplayName("  ");
        filler.setItemMeta(meta);
        for (int i : new int[]{0,1,2,3,4,5,6,7,8,17,26,35,44,53,52,51,50,49,48,47,46,36,27,18,9,0}) {
            GUI.setItem(i, filler);
        }


        player.openInventory(GUI);
        return false;
    }
}
