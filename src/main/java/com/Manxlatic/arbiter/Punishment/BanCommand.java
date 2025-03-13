package com.Manxlatic.arbiter.Punishment;

import com.Manxlatic.arbiter.Managers.DbManager;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import org.bukkit.BanList;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
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

    private final DbManager dbManager;

    public BanCommand(JDA jda, DbManager dbManager) {
        this.jda = jda;
        this.dbManager = dbManager;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {

        if (sender instanceof Player) {
            Player player = (Player) sender;
            if (args.length >= 1) {

                StringBuilder reason = new StringBuilder();
                for (int i = 2; i < args.length; i++) {
                    reason.append(args[i]).append(" ");

                    if (Bukkit.getOfflinePlayer(args[0]) != null) {
                        Player target = Bukkit.getPlayer(args[0]);
                        Bukkit.getBanList(BanList.Type.NAME).addBan(target.getName(), ChatColor.RED + reason.toString(), null, null);
                        target.kickPlayer(ChatColor.RED + "YOU HAVE BEEN BANNED\n" + ChatColor.WHITE + "Reason: " + reason.toString());
                        dbManager.recordGameBan(target.getUniqueId().toString(), Instant.MAX);

                        //send embed message to discord
                        Guild guild = jda.getGuildById("1207642263463927808");
                        TextChannel textChannel = guild.getTextChannelById("1215793613225721938");
                        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm");
                        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
                        String imageUrl = "https://minotar.net/avatar/" + target.getName() + "/60";
                        UUID banUUID = UUID.randomUUID();

                        EmbedBuilder embedBuilder = new EmbedBuilder();
                        embedBuilder.setTitle("**" + target.getName() + "┃" + "Has Been Permanently BANNED" + "**");
                        embedBuilder.setDescription("\n \n " + "**\nTarget**\n `" + player.getName() + "`\n**Executor**\n`" + player.getName() + "**`\nExpiration**\n Never **\nReason**\n`" + reason + "`**\nDate**\n`" + java.time.LocalDateTime.now().format(formatter) + "`" + "\n \n " + banUUID);
                        embedBuilder.setColor(0xFF0000);
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

                    } else {
                        player.sendMessage("Player Not Found");
                    }
                }

            } else {
                player.sendMessage(ChatColor.RED + "Invalid Usage! /ban <player> <reason>");
            }



        }




        return false;
    }
}
