package com.Manxlatic.arbiter.commands.Ranks;

import com.Manxlatic.arbiter.Arbiter;
import com.Manxlatic.arbiter.Managers.DbManager;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.UUID;

public class SidebarCommand implements CommandExecutor {


    private final DbManager dbManager;

    private final Arbiter arbiter;

    public SidebarCommand(DbManager dbManager, Arbiter arbiter) {
        this.dbManager = dbManager;
        this.arbiter = arbiter;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(ChatColor.RED + "This command can only be used by a player.");
            return true;
        }

        Player player = (Player) sender;
        UUID playerId = player.getUniqueId();

        if (!dbManager.isScoreboardDisabled(playerId)) {
            // Disable sidebar
            dbManager.addDisabledScoreboard(playerId);
            arbiter.getNameTagManager().removeScoreboard(player.getScoreboard());
            sender.sendMessage(ChatColor.GREEN + "Scoreboard removed successfully.");

        } else {
            // Re-enable sidebar
            dbManager.removeDisabledScoreboard(playerId);
            arbiter.getNameTagManager().setNameTag(player);
            sender.sendMessage(ChatColor.GREEN + "Scoreboard added successfully.");

        }
        return true;
    }

    // Check if the sidebar is disabled for a player
    public boolean isSidebarDisabled(UUID playerId) {
        return dbManager.isScoreboardDisabled(playerId);
    }
}
