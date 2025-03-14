package com.Manxlatic.arbiter.commands.Punishment;


import com.Manxlatic.arbiter.Arbiter;
import com.Manxlatic.arbiter.Managers.ConfigManager;
import com.Manxlatic.arbiter.Managers.DbManager;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import org.bukkit.BanList;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.UUID;
import java.util.logging.Logger;

public class unbanCommand implements CommandExecutor {

    private final JDA jda;

    private final Arbiter arbiter;
    
    private final DbManager dbManager;

    public unbanCommand(JDA jda, Arbiter arbiter, DbManager dbManager) {
        this.jda = jda;
        this.arbiter = arbiter;
        this.dbManager = dbManager;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String s, String[] args) {
        ConfigManager configManager = arbiter.getConfigManager();
        boolean isSilent = false;

        String targetName = null;

        // Determine if the sender is a Player or the Console
        String senderName = (sender instanceof Player) ? sender.getName() : "Console";
        Player player = sender instanceof Player ? (Player) sender : null; // Cast only if sender is a player

        // check for -s
        //if you have a -s then array count -1 must = 1
        //else
        //array count must = 1

        for (String arg : args) {
            if (arg.equalsIgnoreCase("-s")) {
                isSilent = true;
            } else if (targetName == null) {
                targetName = arg;
            } else {
                sender.sendMessage(ChatColor.RED + "Invalid parameter count. Usage: /unban [-s] <player>");
                return false;
            }
        }

// Validate input after loop
        if (targetName == null) {
            sender.sendMessage(ChatColor.RED + "Invalid parameter count. Usage: /unban [-s] <player>");
            return false;
        }

        /*if (args.length == 1)
        {
            //==1 no -s
        }
        else if (args) //contains -s)
        {
            //do -s stuff
        }
        else
        {
            //fail
        }


        if (Arrays.stream(args).anyMatch(arg -> arg.equalsIgnoreCase("-s")))
        {
            //we have a -s in the array


        }
        // Check parameter count
*/


        // Get the target player or offline player UUID
        OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(targetName); // Use OfflinePlayer for both online and offline players
        var targetUUID = offlinePlayer.getUniqueId();

        BanList banList = Bukkit.getBanList(BanList.Type.NAME);

        // Handle removing bans from the database
        if (dbManager.getBans().containsKey(targetUUID.toString())) {
            String banType = dbManager.getBans().get(targetUUID.toString());

            if ("BAN".equals(banType)) {
                dbManager.removeGameBans(targetUUID.toString());
            } else if ("TEMPBAN".equals(banType)) {
                dbManager.removeGameTempBans(targetUUID.toString());
            }

            if (offlinePlayer.getName() != null) {
                banList.pardon(offlinePlayer.getName());
                sender.sendMessage(ChatColor.LIGHT_PURPLE + targetName + ChatColor.WHITE + " has been " + ChatColor.GREEN + ChatColor.BOLD + "unbanned");
            } else {
                sender.sendMessage(ChatColor.RED + "Unable to unban the specified player.");
                return true;
            }

            sender.sendMessage(ChatColor.GREEN + "Player " + targetName + " has been unbanned."
                    + (isSilent ? " (Silent)" : ""));

            if (!isSilent) {
                Bukkit.broadcastMessage(ChatColor.GREEN + "Player " + targetName + " has been banned.");
            }

            // Logging to Discord
            Guild guild = jda.getGuildById(configManager.getProperty("server_id"));
            TextChannel textChannel = guild.getTextChannelById(configManager.getProperty("staff_logging_channel_id"));
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
            String imageUrl = "https://minotar.net/avatar/" + targetName + "/50";

            EmbedBuilder embedBuilder = new EmbedBuilder();
            embedBuilder.setTitle("**" + targetName + "┃" + "Has Been UNBANNED" + "**");
            embedBuilder.setDescription(
                    "\n \n " +
                            "**Target**\n `" + targetName + "`\n**Executor**\n`" + senderName + "`\n**Date**\n`" +
                            java.time.LocalDateTime.now().format(formatter) + "`"
            );
            embedBuilder.setColor(0x00FF00);
            embedBuilder.setThumbnail(imageUrl);

            MessageEmbed embed = embedBuilder.build();

            if (jda != null && jda.getStatus() == JDA.Status.CONNECTED) {
                if (textChannel != null) {
                    textChannel.sendMessageEmbeds(embed).queue(
                            success -> Logger.getLogger("test2").info("Message sent successfully!"),
                            failure -> Logger.getLogger("test").warning("Failed to send message: " + failure.getMessage())
                    );
                } else {
                    Logger.getLogger("test3").warning("TextChannel is null. Check your channel ID.");
                }
            } else {
                Logger.getLogger("test4").warning("JDA is not properly initialized or not connected.");
            }

            return true;
        } else {
            sender.sendMessage(ChatColor.RED + "This player is not banned.");
            return true;
        }

        // Pardon the player from the BanList

    }
}



