package com.Manxlatic.arbiter.commands.Ranks;


import com.Manxlatic.arbiter.Managers.ColourManager;
import com.Manxlatic.arbiter.Ranks.Ranks;
import com.Manxlatic.arbiter.Ranks.StaffRanks;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class RanksCommand implements CommandExecutor, Listener {

    private final Map<UUID, String[]> selectedColors = new HashMap<>();

    @Override
    public boolean onCommand(CommandSender sender, Command command, String s, String[] args) {

        if (!(sender instanceof Player)) {
            System.out.println(ChatColor.RED + "This can only be used by a player!");
            return false;
        }
        Player player = (Player) sender;

        String message = ColourManager.replaceFormat("&#FF10FFR&#C517F7a&#8A1EEEn&#5024E6k&#152BDDs");

        Inventory GUI = Bukkit.createInventory(player, 54, message);

        ItemStack close = new ItemStack(Material.BARRIER);
        ItemMeta closeMeta = close.getItemMeta();
        closeMeta.setDisplayName("");
        closeMeta.setDisplayName("Close GUI");
        close.setItemMeta(closeMeta);

        GUI.setItem(45, close);

        ItemStack red = new ItemStack(Material.RED_CONCRETE);
        ItemMeta redMeta = red.getItemMeta();
        redMeta.setDisplayName("");
        redMeta.setDisplayName("Close GUI");
        red.setItemMeta(redMeta);

        GUI.setItem(10, red);

        ItemStack green = new ItemStack(Material.LIME_CONCRETE);
        ItemMeta greenMeta = green.getItemMeta();
        greenMeta.setDisplayName("");
        greenMeta.setDisplayName("Close GUI");
        green.setItemMeta(greenMeta);

        GUI.setItem(11, green);

        ItemStack blue = new ItemStack(Material.BLUE_CONCRETE);
        ItemMeta blueMeta = blue.getItemMeta();
        blueMeta.setDisplayName("");
        blueMeta.setDisplayName("Close GUI");
        blue.setItemMeta(blueMeta);

        GUI.setItem(12, blue);

        ItemStack filler = new ItemStack(Material.GRAY_STAINED_GLASS_PANE);
        ItemMeta meta = filler.getItemMeta();
        meta.setDisplayName("  ");
        filler.setItemMeta(meta);
        for (int i : new int[]{0,1,2,3,4,5,6,7,8,17,26,35,44,53,52,51,50,49,48,47,46,36,27,18,9,0}) {
            GUI.setItem(i, filler);
        }


        player.openInventory(GUI);

        for (Ranks ranks : Ranks.values()){
            player.sendMessage(ranks.getDisplay());
        }
        for (StaffRanks staffRanks : StaffRanks.values()) {
            player.sendMessage(staffRanks.getDisplay());
        }

        return false;
    }

    @EventHandler
    public void onClick(InventoryClickEvent e) {
        if (e.getCurrentItem() == null || e.getView().getTitle() == null) return;

        Player player = (Player) e.getWhoClicked();
        UUID playerId = player.getUniqueId();

        String[] colors = selectedColors.getOrDefault(playerId, new String[]{"", ""});

        switch (e.getRawSlot()) {
            case 10:
                if (!colors[0].isEmpty()) {
                    colors[1] = "#FF0000"; // Red
                } else {
                    colors[0] = "#FF0000";
                }
                break;
            case 11:
                if (!colors[0].isEmpty()) {
                    colors[1] = "#00FF00"; // Green
                } else {
                    colors[0] = "#00FF00";
                }
                break;
            case 12:
                if (!colors[0].isEmpty()) {
                    colors[1] = "#0000FF"; // Blue
                } else {
                    colors[0] = "#0000FF";
                }
                break;
            case 45:
                player.closeInventory();
                break;
            default:
                return;
        }

        selectedColors.put(playerId, colors);

        Bukkit.broadcastMessage(ColourManager.BuildColorString(colors[0], colors[1], player.getName()));
        player.sendMessage(ChatColor.GREEN + "Successfully updated your color selection!");
    }
}
