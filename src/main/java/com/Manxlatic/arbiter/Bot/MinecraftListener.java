package com.Manxlatic.arbiter.Bot;

import com.Manxlatic.arbiter.Arbiter;
import com.Manxlatic.arbiter.Managers.ConfigManager;
import com.Manxlatic.arbiter.Managers.DbManager;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import java.util.UUID;

public class MinecraftListener implements Listener {

    private TextChannel textChannel;

    private final Arbiter arbiter;

    private final DbManager dbManager;
    private final JDA jda;

    public MinecraftListener(Arbiter arbiter, DbManager dbManager, JDA jda) {
        this.arbiter = arbiter;
        this.dbManager = dbManager;
        this.jda = jda;
    }


    @EventHandler
    public void onChat(AsyncPlayerChatEvent event) {
        ConfigManager configManager = arbiter.getConfigManager();
        Player player = event.getPlayer();
        if (dbManager.getMutes().containsKey(player.getUniqueId().toString())) {
            event.setCancelled(true);
            return;
        }


        if (event == null) {
            return;
        }



        if (player == null) {
            return;
        }



        if (jda == null) {
            System.err.println("Jda is not initialised");
            return;
        }
        textChannel = jda.getTextChannelById(configManager.getProperty("bridge_channel_id"));

        if (textChannel == null) {
            System.err.println("TextChannel is null. Check your channel ID.");
            return;
        }
        String message = event.getMessage();

        if (message == null) {
            return;
        }


        if (jda.getStatus() == JDA.Status.CONNECTED) {
            textChannel.sendMessage("[" + player.getName() + "] -> " + message).queue();

        } else {
            System.err.println("Status not set");
        }
    }


    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent e){
        ConfigManager configManager = arbiter.getConfigManager();
        textChannel = jda.getTextChannelById(configManager.getProperty("bridge_channel_id"));
        String imageUrl = "https://minotar.net/avatar/" + e.getPlayer().getName() + "/20";

        EmbedBuilder embedBuilder = new EmbedBuilder();

        embedBuilder.setThumbnail(imageUrl).setDescription(" **" + e.getPlayer().getName() + " left the game**");
        embedBuilder.setDescription("**" + e.getPlayer().getName() + " joined the game**");
        embedBuilder.setColor(0x00FF00);


        MessageEmbed embed = embedBuilder.build();
        textChannel.sendMessageEmbeds(embed).queue();
        e.getPlayer().addAttachment(arbiter, "rank.owner", true);

    }
    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent e){
        ConfigManager configManager = arbiter.getConfigManager();
        textChannel = jda.getTextChannelById(configManager.getProperty("bridge_channel_id"));

        String imageUrl = "https://minotar.net/avatar/" + e.getPlayer().getName() + "/20";

        EmbedBuilder embedBuilder = new EmbedBuilder();


        embedBuilder.setThumbnail(imageUrl).setDescription(" **" + e.getPlayer().getName() + " left the game**");
        embedBuilder.setDescription(" **" + e.getPlayer().getName() + " left the game**");
        embedBuilder.setColor(0xFF0000);


        MessageEmbed embed = embedBuilder.build();
        textChannel.sendMessageEmbeds(embed).queue();




    }


}
