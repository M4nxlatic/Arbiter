package com.Manxlatic.arbiter.Bot;

import com.Manxlatic.arbiter.Arbiter;
import com.Manxlatic.arbiter.Managers.ConfigManager;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.bukkit.Bukkit;

public class DiscordListener extends ListenerAdapter {

    private final Arbiter arbiter;

    public DiscordListener(Arbiter arbiter) {
        this.arbiter = arbiter;
    }

    @Override
    public void onMessageReceived(MessageReceivedEvent event) {
        ConfigManager configManager = arbiter.getConfigManager();
        Member member = event.getMember();
        if (member == null) return;
        if (member.getUser().isBot()) {
            return;
        }
        if (!(event.getChannel().getId() == configManager.getProperty("bridge_channel_id"))) {
            return;
        }

        Bukkit.broadcastMessage("[DISCORD] " + member.getNickname() + " -> " + event.getMessage());
    }
}
