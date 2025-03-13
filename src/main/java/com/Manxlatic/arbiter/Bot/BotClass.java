package com.Manxlatic.arbiter.Bot;

import com.Manxlatic.arbiter.Arbiter;
import com.Manxlatic.arbiter.Managers.ConfigManager;
import com.Manxlatic.arbiter.Managers.DbManager;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.entities.channel.concrete.VoiceChannel;
import net.dv8tion.jda.api.events.guild.member.GuildMemberJoinEvent;
import net.dv8tion.jda.api.events.guild.member.GuildMemberRemoveEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.exceptions.RateLimitedException;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.commands.DefaultMemberPermissions;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.requests.GatewayIntent;
import net.dv8tion.jda.api.requests.restaction.CommandListUpdateAction;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.core.Logger;
import org.apache.logging.log4j.core.config.Configurator;

import java.sql.SQLException;
import java.time.Duration;
import java.util.concurrent.TimeUnit;


import static net.dv8tion.jda.api.interactions.commands.OptionType.*;

public class BotClass {

    private static JDA jda;
    private JDABuilder builder;

    private final Arbiter arbiter;



    private ConsoleInjector consoleInjector;

    private final SlashCommandListener slashCommandListener;

    public BotClass(Arbiter arbiter, SlashCommandListener slashCommandListener) {
        this.arbiter = arbiter;
        this.slashCommandListener = slashCommandListener;
    }

    public Boolean start() {
        MessageLogger messageLogger = new MessageLogger(this);

        ConfigManager configManager = arbiter.getConfigManager();




        String botToken = configManager.getProperty("bot_token");
        try {
            builder = JDABuilder.createDefault(botToken);
            builder.addEventListeners(new SlashCommandListener(new DbManager(arbiter), this, arbiter));
            builder.addEventListeners(new AutoMod(new DbManager(arbiter)));
            builder.addEventListeners(new DiscordListener(arbiter));
            //builder.addEventListeners(eventLogger);
            builder.addEventListeners(messageLogger);
            builder.enableIntents(GatewayIntent.MESSAGE_CONTENT, GatewayIntent.GUILD_MEMBERS);
            builder.setActivity(Activity.watching("you"));
            jda = builder.build(); // Build JDA after registering listeners
            System.out.println("discord.java jda " + jda);

            jda.awaitReady();



            // Set up ConsoleInjector
            JDADiscordService discordService = new JDADiscordService("https://discord.com/api/webhooks/" + configManager.getProperty("webhook_id")); // Replace with your actual webhook ID
            consoleInjector = new ConsoleInjector(discordService);

            // Add the ConsoleInjector appender to the root logger
            Logger rootLogger = (Logger) LogManager.getRootLogger();
            rootLogger.addAppender(consoleInjector);

            // Start the ConsoleInjector appender
            consoleInjector.start();

            // Set the logger levels to suppress debug and trace messages
            Configurator.setLevel("com.testing.messaging", Level.INFO);

        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }

        Guild guild = jda.getGuildById(configManager.getProperty("server_id"));
        if (guild == null) {
            System.err.println("Guild not found!");
            return false;
        }
        if (guild != null) {
            TextChannel logChannel = guild.getTextChannelById(configManager.getProperty("log_channel_id"));
            if (logChannel != null) {
                messageLogger.setLogChannel(logChannel);
            }
        }
        String newName = "\uD83D\uDFE2 ┃server status";
        VoiceChannel voiceChannel = guild.getVoiceChannelById(configManager.getProperty("voice_status_channel_id"));
        if (voiceChannel != null) {
            System.out.println("Setting VC name to: " + newName);
            voiceChannel.getManager().setName(newName).queue(
                    success -> System.out.println("Successfully updated channel name to: " + newName),
                    failure -> {
                        if (failure.getCause() instanceof RateLimitedException) {
                            RateLimitedException rateLimitException = (RateLimitedException) failure.getCause();
                            System.err.println("Rate limit exceeded. Retry after: " + rateLimitException.getRetryAfter());
                            // Implement retry logic if necessary
                        } else {
                            System.err.println("Failed to update channel name. Error: " + failure.getMessage());
                        }
                    }
            );
        } else {
            System.err.println("VoiceChannel with ID " + voiceChannel.getId() + " is null");
        }

        TextChannel textChannel = guild.getTextChannelById(configManager.getProperty("bridge_channel_id"));
        if (textChannel != null) {
            textChannel.sendMessage("Server has started :white_check_mark:").queue();
        } else {
            System.err.println("TextChannel is null");
        }

        CommandListUpdateAction commands = jda.updateCommands();

        // Add all your commands on this action instance
        commands.addCommands(
                Commands.slash("say", "Makes the bot say what you tell it to")
                        .addOption(STRING, "content", "What the bot should say", true)
                        .setDefaultPermissions(DefaultMemberPermissions.DISABLED),
                Commands.slash("rules", "rules?")
                        .addOption(CHANNEL, "channel", "where to send the rules", true)
                        .setDefaultPermissions(DefaultMemberPermissions.DISABLED),
                Commands.slash("unmute", "unmutes a member")
                        .addOption(USER, "member", "choose someone to unmute", true)
                        .setDefaultPermissions(DefaultMemberPermissions.DISABLED),
                Commands.slash("unban", "unbans a user")
                        .addOption(STRING, "user", "choose someone to unban", true)
                        .setDefaultPermissions(DefaultMemberPermissions.DISABLED),
                Commands.slash("ban", "bans a member")
                        .addOption(USER, "member", "choose someone to ban", true)
                        .setDefaultPermissions(DefaultMemberPermissions.DISABLED),
                Commands.slash("mute", "mutes a member")
                        .addOption(USER, "member", "choose someone to mute", true)
                        .addOption(STRING, "duration", "how long to mute them for")
                        .setDefaultPermissions(DefaultMemberPermissions.DISABLED),
                Commands.slash("tempban", "temporarily bans a member")
                        .addOption(USER, "member", "choose someone to ban", true)
                        .addOption(STRING, "duration", "how long to ban them for")
                        .setDefaultPermissions(DefaultMemberPermissions.DISABLED),
                Commands.slash("execute", "Execute a Minecraft console command")
                        .addOption(STRING, "command", "Minecraft command to execute", true)
                        .setDefaultPermissions(DefaultMemberPermissions.DISABLED),
                Commands.slash("removeinfractions", "clears a users infractions")
                        .addOption(STRING, "user", "which user", true)
                        .setDefaultPermissions(DefaultMemberPermissions.DISABLED)

        );

        // Then finally send your commands to discord using the API
        commands.queue();


        return true;
    }

