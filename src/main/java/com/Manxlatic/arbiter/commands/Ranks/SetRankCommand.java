package com.Manxlatic.arbiter.commands.Ranks;

import com.Manxlatic.arbiter.Arbiter;
import com.Manxlatic.arbiter.Ranks.Ranks;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class SetRankCommand implements CommandExecutor {
    private final Arbiter arbiter;

    public SetRankCommand(Arbiter arbiter) {
        this.arbiter = arbiter;
    }

    @Override
    public boolean onCommand(CommandSender commandSender, Command command, String label, String[] args) {

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
        for (Ranks ranks : Ranks.values()) {
            if (ranks.name().equalsIgnoreCase(args[1])) {
                if (arbiter.getDbManager().GetRank(target.getUniqueId()) == ranks) {
                    player.sendMessage(target.getName() + "'s rank is already " + ranks.getDisplay());
                    return false;
                }
                //remove tag
                arbiter.getNameTagManager().removeNameTag(target.getPlayer());
                //set rank to one specified
                arbiter.getDbManager().Setrank(false, target.getUniqueId(), ranks.name());
                player.sendMessage("Successfully Set " + target.getName() + "'s rank to " + ranks.getDisplay());

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
        /*if (!(sender instanceof Player)) {
            sender.sendMessage("This command can only be used by players.");
            return true;
        }

        Player player = (Player) sender;
        if (args.length != 2) {
            player.sendMessage("Usage: /setrank <player> <rank>");
            return true;
        }

        String targetPlayerName = args[0];
        String rankName = args[1].toUpperCase();

        Player targetPlayer = player.getServer().getPlayer(targetPlayerName);
        if (targetPlayer == null) {
            player.sendMessage("Player not found.");
            return true;
        }

        try {
            Ranks rank = Ranks.valueOf(rankName);
            ArrayList<String> permissions = rank.getPermissions();

            //reset the permissions for the user

            // Apply permissions to the target player
            for (String permission : permissions) {
                targetPlayer.addAttachment(main).setPermission(permission, true);
            }

            player.sendMessage("Assigned rank " + rankName + " to " + targetPlayerName);
            targetPlayer.sendMessage("You have been assigned the rank " + rankName);

        } catch (IllegalArgumentException e) {
            player.sendMessage("Invalid rank specified.");
        }

        return true;
    }
}

import com.testing.perms_ranks.PermsRanks;
import com.testing.perms_ranks.Ranks;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;


public class SetRankCommand implements CommandExecutor {

    private PermsRanks main;

    public SetRankCommand(PermsRanks main) {
        this.main = main;
    }
    @Override
    public boolean onCommand(CommandSender commandSender, Command command, String s, String[] args) {

        Player player = (Player) commandSender;

        if (!player.isOp()) {
            player.sendMessage(ChatColor.RED + "You do not have permission to use this command!");
            return false;
        }

        if (args.length == 0) { commandSender.sendMessage(ChatColor.RED + "Invalid parameters"); return false;}
        if (args.length >= 3) { commandSender.sendMessage(ChatColor.RED + "Invalid parameters"); return false; }
        if (args.length == 2) {

            if (Bukkit.getOfflinePlayer(args[0]) == null) {
                player.sendMessage(ChatColor.RED + "This player has never joined");
                return false;
            }

            OfflinePlayer target = Bukkit.getOfflinePlayer(args[0]);
            boolean foundRank = false;
            for (Ranks ranks : Ranks.values()) {
                if (ranks.name().equalsIgnoreCase(args[1])) {
                    if (main.getRankManager().getRank(target.getUniqueId()) == ranks) {
                        player.sendMessage(target.getName() + "'s rank is already " + ranks.getDisplay());
                        return false;
                    }

                    main.getNameTagManager().removeNameTag(target.getPlayer());
                    main.getRankManager().setRank(target.getUniqueId(), ranks );
                    player.sendMessage("Successfully Set " + target.getName() + "'s rank to " + ranks.getDisplay());

                    main.getNameTagManager().setNameTag(target.getPlayer());
                    main.getNameTagManager().newNameTag(target.getPlayer());

                    foundRank = true;
                    break;
                }

            }

            if (!foundRank) {
                player.sendMessage(ChatColor.RED + "Please provide a valid rank");
            }

            switch (args[1].toLowerCase()) {
                case "moderator":
                    if (main.getRankManager().getRank(target.getUniqueId()) == Ranks.MODERATOR) {
                        commandSender.sendMessage(target + "'s rank is already " + Ranks.MODERATOR.getDisplay());
                        return false;
                    }
                    main.getRankManager().setRank(target.getUniqueId(), Ranks.MODERATOR);
                    commandSender.sendMessage("Successfully Set " + target.getName() + "'s rank to " + Ranks.MODERATOR.getDisplay());
                    break;
                case "admin":
                    if (main.getRankManager().getRank(target.getUniqueId()) == Ranks.ADMIN) {
                        commandSender.sendMessage(target + "'s rank is already " + Ranks.ADMIN.getDisplay());
                        return false;
                    }
                    main.getRankManager().setRank(target.getUniqueId(), Ranks.ADMIN);
                    commandSender.sendMessage("Successfully Set " + target.getName() + "'s rank to " + Ranks.ADMIN.getDisplay());
                    break;
                case "owner":
                    if (main.getRankManager().getRank(target.getUniqueId()) == Ranks.OWNER) {
                        commandSender.sendMessage(target + "'s rank is already " + Ranks.OWNER.getDisplay());
                        return false;
                    }
                    main.getRankManager().setRank(target.getUniqueId(), Ranks.OWNER);
                    commandSender.sendMessage("Successfully Set " + target.getName() + "'s rank to " + Ranks.OWNER.getDisplay());
                    break;
                default:
                    if (main.getRankManager().getRank(target.getUniqueId()) == Ranks.PLAYER) {
                        commandSender.sendMessage(target + "'s rank is already " + Ranks.PLAYER.getDisplay());
                        return false;
                    }
                    main.getRankManager().setRank(target.getUniqueId(), Ranks.PLAYER);
                    commandSender.sendMessage("Successfully Set " + target.getName() + "'s rank to " + Ranks.PLAYER.getDisplay());
                    break;
            }



        }
        return false;
    }
}

 */
