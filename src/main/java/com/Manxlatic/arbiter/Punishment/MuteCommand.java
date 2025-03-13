package com.Manxlatic.arbiter.Punishment;



import com.Manxlatic.arbiter.Arbiter;
import com.Manxlatic.arbiter.Managers.DbManager;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;

import java.text.SimpleDateFormat;
import java.time.Instant;
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



        Player player = (Player) sender;
        Player target = Bukkit.getPlayer(args[0]);


        if (sender instanceof Player) {
            if (args.length < 2) {
                return false;
            }
            StringBuilder reason = new StringBuilder();
            for (int i = 1; i < args.length; i++) {
                reason.append(args[i]).append(" ");
            }

            if (player != null) {
                Instant duration = Instant.MAX;
                player.sendMessage(ChatColor.RED + target.getName() + " has been permanently MUTED. Reason: " + reason);

                dbManager.recordGameMute(target.getUniqueId().toString(), duration);


                Guild guild = jda.getGuildById("1207642263463927808");
                TextChannel textChannel = guild.getTextChannelById("1215793613225721938");
                SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm");
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
                String imageUrl = "https://minotar.net/avatar/" + target.getName() + "/60";
                UUID muteUUID = UUID.randomUUID();

                EmbedBuilder embedBuilder = new EmbedBuilder();
                embedBuilder.setTitle("**" + target.getName() + "┃" + "Has Been Permanently MUTED" + "**");
                embedBuilder.setDescription("\n \n " + "**\nTarget**\n `" + player.getName() + "`\n**Executor**\n`" + player.getName() + "**`\nExpiration**\n Never **\nReason**\n`" + reason + "`**\nDate**\n`" + java.time.LocalDateTime.now().format(formatter) + "`" + "\n \n" + muteUUID);

                embedBuilder.setColor(0xFDDA0D);
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
        }
        return false;
    }


}