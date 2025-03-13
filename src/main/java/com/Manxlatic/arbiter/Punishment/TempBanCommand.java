package com.Manxlatic.arbiter.Punishment;

import com.Manxlatic.arbiter.Managers.DbManager;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import org.bukkit.BanList;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.text.SimpleDateFormat;
import java.time.Duration;
import java.time.Instant;
import java.time.format.DateTimeFormatter;
import java.util.Calendar;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class TempBanCommand implements CommandExecutor {
    private final JDA jda;

    private final DbManager dbManager;

    public TempBanCommand(JDA jda, DbManager dbManager) {
        this.jda = jda;
        this.dbManager = dbManager;
    }


    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {

        Calendar calendar = Calendar.getInstance();


        if (!(sender instanceof Player))
        {
            return false;
        }

        Player player = (Player) sender;



        StringBuilder reason = new StringBuilder();
        for (int i = 2; i < args.length; i++) {
            reason.append(args[i]).append(" ");
        }

        Instant duration = Instant.now().plus(parse(args[1]));

        Player target = Bukkit.getPlayer(args[0]);

        // Check if the player is online
        BanList banList = Bukkit.getBanList(BanList.Type.NAME);
        if (target != null) {
            // Ban the player using the BanList
            banList.addBan(target.getName(), "Banned for reason", calendar.getTime(), "Console");

            // Kick the player
            target.kickPlayer("You have been temporarily banned until: " + duration + " for: " + reason);
        } else {
            // Player is not online, you can still add a ban entry for offline players
            banList.addBan(args[0], "Banned for reason " + reason, calendar.getTime(), "Console");
        }

        dbManager.recordGameTempBan(player.getUniqueId().toString(), duration);

        player.sendMessage("Player " + args[0] + " has been banned");

        Guild guild = jda.getGuildById("1207642263463927808");
        TextChannel textChannel = guild.getTextChannelById("1215793613225721938");
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm");
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
        String imageUrl = "https://minotar.net/avatar/" + target.getName() + "/60";


        EmbedBuilder embedBuilder = new EmbedBuilder();
        embedBuilder.setTitle("**" + target.getName() + "┃" + "Has Been BANNED" + "**");
        embedBuilder.setDescription("\n \n " + "**\nTarget**\n `" + target.getName() + "`\n**Executor**\n`" + player.getName() + "`**\nExpiration**\n`" + dateFormat.format(calendar.getTime()) + "`**\nReason**\n`" + reason + "`**\nDate**\n`" + java.time.LocalDateTime.now().format(formatter) + "`");
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
        return false;
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