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

import java.text.SimpleDateFormat;
import java.time.Duration;
import java.time.Instant;
import java.time.format.DateTimeFormatter;
import java.util.Calendar;
import java.util.Date;
import java.util.UUID;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class TempBanCommand implements CommandExecutor {
    private final JDA jda;

    private final Arbiter arbiter;

    private final DbManager dbManager;

    public TempBanCommand(JDA jda, Arbiter arbiter, DbManager dbManager) {
        this.jda = jda;
        this.arbiter = arbiter;
        this.dbManager = dbManager;
    }


    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        ConfigManager configManager = arbiter.getConfigManager();

        boolean isSilent = false; // Track if the -s flag is used
        String targetName = null; // Store the player's name
        String durationArg = null; // Store the duration argument
        StringBuilder reason = new StringBuilder(); // Store the reason for the ban

        // Ensure JDA is properly initialized
        if (jda == null) {
            System.err.println("JDA IS NULL");
            return false;
        }

        // Validate minimum number of arguments
        if (args.length < 3) {
            sender.sendMessage(ChatColor.RED + "Invalid parameter count. Usage: /tempban [-s] <player> <duration> <reason>");
            return false;
        }

        try {
            // Parse arguments (supporting flexible placement of -s)
            for (String arg : args) {
                if (arg.equalsIgnoreCase("-s")) {
                    isSilent = true; // Enable silent mode
                } else if (targetName == null) {
                    targetName = arg; // Set target player's name
                } else if (durationArg == null) {
                    durationArg = arg; // Set duration argument
                } else {
                    reason.append(arg).append(" "); // Append remaining arguments to the reason
                }
            }

            // Validate parsed arguments
            if (targetName == null || durationArg == null || reason.length() == 0) {
                sender.sendMessage(ChatColor.RED + "Invalid parameter count. Usage: /tempban [-s] <player> <duration> <reason>");
                return false;
            }

            // Determine if the command is executed by a Player or Console
            String executorName = (sender instanceof Player) ? sender.getName() : "Console"; // Use "Console" for the console sender
            UUID executorUUID = (sender instanceof Player) ? ((Player) sender).getUniqueId() : null; // Get UUID if sender is a player

            // Parse duration into an expiration time
            Instant expirationTime = Instant.now().plus(parse(durationArg));

            // Fetch the target player (online or offline)
            OfflinePlayer target = Bukkit.getOfflinePlayer(targetName);

            if (target == null || target.getName() == null) {
                sender.sendMessage(ChatColor.RED + "Player " + targetName + " does not exist.");
                return false;
            }

            // Add the ban to the server ban list
            BanList banList = Bukkit.getBanList(BanList.Type.NAME);
            banList.addBan(target.getName(), reason.toString().trim(), Date.from(expirationTime), executorName);

            // If the player is currently online, kick them with a ban message
            if (target.isOnline()) {
                Player onlineTarget = (Player) target;
                onlineTarget.kickPlayer(ChatColor.RED + "You have been temporarily banned.\n" +
                        ChatColor.GRAY + "Expires: " + expirationTime.toString() + "\n" +
                        ChatColor.GRAY + "Reason: " + reason.toString().trim());
            }

            // Record the tempban in the database
            if (executorUUID != null) {
                dbManager.recordGameTempBan(executorUUID.toString(), expirationTime);
            }

            // Notify the executor
            sender.sendMessage(ChatColor.RED + "Player " + targetName + " has been temporarily banned. Reason: " + reason.toString().trim() + ChatColor.WHITE + " (Silent)");

            // Broadcast ban message unless silent mode is enabled
            if (!isSilent) {
                Bukkit.broadcastMessage(
                        ChatColor.RED + "Player " + targetName + " has been temporarily banned by "
                                + executorName + ". Reason: " + reason.toString().trim()
                );
            }

            // Send ban notification to Discord
            Guild guild = jda.getGuildById(configManager.getProperty("server_id"));
            TextChannel textChannel = guild.getTextChannelById(configManager.getProperty("staff_logging_channel_id"));
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm");
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
            String imageUrl = "https://minotar.net/avatar/" + target.getName() + "/60";

            EmbedBuilder embedBuilder = new EmbedBuilder();
            embedBuilder.setTitle("**" + target.getName() + "┃" + "Has Been TEMPORARILY BANNED" + "**");
            embedBuilder.setDescription(
                    "\n \n" +
                            "**Target**\n `" + target.getName() + "`\n" +
                            "**Executor**\n `" + executorName + "`\n" +
                            "**Expiration**\n `" + dateFormat.format(Date.from(expirationTime)) + "`\n" +
                            "**Reason**\n `" + reason.toString().trim() + "`\n" +
                            "**Date**\n `" + java.time.LocalDateTime.now().format(formatter) + "`"
            );
            embedBuilder.setColor(0xFF0000);
            embedBuilder.setThumbnail(imageUrl);

            MessageEmbed embed = embedBuilder.build();

            if (jda != null && jda.getStatus() == JDA.Status.CONNECTED) {
                if (textChannel != null) {
                    textChannel.sendMessageEmbeds(embed).queue(
                            success -> Logger.getLogger("TempBanLogger").info("Discord tempban notification sent successfully."),
                            failure -> Logger.getLogger("TempBanLogger").warning("Failed to send Discord message: " + failure.getMessage())
                    );
                } else {
                    Logger.getLogger("TempBanLogger").warning("TextChannel is null. Check your channel ID.");
                }
            } else {
                Logger.getLogger("TempBanLogger").warning("JDA is not properly initialized or not connected.");
            }

            return true; // Command executed successfully

        } catch (Exception e) {
            sender.sendMessage(ChatColor.RED + "An error occurred while executing the /tempban command. Please check the logs.");
            Logger.getLogger("TempBanLogger").severe("An error occurred while executing the /tempban command: " + e.getMessage());
            e.printStackTrace();
            return false; // Command failed
        }
    }

    private final Pattern DURATION_PATTERN = Pattern.compile("(\\d+)([smhd])");

    public Duration parse(String durationString) {
        Matcher matcher = DURATION_PATTERN.matcher(durationString.toLowerCase());
        Duration duration = Duration.ZERO;

        while (matcher.find()) {
            long amount = Long.parseLong(matcher.group(1));
            char unit = matcher.group(2).charAt(0);

            switch (unit) {
                case 's':
                    duration = duration.plusSeconds(amount);
                    break;
                case 'm':
                    duration = duration.plusMinutes(amount);
                    break;
                case 'h':
                    duration = duration.plusHours(amount);
                    break;
                case 'd':
                    duration = duration.plusDays(amount);
                    break;
                default:
                    throw new IllegalArgumentException("Unknown time unit: " + unit);
            }
        }

        return duration;
    }
}