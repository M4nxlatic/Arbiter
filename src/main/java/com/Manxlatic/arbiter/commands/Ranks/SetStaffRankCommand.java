package com.Manxlatic.arbiter.commands.Ranks;

import com.Manxlatic.arbiter.Arbiter;
import com.Manxlatic.arbiter.Ranks.StaffRanks;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class SetStaffRankCommand implements CommandExecutor {

    private Arbiter arbiter;

    public SetStaffRankCommand(Arbiter arbiter) {
        this.arbiter = arbiter;
    }

    @Override
    public boolean onCommand(CommandSender commandSender, Command command, String s, String[] args) {
        //casts player to sender
        Player player = (Player) commandSender;

        //checks is player is OP
        if (!player.isOp()) {
            player.sendMessage(ChatColor.RED + "You do not have permission to use this command!");
            return false;
        }

        //checks parameters
        if (!(args.length == 2)) {
            player.sendMessage("invalid parameters");
        }

        //checks if player exists
        if (Bukkit.getOfflinePlayer(args[0]) == null) {
            player.sendMessage(ChatColor.RED + "This player has never joined");
            return false;
        }

        OfflinePlayer target = Bukkit.getOfflinePlayer(args[0]);
        boolean foundRank = false;
        for (StaffRanks staffRanks : StaffRanks.values()) {
            if (staffRanks.name().equalsIgnoreCase(args[1])) {
                /*
                if (staffRanks == StaffRanks.PLAYER) {
                    if (!(arbiter.getDbManager().GetStaffRank(target.getUniqueId()) == StaffRanks.PLAYER)) {
                        arbiter.getDbManager().RemoveSetupPlayers(target.getUniqueId());
                        //arbiter.getDbManager().RemoveVerified(target.getUniqueId());
                    }
                } */

                if (arbiter.getDbManager().GetStaffRank(target.getUniqueId()) == staffRanks) {
                    player.sendMessage(target.getName() + "'s rank is already " + staffRanks.getDisplay());
                    return false;
                }
                //remove tag
                arbiter.getNameTagManager().removeNameTag(target.getPlayer());
                //set rank to one specified
                arbiter.getDbManager().Setrank(true, target.getUniqueId(), staffRanks.name());
                player.sendMessage("Successfully Set " + target.getName() + "'s rank to " + staffRanks.getDisplay());

                //sets tag
                arbiter.getNameTagManager().setNameTag(target.getPlayer());
                arbiter.getNameTagManager().newNameTag(target.getPlayer());

                foundRank = true;
                break;
            }
        }

        if (!foundRank) {
            player.sendMessage(ChatColor.RED + "Please provide a valid rank");
        }

        return false;
    }
}