    public JDA getJda() {
        return jda;
    }


    public class AutoMod extends ListenerAdapter {

        private DbManager dbManager;

        public AutoMod(DbManager dbManager) {
            this.dbManager = dbManager;
        }


        private final long MUTE_DURATION = TimeUnit.MINUTES.toMillis(60); // Duration for which the user is muted
        private final int MAX_WARNINGS_BEFORE_MUTE = 5;
        private final int MAX_MUTES_BEFORE_BAN = 5;


        @Override
        public void onMessageReceived(MessageReceivedEvent event) {
            Message message = event.getMessage();
            User author = message.getAuthor();
            String content = message.getContentRaw();
            Member member = event.getMember();

            // Check if the message was sent in a guild
            if (event.isFromGuild()) {
                Guild guild = event.getGuild();
                if (guild == null) {
                    System.out.println("Guild is null");
                    return;
                }

                //Role muteRole = guild.getRolesByName("Muted", true).stream().findFirst().orElse(null);

                // Skip if the author is a bot
                if (author.isBot()) {
                    return;
                }

                /* If the user is muted, prevent the message from being shown
                if (member != null && muteRole != null && member.getRoles().contains(muteRole)) {
                    // Delete the message immediately
                    message.delete().queue(success -> {
                        // Notify the user via DM after deleting the message
                        author.openPrivateChannel().queue(privateChannel -> {
                            privateChannel.sendMessage("You are currently muted. Your message was deleted.").queue();
                        });
                    }, failure -> {
                        // Log error if message deletion fails
                        System.err.println("Failed to delete message: " + failure.getMessage());
                    });
                    return;
                }*/

                // Check for banned words
                if (containsBannedWord(content)) {
                    // Delete the message immediately
                    message.delete().queue(success -> {
                        // Notify the user via DM after deleting the message
                        author.openPrivateChannel().queue(privateChannel -> {
                            privateChannel.sendMessage("Your message contained inappropriate content and was deleted.").queue();
                        });

                        // Record the infraction
                        handleInfraction(event);
                    }, failure -> {
                        // Log error if message deletion fails
                        System.err.println("Failed to delete message: " + failure.getMessage());
                    });
                    return;
                }
            } else {
                // Handle direct messages if necessary
                System.out.println("Message is from a direct message");
                // Optionally, handle direct messages here
            }
        }


