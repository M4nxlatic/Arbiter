package com.Manxlatic.arbiter.Bot;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.audit.ActionType;
import net.dv8tion.jda.api.audit.AuditLogEntry;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.entities.channel.concrete.VoiceChannel;
import net.dv8tion.jda.api.events.channel.ChannelCreateEvent;
import net.dv8tion.jda.api.events.channel.ChannelDeleteEvent;
import net.dv8tion.jda.api.events.channel.update.ChannelUpdateNameEvent;
import net.dv8tion.jda.api.events.guild.GuildBanEvent;
import net.dv8tion.jda.api.events.guild.GuildUnbanEvent;
import net.dv8tion.jda.api.events.guild.member.GuildMemberJoinEvent;
import net.dv8tion.jda.api.events.guild.member.GuildMemberRemoveEvent;
import net.dv8tion.jda.api.events.guild.member.update.GuildMemberUpdateNicknameEvent;
import net.dv8tion.jda.api.events.guild.voice.GuildVoiceUpdateEvent;
import net.dv8tion.jda.api.events.message.MessageBulkDeleteEvent;
import net.dv8tion.jda.api.events.message.MessageDeleteEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.events.message.MessageUpdateEvent;
import net.dv8tion.jda.api.events.role.RoleCreateEvent;
import net.dv8tion.jda.api.events.role.RoleDeleteEvent;
import net.dv8tion.jda.api.events.role.update.RoleUpdateColorEvent;
import net.dv8tion.jda.api.events.role.update.RoleUpdateIconEvent;
import net.dv8tion.jda.api.events.role.update.RoleUpdateNameEvent;
import net.dv8tion.jda.api.events.role.update.RoleUpdatePermissionsEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.requests.restaction.pagination.AuditLogPaginationAction;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class MessageLogger extends ListenerAdapter {

    private final Map<String, String> messageCache = new HashMap<>();
    private TextChannel logChannel;

    private final BotClass bot;

    public MessageLogger(BotClass bot) {
        this.bot = bot;
    }

    public void setLogChannel(TextChannel logChannel) {
        this.logChannel = logChannel;
    }

    @Override
    public void onMessageReceived(MessageReceivedEvent event) {
        if (event.getAuthor().isBot()) return;

        // Cache the message content when it is first received
        messageCache.put(event.getMessageId(), event.getMessage().getContentDisplay());
    }

    @Override
    public void onMessageUpdate(MessageUpdateEvent event) {
        if (event.getAuthor().isBot()) return;
        String oldContent = messageCache.get(event.getMessageId());
        if (oldContent != null) {
            String newContent = event.getMessage().getContentDisplay();
            User author = event.getAuthor();
            String timestamp = DateTimeFormatter.ofPattern("HH:mm").format(LocalDateTime.now());
            String MessageUrl = event.getMessage().getJumpUrl();
            String ChannelUrl = "https://discord.com/channels/" + event.getGuild().getId() + "/" + event.getChannel().getId();
            String UserUrl = author.getAvatarUrl() + "?size=32";
            EmbedBuilder embedBuilder = new EmbedBuilder();
            embedBuilder.setThumbnail(UserUrl).setDescription("***" + author.getName() + "***\n **Message edited in** " + ChannelUrl + " [**Jump to Message**](" + MessageUrl + ")\n\n Before:\n " + oldContent + "\n\n After:\n " + newContent);
            embedBuilder.setColor(0xFFFF00);
            embedBuilder.setFooter("User Id: " + author.getId() + " • Today at " + timestamp);
            MessageEmbed embed = embedBuilder.build();
            System.out.println("printing Message to: " + logChannel);
            logChannel.sendMessageEmbeds(embed).queue();
            messageCache.put(event.getMessageId(), newContent);
        }
    }

    @Override
    public void onChannelUpdateName(ChannelUpdateNameEvent event) {
        String ChannelUrl = "https://discord.com/channels/" + event.getGuild().getId() + "/" + event.getChannel().getId();
        String timestamp = DateTimeFormatter.ofPattern("HH:mm").format(LocalDateTime.now());
        EmbedBuilder embedBuilder = new EmbedBuilder();
        embedBuilder.setTitle("Channel Updated");
        embedBuilder.setDescription(ChannelUrl + " was changed:\n\nName changed: **" + event.getOldValue() + " -> " + event.getNewValue() + "**");
        embedBuilder.setFooter("Channel Id: " + event.getChannel().getId() + " • Today at " + timestamp);
        MessageEmbed embed = embedBuilder.build();
        logChannel.sendMessageEmbeds(embed).queue();
    }

    @Override
    public void onChannelCreate(ChannelCreateEvent event) {
        String timestamp = DateTimeFormatter.ofPattern("HH:mm").format(LocalDateTime.now());
        EmbedBuilder embedBuilder = new EmbedBuilder();
        embedBuilder.setTitle("**Channel Created**");
        embedBuilder.setColor(0x00FF00);
        embedBuilder.setDescription("**Channel Created: #" + event.getChannel().getName() + "**");
        embedBuilder.setFooter("Channel Id: " + event.getChannel().getId() + " • Today at " + timestamp);
        MessageEmbed embed = embedBuilder.build();
        logChannel.sendMessageEmbeds(embed).queue();
    }

    @Override
    public void onChannelDelete(ChannelDeleteEvent event) {
        String timestamp = DateTimeFormatter.ofPattern("HH:mm").format(LocalDateTime.now());
        EmbedBuilder embedBuilder = new EmbedBuilder();
        embedBuilder.setTitle("**Channel Deleted**");
        embedBuilder.setColor(0xFF0000);
        embedBuilder.setDescription("Channel Deleted: " + event.getChannel().getName());
        embedBuilder.setFooter("Channel Id: " + event.getChannel().getId() + " • Today at " + timestamp);
        MessageEmbed embed = embedBuilder.build();
        logChannel.sendMessageEmbeds(embed).queue();
    }

    @Override
    public void onGuildBan(GuildBanEvent event) {
        String timestamp = DateTimeFormatter.ofPattern("HH:mm").format(LocalDateTime.now());
        EmbedBuilder embedBuilder = new EmbedBuilder();
        embedBuilder.setTitle("**Member Banned**");
        embedBuilder.setColor(0xFF0000);
        embedBuilder.setDescription(event.getUser().getAsMention() + event.getUser().getEffectiveName());
        embedBuilder.setFooter("User Id: " + event.getUser().getId() + " • Today at " + timestamp);
        MessageEmbed embed = embedBuilder.build();
        logChannel.sendMessageEmbeds(embed).queue();
    }

    @Override
    public void onGuildUnban(GuildUnbanEvent event) {
        String timestamp = DateTimeFormatter.ofPattern("HH:mm").format(LocalDateTime.now());
        EmbedBuilder embedBuilder = new EmbedBuilder();
        embedBuilder.setTitle("**Member Unbanned**");
        embedBuilder.setColor(0x00FF00);
        embedBuilder.setDescription(event.getUser().getAsMention() + event.getUser().getEffectiveName());
        embedBuilder.setFooter("User Id: " + event.getUser().getId() + " • Today at " + timestamp);
        MessageEmbed embed = embedBuilder.build();
        logChannel.sendMessageEmbeds(embed).queue();
    }

    @Override
    public void onGuildMemberJoin(GuildMemberJoinEvent event) {
        String timestamp = DateTimeFormatter.ofPattern("HH:mm").format(LocalDateTime.now());
        EmbedBuilder embedBuilder = new EmbedBuilder();
        embedBuilder.setTitle("**Member Joined**");
        embedBuilder.setColor(0x00FF00);
        embedBuilder.setDescription(event.getUser().getAsMention() + event.getUser().getEffectiveName());
        embedBuilder.setFooter("User Id: " + event.getUser().getId() + " • Today at " + timestamp);
        MessageEmbed embed = embedBuilder.build();
        logChannel.sendMessageEmbeds(embed).queue();
    }

    @Override
    public void onGuildMemberRemove(GuildMemberRemoveEvent event) {
        String timestamp = DateTimeFormatter.ofPattern("HH:mm").format(LocalDateTime.now());
        EmbedBuilder embedBuilder = new EmbedBuilder();
        embedBuilder.setTitle("**Member Left**");
        embedBuilder.setColor(0x00FF00);
        embedBuilder.setDescription(event.getUser().getAsMention() + event.getUser().getEffectiveName());
        embedBuilder.setFooter("User Id: " + event.getUser().getId() + " • Today at " + timestamp);
        MessageEmbed embed = embedBuilder.build();
        logChannel.sendMessageEmbeds(embed).queue();
    }

    @Override
    public void onGuildMemberUpdateNickname(GuildMemberUpdateNicknameEvent event) {
        String timestamp = DateTimeFormatter.ofPattern("HH:mm").format(LocalDateTime.now());
        EmbedBuilder embedBuilder = new EmbedBuilder();
        embedBuilder.setTitle("**Nickname Changed**");
        embedBuilder.setColor(0x00FF00);
        embedBuilder.setDescription(event.getUser().getAsMention() + "\n\n Before:\n" + event.getOldNickname() + "\n\nAfter:\n" + event.getNewNickname());
        embedBuilder.setFooter("User Id: " + event.getUser().getId() + " • Today at " + timestamp);
        MessageEmbed embed = embedBuilder.build();
        logChannel.sendMessageEmbeds(embed).queue();
    }

    @Override
    public void onRoleCreate(RoleCreateEvent event) {
        String timestamp = DateTimeFormatter.ofPattern("HH:mm").format(LocalDateTime.now());
        EmbedBuilder embedBuilder = new EmbedBuilder();
        embedBuilder.setTitle("**Role Created**");
        embedBuilder.setColor(0x00FF00);
        embedBuilder.setDescription("Role Created: " + event.getRole().getName());
        embedBuilder.setFooter("Role Id: " + event.getRole().getId() + " • Today at " + timestamp);
        MessageEmbed embed = embedBuilder.build();
        logChannel.sendMessageEmbeds(embed).queue();
    }

    @Override
    public void onRoleDelete(RoleDeleteEvent event) {
        String timestamp = DateTimeFormatter.ofPattern("HH:mm").format(LocalDateTime.now());
        EmbedBuilder embedBuilder = new EmbedBuilder();
        embedBuilder.setTitle("**Role Deleted**");
        embedBuilder.setColor(0xFF0000);
        embedBuilder.setDescription("Role Deleted: " + event.getRole().getName());
        embedBuilder.setFooter("Role Id: " + event.getRole().getId() + " • Today at " + timestamp);
        MessageEmbed embed = embedBuilder.build();
        logChannel.sendMessageEmbeds(embed).queue();
    }

    @Override
    public void onRoleUpdateColor(RoleUpdateColorEvent event) {
        String timestamp = DateTimeFormatter.ofPattern("HH:mm").format(LocalDateTime.now());
        EmbedBuilder embedBuilder = new EmbedBuilder();
        embedBuilder.setTitle("**Role Color Changed**");
        embedBuilder.setColor(event.getNewColor());
        embedBuilder.setDescription(event.getRole().getName()  + "\n\n Role Color Changed: #" + event.getOldColor().hashCode()  + " -> #" + event.getNewColor().hashCode());
        embedBuilder.setFooter("Role Id: " + event.getRole().getId() + " • Today at " + timestamp);
        MessageEmbed embed = embedBuilder.build();
        logChannel.sendMessageEmbeds(embed).queue();
    }

    @Override
    public void onRoleUpdateIcon(RoleUpdateIconEvent event) {
        String timestamp = DateTimeFormatter.ofPattern("HH:mm").format(LocalDateTime.now());
        EmbedBuilder embedBuilder = new EmbedBuilder();
        if (event.getOldIcon() == null) {
            embedBuilder.setTitle("**Role Icon Added**");
            embedBuilder.setColor(0x00FF00);
            embedBuilder.setDescription(event.getRole().getName()  + "\n\n Role Icon Added: " + event.getNewIcon().getIcon());
            embedBuilder.setFooter("Role Id: " + event.getRole().getId() + " • Today at " + timestamp);
        }else {

            embedBuilder.setTitle("**Role Icon Changed**");
            embedBuilder.setColor(0x00FF00);
            embedBuilder.setDescription(event.getRole().getName() + "\n\n Role Icon Changed: #" + event.getOldIcon().getIcon() + " -> #" + event.getNewIcon().getIcon());
            embedBuilder.setFooter("Role Id: " + event.getRole().getId() + " • Today at " + timestamp);
        }
        MessageEmbed embed = embedBuilder.build();
        logChannel.sendMessageEmbeds(embed).queue();
    }

    @Override
    public void onRoleUpdateName(RoleUpdateNameEvent event) {
        String timestamp = DateTimeFormatter.ofPattern("HH:mm").format(LocalDateTime.now());
        EmbedBuilder embedBuilder = new EmbedBuilder();
        embedBuilder.setTitle("**Nickname Changed**");
        embedBuilder.setColor(0x00FF00);
        embedBuilder.setDescription("Before:\n" + event.getOldName() + "\n\nAfter:\n" + event.getNewName());
        embedBuilder.setFooter("User Id: " + event.getRole().getId() + " • Today at " + timestamp);
        MessageEmbed embed = embedBuilder.build();
        logChannel.sendMessageEmbeds(embed).queue();
    }

    @Override
    public void onRoleUpdatePermissions(RoleUpdatePermissionsEvent event) {
        Role role = event.getRole();
        List<Permission> oldPermissions = event.getOldPermissions().stream().collect(Collectors.toList());
        List<Permission> newPermissions = event.getNewPermissions().stream().collect(Collectors.toList());
        String oldPermissionsList = oldPermissions.stream()
                .map(Permission::getName)
                .sorted()
                .collect(Collectors.joining("\n• ", "• ", ""));
        String newPermissionsList = newPermissions.stream()
                .map(Permission::getName)
                .sorted()
                .collect(Collectors.joining("\n• ", "• ", ""));
        EmbedBuilder embedBuilder = new EmbedBuilder();
        embedBuilder.setTitle("Role Permissions Updated");
        embedBuilder.setDescription(role.getName() + "\n\n**Before:**\n" + oldPermissionsList + "\n\n**After:**\n" + newPermissionsList);
        embedBuilder.setColor(0xFF0000);
        embedBuilder.setFooter("Role Id: " + role.getId());
        MessageEmbed embed = embedBuilder.build();
        logChannel.sendMessageEmbeds(embed).queue();
    }

    @Override
    public void onMessageDelete(MessageDeleteEvent event) {
        String messageId = event.getMessageId();
        Guild guild = event.getGuild();
        AuditLogPaginationAction auditLogs = guild.retrieveAuditLogs();
        auditLogs.queue(entries -> {
            for (AuditLogEntry entry : entries) {
                if (entry.getType() == ActionType.MESSAGE_DELETE && entry.getTargetId().equals(messageId)) {

                    String authorId = entry.getUserId();
                    User author = bot.getJda().getUserById(authorId);
                    String timestamp = DateTimeFormatter.ofPattern("HH:mm").format(LocalDateTime.now());
                    String ChannelUrl = "https://discord.com/channels/1207642263463927808/" + event.getChannel().getId();

                    EmbedBuilder embedBuilder = new EmbedBuilder();
                    embedBuilder.setTitle("Message Deleted");
                    embedBuilder.setDescription("Message Deleted in: " + ChannelUrl);
                    embedBuilder.setColor(0xFFFF00);
                    embedBuilder.setFooter("User Id: " + author.getId() + " • " + timestamp);
                    MessageEmbed embed = embedBuilder.build();

                }
            }
        });
    }
    @Override
    public void onMessageBulkDelete(MessageBulkDeleteEvent event) {
        String timestamp = DateTimeFormatter.ofPattern("HH:mm").format(LocalDateTime.now());
        EmbedBuilder embedBuilder = new EmbedBuilder();
        embedBuilder.setTitle("**Bulk Delete**");
        embedBuilder.setColor(0x00FF00);
        embedBuilder.setDescription("Bulk Delete in " + event.getChannel().getAsMention() + ", " + event.getResponseNumber() + " Messages Deleted");
        embedBuilder.setFooter("Channel id" + event.getChannel().getId() + " • " + timestamp);
        MessageEmbed embed = embedBuilder.build();
        logChannel.sendMessageEmbeds(embed).queue();
    }

    @Override
    public void onGuildVoiceUpdate(GuildVoiceUpdateEvent event) {
        VoiceChannel oldChannel = (VoiceChannel) event.getOldValue();
        VoiceChannel newChannel = (VoiceChannel) event.getNewValue();
        String timestamp = DateTimeFormatter.ofPattern("HH:mm").format(LocalDateTime.now());
        EmbedBuilder embedBuilder = new EmbedBuilder();

        if (oldChannel == null && newChannel != null) {
            embedBuilder.setTitle("**Voice Channel Joined**");
            embedBuilder.setColor(0x00FF00);
            embedBuilder.setDescription(event.getMember().getAsMention() + " has joined " + newChannel.getAsMention());
            embedBuilder.setFooter("Channel id" + newChannel.getId() + " • " + timestamp);
        } else if (oldChannel != null && newChannel == null) {
            embedBuilder.setTitle("**Voice Channel Left**");
            embedBuilder.setColor(0xFF0000);
            embedBuilder.setDescription(event.getMember().getAsMention() + " has left " + oldChannel.getAsMention());
            embedBuilder.setFooter("Channel id" + oldChannel.getId() + " • " + timestamp);
        } else if (oldChannel != null && newChannel != null) {
            embedBuilder.setTitle("**Voice Channel Moved**");
            embedBuilder.setColor(0xFFFF00);
            embedBuilder.setDescription(event.getMember().getAsMention() + " has moved from " + oldChannel.getAsMention() + " to " + newChannel.getAsMention());
            embedBuilder.setFooter("oldChannel id: " + oldChannel.getId() + " • " + timestamp);
        }
        MessageEmbed embed = embedBuilder.build();
        logChannel.sendMessageEmbeds(embed).queue();
    }

}
