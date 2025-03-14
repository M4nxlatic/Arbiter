package com.Manxlatic.arbiter.commands.Punishment;

import com.Manxlatic.arbiter.Arbiter;
import com.Manxlatic.arbiter.Managers.ConfigManager;
import com.Manxlatic.arbiter.Managers.DbManager;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;

import java.time.format.DateTimeFormatter;
import java.util.UUID;
import java.util.logging.Logger;

public class unmuteCommand implements CommandExecutor {

    private final JDA jda;

    private final DbManager dbManager;

    private final Arbiter arbiter;


    public unmuteCommand(JDA jda, DbManager dbManager, Arbiter arbiter) {
        this.jda = jda;
        this.dbManager = dbManager;
        this.arbiter = arbiter;
    }


    @Override
    public boolean onCommand(CommandSender sender, Command command, String s, String[] args) {
        ConfigManager configManager = arbiter.getConfigManager();
        boolean isSilent = false; // To track if `-s` flag is used
        String targetName = null; // To store the player's name

        // Determine if the sender is a Player or the Console
        String senderName = (sender instanceof Player) ? sender.getName() : "Console";
        Player player = sender instanceof Player ? (Player) sender : null;

        // Parse arguments to check for `-s` and the player's name
        for (String arg : args) {
            if (arg.equalsIgnoreCase("-s")) {
                isSilent = true; // Enable silent mode
            } else if (targetName == null) {
                targetName = arg; // Assign the player's name
            } else {
                sender.sendMessage(ChatColor.RED + "Invalid parameter count. Usage: /unmute [-s] <player>");
                return false;
            }
        }

        // Validate input after loop
        if (targetName == null) {
            sender.sendMessage(ChatColor.RED + "Invalid parameter count. Usage: /unmute [-s] <player>");
            return false;
        }

        // Check if the target player exists (online/offline)
        OfflinePlayer target = Bukkit.getOfflinePlayer(targetName); // Get target as OfflinePlayer
        if (target == null || target.getName() == null) {
            sender.sendMessage(ChatColor.RED + "Player not found or doesn't exist.");
            return false;
        }

        // Retrieve the target UUID
        String targetUUID = target.getUniqueId().toString();
        if (dbManager.getMutes().containsKey(targetUUID)) {
            // Check mute type (PERMANENT MUTE or TEMPORARY MUTE) and remove it
            String muteType = dbManager.getMutes().get(targetUUID);
            if ("MUTE".equals(muteType)) {
                dbManager.removeGameMutes(targetUUID);
            } else if ("TEMPMUTE".equals(muteType)) {
                dbManager.removeGameTempMutes(targetUUID);
            }

            // Send feedback messages
            if (target.getName() != null) {
                sender.sendMessage(ChatColor.LIGHT_PURPLE + target.getName() + ChatColor.WHITE + " has been " + ChatColor.GREEN + ChatColor.BOLD + "unmuted.");
            } else {
                sender.sendMessage(ChatColor.RED + "Unable to unmute the specified player.");
                return true;
            }

            sender.sendMessage(ChatColor.GREEN + "Player " + targetName + " has been unmuted."
                    + (isSilent ? " (Silent)" : ""));

            // Broadcast the unmute message (if not silent)
            if (!isSilent) {
                Bukkit.broadcastMessage(ChatColor.LIGHT_PURPLE + targetName + ChatColor.WHITE + " was unmuted by " + senderName);
            }

            // Logging to Discord
            Guild guild = jda.getGuildById(configManager.getProperty("server_id"));
            TextChannel textChannel = guild.getTextChannelById(configManager.getProperty("staff_logging_channel_id"));
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
            String imageUrl = "https://minotar.net/avatar/" + target.getName() + "/50";

            EmbedBuilder embedBuilder = new EmbedBuilder();
            embedBuilder.setTitle("**" + targetName + "┃" + "Has Been UNMUTED" + "**");
            embedBuilder.setDescription(
                    "\n \n " +
                            "**Target**\n `" + targetName + "`\n" +
                            "**Executor**\n`" + senderName + "`\n" +
                            "**Date**\n`" + java.time.LocalDateTime.now().format(formatter) + "`");
            embedBuilder.setColor(0x00FF00);
            embedBuilder.setThumbnail(imageUrl);

            MessageEmbed embed = embedBuilder.build();

            // Try to send the embed to Discord
            if (jda != null && jda.getStatus() == JDA.Status.CONNECTED) {
                if (textChannel != null) {
                    textChannel.sendMessageEmbeds(embed).queue(
                            success -> Logger.getLogger("DiscordLogger").info("Message sent successfully!"),
                            failure -> Logger.getLogger("DiscordLogger").warning("Failed to send message: " + failure.getMessage())
                    );
                } else {
                    Logger.getLogger("DiscordLogger").warning("TextChannel is null. Check your channel ID.");
                }
            } else {
                Logger.getLogger("DiscordLogger").warning("JDA is not properly initialized or not connected.");
            }

            return true; // Command successfully executed
        } else {
            // If the player is not muted
            sender.sendMessage(ChatColor.RED + "This player is not muted.");
            return true;
        }
    }

}