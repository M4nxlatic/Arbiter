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
import java.time.Instant;
import java.time.format.DateTimeFormatter;
import java.util.UUID;
import java.util.logging.Logger;

public class BanCommand implements CommandExecutor {

    //  /ban <player> <reason>
    private final JDA jda;

    private final Arbiter arbiter;

    private final DbManager dbManager;

    public BanCommand(JDA jda, Arbiter arbiter, DbManager dbManager) {
        this.jda = jda;
        this.arbiter = arbiter;
        this.dbManager = dbManager;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        ConfigManager configManager = arbiter.getConfigManager();
        boolean isSilent = false; // To track whether the `-s` flag is used
        String targetName = null; // To store the player name

        // Check if the sender is a player
        if (sender instanceof Player) {
            Player player = (Player) sender;

            // Parse arguments to find `-s` and the target player's name
            for (String arg : args) {
                if (arg.equalsIgnoreCase("-s")) {
                    isSilent = true; // Set silent mode if "-s" is found
                } else if (targetName == null) {
                    targetName = arg; // Assign the first non-flag argument as the target name
                } else {
                    // Invalid usage
                    sender.sendMessage(ChatColor.RED + "Invalid parameter count. Usage: /ban [-s] <player> <reason>");
                    return false;
                }
            }

            // Check if the target player name is provided
            if (targetName == null) {
                sender.sendMessage(ChatColor.RED + "Invalid Usage! /ban [-s] <player> <reason>");
                return false;
            }

            // Check if there's enough arguments for the reason (minimum of 2 args where `args.length >= 2`)
            if (args.length < 2 && !isSilent) {
                sender.sendMessage(ChatColor.RED + "Invalid Usage! /ban [-s] <player> <reason>");
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

            // Attempt to get the target player (online or offline)
            OfflinePlayer target = Bukkit.getOfflinePlayer(targetName);
            if (target == null || target.getName() == null) {
                sender.sendMessage(ChatColor.RED + "Player not found: " + targetName);
                return false;
            }

            // Add a permanent ban to the target player
            Bukkit.getBanList(BanList.Type.NAME).addBan(
                    target.getName(),
                    ChatColor.RED + reason.toString().trim(),
                    null,
                    player.getName() // Banned by executor
            );

            // If the target is online, kick them immediately
            if (target.isOnline()) {
                Player onlineTarget = (Player) target;
                onlineTarget.kickPlayer(ChatColor.RED + "YOU HAVE BEEN BANNED\n"
                        + ChatColor.WHITE + "Reason: " + reason.toString().trim());
            }

            // Record the ban in the database as permanent
            dbManager.recordGameBan(
                    target.getUniqueId().toString(),
                    Instant.MAX // Long-term ban
            );

            // Log the ban to Discord as an embed message
            Guild guild = jda.getGuildById(configManager.getProperty("server_id"));
            TextChannel textChannel = guild.getTextChannelById(configManager.getProperty("staff_logging_channel_id"));
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm");
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
            String imageUrl = "https://minotar.net/avatar/" + target.getName() + "/60";
            UUID banUUID = UUID.randomUUID();

            EmbedBuilder embedBuilder = new EmbedBuilder();
            embedBuilder.setTitle("**" + target.getName() + "┃" + "Has Been Permanently BANNED" + "**");
            embedBuilder.setDescription("\n \n"
                    + "**Target**\n `" + target.getName() + "`\n"
                    + "**Executor**\n`" + player.getName() + "`\n"
                    + "**Expiration**\nNever\n"
                    + "**Reason**\n`" + reason.toString().trim() + "`\n"
                    + "**Date**\n`" + java.time.LocalDateTime.now().format(formatter) + "`\n \n"
                    + banUUID);
            embedBuilder.setColor(0xFF0000);
            embedBuilder.setThumbnail(imageUrl);

            MessageEmbed embed = embedBuilder.build();
            if (jda != null && jda.getStatus() == JDA.Status.CONNECTED && textChannel != null) {
                textChannel.sendMessageEmbeds(embed).queue(
                        success -> Logger.getLogger("test2").info("Message sent successfully!"),
                        failure -> Logger.getLogger("test").warning("Failed to send message: " + failure.getMessage())
                );
            } else {
                Logger.getLogger("test3").warning("JDA is not properly initialized or the channel is incorrect.");
            }

            // Notify the sender about the ban
            sender.sendMessage(ChatColor.GREEN + "Player " + target.getName() + " has been banned."
                    + (isSilent ? " (Silent)" : ""));

            return true; // Command executed successfully
        }

        // If sender is not a player, manage console logic (if applicable)
        sender.sendMessage(ChatColor.RED + "Only players can execute this command.");
        return false;
    }
}
