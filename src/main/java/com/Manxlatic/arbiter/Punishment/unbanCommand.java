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

import java.time.format.DateTimeFormatter;
import java.util.UUID;
import java.util.logging.Logger;

public class unbanCommand implements CommandExecutor {

    private final JDA jda;
    
    private final DbManager dbManager;

    public unbanCommand(JDA jda, DbManager dbManager) {
        this.jda = jda;
        this.dbManager = dbManager;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String s, String[] args) {

        Player player = (Player) sender;


        if (args.length >= 2) {
            player.sendMessage("Invalid parameter count, /unban <player>");
            return false;
        }

        Player target = Bukkit.getPlayer(args[0]);
        UUID targetUUID = target.getUniqueId();

        BanList banList = Bukkit.getBanList(BanList.Type.NAME);
        if (dbManager.getBans().containsKey(targetUUID)) {
            if (dbManager.getBans().get(targetUUID) == "MUTE") {
                dbManager.removeGameMutes(targetUUID.toString());
            } else if (dbManager.getBans().get(targetUUID) == "TEMPMUTE") {
                dbManager.removeGameTempMutes(targetUUID.toString());
            }
        } else {
            sender.sendMessage(ChatColor.RED + "This player is not muted");
        }
        if (target != null) {
            banList.pardon(target.getName());
            player.sendMessage(ChatColor.LIGHT_PURPLE + target.getName() + ChatColor.WHITE + " has been " + ChatColor.GREEN + ChatColor.BOLD + "unbanned");
        } else {
            banList.pardon(args[0]);
            player.sendMessage(ChatColor.LIGHT_PURPLE + target.getName() + ChatColor.WHITE + " has been " + ChatColor.GREEN + ChatColor.BOLD + "unbanned");
            Guild guild = jda.getGuildById("1207642263463927808");

            TextChannel textChannel = guild.getTextChannelById("1215793613225721938");
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
            String imageUrl = "https://minotar.net/avatar/" + target.getName() + "/50";

            EmbedBuilder embedBuilder = new EmbedBuilder();
            embedBuilder.setTitle("**" + target.getName() + "┃" + "Has Been UNBANNED" + "**");
            embedBuilder.setDescription("\n \n " + "**\nTarget**\n `" + player.getName() + "`\n**Executor**\n`" + player.getName() + "`\n**Date**\n`" + java.time.LocalDateTime.now().format(formatter) + "`");
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
        }
        return false;
    }
}