        private boolean containsBannedWord(String content) {
            // Define regex pattern for banned words
            String regex = "(?i)\\b(sh[\\s!@#\\$%^&*()_\\-+=]*i[\\s!@#\\$%^&*()_\\-+=]*t|"
                    + "f[\\s!@#\\$%^&*()_\\-+=]*u[\\s!@#\\$%^&*()_\\-+=]*c[\\s!@#\\$%^&*()_\\-+=]*k|"
                    + "b[\\s!@#\\$%^&*()_\\-+=]*i[\\s!@#\\$%^&*()_\\-+=]*t[\\s!@#\\$%^&*()_\\-+=]*c[\\s!@#\\$%^&*()_\\-+=]*h|"
                    + "c[\\s!@#\\$%^&*()_\\-+=]*u[\\s!@#\\$%^&*()_\\-+=]*n[\\s!@#\\$%^&*()_\\-+=]*t|"
                    + "a[\\s!@#\\$%^&*()_\\-+=]*s[\\s!@#\\$%^&*()_\\-+=]*s|"
                    + "d[\\s!@#\\$%^&*()_\\-+=]*i[\\s!@#\\$%^&*()_\\-+=]*c[\\s!@#\\$%^&*()_\\-+=]*k|"
                    + "m[\\s!@#\\$%^&*()_\\-+=]*o[\\s!@#\\$%^&*()_\\-+=]*t[\\s!@#\\$%^&*()_\\-+=]*h|"
                    + "p[\\s!@#\\$%^&*()_\\-+=]*u[\\s!@#\\$%^&*()_\\-+=]*s[\\s!@#\\$%^&*()_\\-+=]*s|"
                    + "s[\\s!@#\\$%^&*()_\\-+=]*l[\\s!@#\\$%^&*()_\\-+=]*u[\\s!@#\\$%^&*()_\\-+=]*t|"
                    + "n[\\s!@#\\$%^&*()_\\-+=]*i[\\s!@#\\$%^&*()_\\-+=]*g[\\s!@#\\$%^&*()_\\-+=]*g[\\s!@#\\$%^&*()_\\-+=]*a[\\s!@#\\$%^&*()_\\-+=]*r[\\s!@#\\$%^&*()_\\-+=]*)\\b";
            return content.matches(regex);
        }

        private void handleInfraction(MessageReceivedEvent event) {
            ConfigManager configManager = arbiter.getConfigManager();
            User author = event.getAuthor();
            String userId = author.getId();
            Member member = event.getMember();
            TextChannel staffChannel = event.getGuild().getTextChannelById(configManager.getProperty("staff_logging_channel_id"));

            dbManager.recordInfraction(userId, "warning");
            int warnings = dbManager.countUserInfractions(userId, "warning");
            int mutes = dbManager.countUserInfractions(userId, "mute");

            String privateMessage = "You have been warned for inappropriate content. " +
                    "Please be mindful of the community guidelines to avoid further actions.";

            if (warnings >= MAX_WARNINGS_BEFORE_MUTE) {
                // Apply timeout (referred to as "muting")
                if (member != null) {
                    // Example timeout duration (e.g., 1 hour)
                    Duration timeoutDuration = Duration.ofHours(1);
                    member.timeoutFor(timeoutDuration).queue(
                            success -> staffChannel.sendMessage("User " + author.getName() + " has been muted for inappropriate content.").queue(),
                            failure -> staffChannel.sendMessage("Failed to mute user: " + failure.getMessage()).queue()
                    );
                    dbManager.resetWarnings(userId);
                    dbManager.recordInfraction(author.getId(), "mute");
                    privateMessage = "You have been muted for inappropriate content. " +
                            "Please be mindful of the community guidelines to avoid further actions.";
                } else {
                    dbManager.recordInfraction(userId, "warning");
                }
                if (mutes >= MAX_MUTES_BEFORE_BAN) {
                    banUser(event, author);
                    privateMessage = "You have been banned for repeated infractions. " +
                            "You can appeal this decision with the server admin.";
                }

                String finalMessage = privateMessage;
                author.openPrivateChannel().queue(privateChannel -> {
                    staffChannel.sendMessage(finalMessage).queue();
                });
            }
        }


        public void banUser(MessageReceivedEvent event, User user) {
            ConfigManager configManager = arbiter.getConfigManager();
            Guild guild = event.getGuild();
            Member member = guild.getMember(user);
            TextChannel staffChannel = guild.getTextChannelById(configManager.getProperty("staff_logging_channel_id"));
            if (member != null) {
                guild.ban(member, 0, TimeUnit.DAYS).queue();
                staffChannel.sendMessage("User " + user.getAsTag() + " has been banned for exceeding the maximum number of mutes.").queue();
                dbManager.recordInfraction(user.getId(), "ban");
            }
        }

        @Override
        public void onGuildMemberJoin(GuildMemberJoinEvent event) {
            Guild guild = event.getGuild();
            Member member = event.getMember();

            if (member != null) {
                // Get the role by name
                Role trainer = guild.getRolesByName("trainer", true).stream().findFirst().orElse(null);
                if (trainer != null) {
                    // Add the role to the member
                    guild.addRoleToMember(member, trainer).queue(
                            success -> System.out.println("Role added successfully"),
                            error -> System.err.println("Failed to add role: " + error.getMessage())
                    );
                } else {
                    System.err.println("Role 'trainer' not found.");
                }
            } else {
                System.err.println("Member not found.");
            }

            try {
                dbManager.insertUser(event.getUser().getIdLong(), event.getUser().getName());
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        }


        @Override
        public void onGuildMemberRemove(GuildMemberRemoveEvent event) {
            try {
                dbManager.deleteUser(event.getUser().getIdLong());
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        }

    }
}