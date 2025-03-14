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
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;

import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.UUID;
import java.util.logging.Logger;

public class MuteCommand implements CommandExecutor, Listener {

    private final Arbiter arbiter;

    private final DbManager dbManager;

    private final JDA jda;

    public MuteCommand(Arbiter arbiter, DbManager dbManager, JDA jda) {
        this.arbiter = arbiter;
        this.dbManager = dbManager;
        this.jda = jda;
    }


    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        ConfigManager configManager = arbiter.getConfigManager();
        boolean isSilent = false; // To track whether the `-s` flag is used
        String targetName = null; // To store the player's name
        String executorName = (sender instanceof Player) ? sender.getName() : "Console"; // Determine the executor's name

        try {
            // Parse arguments to find `-s` and the target player's name
            for (String arg : args) {
                if (arg.equalsIgnoreCase("-s")) {
                    isSilent = true; // Set silent mode if "-s" is found
                } else if (targetName == null) {
                    targetName = arg; // Assign the first non-flag argument as the target name
                } else {
                    // Invalid usage
                    sender.sendMessage(ChatColor.RED + "Invalid parameter count. Usage: /mute [-s] <player> <reason>");
                    return false;
                }
            }

            // Check if the target player name is provided
            if (targetName == null) {
                sender.sendMessage(ChatColor.RED + "Invalid Usage! /mute [-s] <player> <reason>");
                return false;
            }

            // Check if there's enough arguments for the reason
            if ((args.length < 2 && !isSilent) || args.length < 1) {
                sender.sendMessage(ChatColor.RED + "Invalid Usage! /mute [-s] <player> <reason>");
                return false;
            }

            // Prepare the reason string (skipping already parsed `-s` and player name)
            StringBuilder reason = new StringBuilder();
            for (int i = 1; i < args.length; i++) {
                // Skip "-s" (if present) and target name
                if (args[i].equalsIgnoreCase("-s") || args[i].equalsIgnoreCase(targetName)) {
                    continue;
                }
                reason.append(args[i]).append(" ");
            }
            String reasonText = reason.toString().trim();

            // Attempt to get the target player (online or offline)
            OfflinePlayer target = Bukkit.getOfflinePlayer(targetName);
            if (target == null || target.getName() == null) {
                sender.sendMessage(ChatColor.RED + "Player not found: " + targetName);
                return false;
            }

            // Record the mute in the database as permanent
            Instant duration = Instant.MAX; // Permanent mute
            dbManager.recordGameMute(target.getUniqueId().toString(), duration);

            // Notify the executor about the mute
            sender.sendMessage(ChatColor.RED + "Player " + target.getName() + " has been permanently MUTED. Reason: " + reasonText);

            // Broadcast the mute message if not in silent mode
            if (!isSilent) {
                Bukkit.broadcastMessage(ChatColor.RED + "Player " + target.getName() + " has been muted by "
                        + executorName + ". Reason: " + reasonText);
            }

            // Log the mute to Discord as an embed message
            Guild guild = jda.getGuildById(configManager.getProperty("server_id"));
            TextChannel textChannel = guild.getTextChannelById(configManager.getProperty("staff_logging_channel_id"));
            if (guild != null && textChannel != null) {
                String imageUrl = "https://minotar.net/avatar/" + target.getName() + "/60";
                UUID muteUUID = UUID.randomUUID();

                EmbedBuilder embedBuilder = new EmbedBuilder();
                embedBuilder.setTitle("**" + target.getName() + "┃" + "Has Been Permanently MUTED" + "**");
                embedBuilder.setDescription("\n \n"
                        + "**Target**\n `" + target.getName() + "`\n"
                        + "**Executor**\n`" + executorName + "`\n"
                        + "**Expiration**\nNever\n"
                        + "**Reason**\n`" + reasonText + "`\n"
                        + "**Date**\n`" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")) + "`\n \n"
                        + muteUUID);
                embedBuilder.setColor(0xFDDA0D);
                embedBuilder.setThumbnail(imageUrl);

                MessageEmbed embed = embedBuilder.build();
                if (jda.getStatus() == JDA.Status.CONNECTED) {
                    textChannel.sendMessageEmbeds(embed).queue(
                            success -> Logger.getLogger("MuteLogger").info("Discord mute notification sent successfully."),
                            failure -> Logger.getLogger("MuteLogger").warning("Failed to send message: " + failure.getMessage())
                    );
                }
            } else {
                Logger.getLogger("MuteLogger").warning("Guild or TextChannel is not properly configured.");
            }

            return true; // Command executed successfully
        } catch (Exception e) {
            sender.sendMessage(ChatColor.RED + "An error occurred while executing the /mute command. Please check the logs.");
            Logger.getLogger("MuteLogger").severe("An error occurred while executing the /mute command: " + e.getMessage());
            e.printStackTrace();
            return false; // Command failed
        }
    }
}