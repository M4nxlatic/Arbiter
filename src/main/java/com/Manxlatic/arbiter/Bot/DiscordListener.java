package com.Manxlatic.arbiter.Bot;

import com.Manxlatic.arbiter.Arbiter;
import com.Manxlatic.arbiter.Managers.ConfigManager;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.bukkit.Bukkit;

public class DiscordListener extends ListenerAdapter {

    private final Arbiter arbiter;
    private final ConfigManager configManager;

    public DiscordListener(Arbiter arbiter) {
        this.arbiter = arbiter;
        this.configManager = arbiter.getConfigManager(); // Initialize here
    }

    @Override
    public void onMessageReceived(MessageReceivedEvent event) {
        Member member = event.getMember();
        if (member == null) return;
        if (member.getUser().isBot()) {
            return;
        }
        if (!(event.getChannel().getId().equals(configManager.getProperty("bridge_channel_id")))) {
            return;
        }

        String nickname = (member.getNickname() != null) ? member.getNickname() : member.getEffectiveName();

        Bukkit.broadcastMessage("[DISCORD] " + nickname + " -> " + event.getMessage().getContentDisplay());
    }
}
