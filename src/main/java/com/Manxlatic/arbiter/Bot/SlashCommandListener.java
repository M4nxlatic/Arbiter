package com.Manxlatic.arbiter.Bot;


import com.Manxlatic.arbiter.Arbiter;
import com.Manxlatic.arbiter.Managers.ConfigManager;
import com.Manxlatic.arbiter.Managers.DbManager;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.requests.RestAction;
import org.bukkit.Bukkit;
import org.bukkit.command.ConsoleCommandSender;

import java.time.Duration;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SlashCommandListener extends ListenerAdapter {
    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
    private final DbManager dbManager;

    private final Arbiter arbiter;

    public SlashCommandListener(DbManager dbManager, BotClass bot, Arbiter arbiter) {
        this.dbManager = dbManager;
        this.arbiter = arbiter;
    }

    private static BotClass instance;

    public BotClass getInstance() {
        if (instance == null) {
            instance = new BotClass(arbiter, this);
        }
        return instance;
    }
    @Override
    public void onSlashCommandInteraction(SlashCommandInteractionEvent event) {
        ConfigManager configManager = arbiter.getConfigManager();
        BotClass bot = getInstance();
        switch (event.getName()) {
            case "say": {
                String content = event.getOption("content", OptionMapping::getAsString);
                event.reply(content).queue();
                break;
            }
            case "rules": {
                event.reply("Working on it...").queue();
                Guild guild = bot.getJda().getGuildById(configManager.getProperty("server_id"));
                if (guild == null) {
                    event.getHook().sendMessage("Guild not found!").setEphemeral(true).queue();
                    break;
                }
                String channelId = event.getOption("channel") != null ? event.getOption("channel").getAsString() : null;
                if (channelId == null || channelId.isEmpty()) {
                    event.getHook().sendMessage("You must specify a valid channel.").setEphemeral(true).queue();
                    return;
                }
                TextChannel channel = guild.getTextChannelById(channelId);
                if (channel == null) {
                    event.getHook().sendMessage("The selected channel could not be found.").setEphemeral(true).queue();
                    return;
                }
                EmbedBuilder embedBuilder = new EmbedBuilder();
                embedBuilder.setTitle("**RULES**");
                embedBuilder.setDescription(
                        "**1. Be respectful** - You must respect all users, regardless of your liking towards them. Treat others the way you want to be treated.\n" +
                                "**2. No Inappropriate Language** - The use of profanity should be kept to a minimum. However, any derogatory language towards any user is prohibited.\n" +
                                "**3. No spamming** - Don't send a lot of small messages right after each other. Do not disrupt chat by spamming.\n" +
                                "**4. No pornographic/adult/other NSFW material** - This is a community server and not meant to share this kind of material.\n" +
                                "**5. No advertisements** - We do not tolerate any kind of advertisements, whether it be for other communities or streams.\n" +
                                "**6. No offensive names and profile pictures** - You will be asked to change your name or profile picture if our staff deems them inappropriate.\n" +
                                "**7. Server Raiding** - Raiding or mentions of raiding are not allowed.\n" +
                                "**8. Direct & Indirect Threats** - Threats to other users of DDoS, Death, DoX, abuse, and other malicious threats are absolutely prohibited and disallowed.\n" +
                                "**9. Follow the Discord Community Guidelines** - You can find them **[here](https://discordapp.com/guidelines)**.\n" +
                                "**10. Please refrain from directly messaging individual staff members for support.** If you encounter an urgent issue such as spamming, hacking, or similar concerns, please contact an online staff member who will assist you promptly.\n\n" +
                                "**Our staff may issue Warnings, Mutes, Kicks, or Bans at their discretion.** If you believe a punishment was unfair, please create a ticket to address the matter. Do NOT discuss it in public channels, as this may result in further action being taken.\n\n" +
                                "**Your presence in this server implies accepting these rules, including all further changes.** These changes might be done at any time without notice; it is your responsibility to check for them.\n\n **Thank you,**"
                );
                embedBuilder.setFooter("- Pulse-Pixel staff team.");

                MessageEmbed embed = embedBuilder.build();
                channel.sendMessageEmbeds(embed).queue(
                        success -> event.getHook().sendMessage("Rules have been sent to the specified channel.").setEphemeral(true).queue(),
                        failure -> event.getHook().sendMessage("An error occurred while sending the rules.").setEphemeral(true).queue()
                );
                break;
            }
            case "unmute": {
                event.reply("Working on it...").setEphemeral(true).queue();
                Member member = event.getOption("member") != null ? event.getOption("member").getAsMember() : null;
                OffsetDateTime timeoutEnd = member.getTimeOutEnd();

                if (timeoutEnd != null && timeoutEnd.isAfter(OffsetDateTime.now(ZoneOffset.UTC))) {
                    // Remove timeout by setting the timeout end to the past
                    RestAction<Void> restAction = member.timeoutFor(1L, TimeUnit.SECONDS);
                    TextChannel staffChannel = event.getGuild().getTextChannelById(configManager.getProperty("staff_logging_channel_id"));

                    restAction.queue(
                            success -> {
                                // Send a message to the staff channel
                                if (staffChannel != null) {
                                    staffChannel.sendMessage("Member " + member.getEffectiveName() + " has been unmuted.").queue();
                                } else {
                                    System.err.println("Staff channel not found.");
                                }

                                // Reply to the command sender
                                event.getHook().editOriginal("Timeout removed successfully.").queue();
                            },
                            error -> {
                                // Reply to the command sender in case of an error
                                event.getHook().editOriginal("Failed to remove timeout: " + error.getMessage()).queue();
                            }
                    );
                } else {
                    System.out.println("User is not currently timed out.");
                }
                break;
            }
            case "unban": {
                event.reply("Working on it...").setEphemeral(true).queue();
                Long userId = event.getOption("user") != null ? event.getOption("user").getAsLong() : null;
                if (userId == null) {
                    System.err.println("UserId is null");
                    return;
                }

                Guild guild = event.getGuild();
                if (guild == null) {
                    System.err.println("Guild is null");
                    return;
                }

                // Proceed to unban the user
                guild.unban(UserSnowflake.fromId(userId)).queue(
                        success -> {
                            TextChannel staffChannel = guild.getTextChannelById(configManager.getProperty("staff_logging_channel_id"));

                            event.getHook().editOriginal("User with ID" + userId + " has been unbanned.").queue();
                            if (staffChannel != null) {
                                staffChannel.sendMessage("User with ID " + userId + " has been unbanned.").queue();
                            } else {
                                System.err.println("Staff channel is null");
                            }
                        },
                        error -> {
                            TextChannel staffChannel = guild.getTextChannelById(configManager.getProperty("staff_logging_channel_id"));
                            if (staffChannel != null) {
                                event.getHook().editOriginal("Failed to remove ban: " + error.getMessage()).queue();
                            } else {
                                System.err.println("Staff channel is null");
                            }
                        }
                );
                break;
            }
            case "ban": {
                Long memberId = event.getOption("member") != null ? event.getOption("member").getAsLong() : null;
                System.out.println(memberId);

                if (memberId == null) {
                    System.err.println("Member ID is null");
                    return;
                }

                Guild guild = event.getGuild();
                if (guild == null) {
                    System.err.println("Guild is null");
                    return;
                }

                // Proceed with banning
                TextChannel staffChannel = guild.getTextChannelById(configManager.getProperty("staff_logging_channel_id"));
                guild.retrieveMemberById(memberId).queue(
                        member -> {
                            // Successful retrieval
                            System.out.println("Successfully retrieved member: " + member.getUser().getAsTag());
                            guild.ban(member, 0, TimeUnit.DAYS).queue(
                                    success -> {
                                        event.reply("User " + member.getUser().getAsTag() + " has been banned successfully.").setEphemeral(true).queue();
                                        staffChannel.sendMessage("User " + member.getUser().getAsTag() + " has been banned successfully.").queue();
                                    },
                                    error -> event.reply("Failed to ban user: " + error.getMessage()).setEphemeral(true).queue()
                            );
                        },
                        error -> {
                            // Handle the error
                            System.err.println("Failed to retrieve member: " + error.getMessage());
                            event.reply("Failed to retrieve member: " + error.getMessage()).setEphemeral(true).queue();
                        }
                );
                break;
            }
            case "mute": {
                event.reply("Working on it...").setEphemeral(true).queue();

                Member member = event.getOption("member") != null ? event.getOption("member").getAsMember() : null;
                String durationString = event.getOption("duration") != null ? event.getOption("duration").getAsString() : null;

                if (member != null && durationString != null) {
                    try {
                        Duration duration = parse(durationString);
                        String formattedDuration = format(duration);
                        // Apply the timeout (mute) for the calculated duration
                        member.timeoutFor(duration).queue(
                                success -> {
                                    TextChannel staffChannel = event.getGuild().getTextChannelById("1215793613225721938");
                                    event.getHook().editOriginal("User " + member.getEffectiveName() + " has been muted for " + formattedDuration + ".").queue();
                                    if (staffChannel != null) {
                                        staffChannel.sendMessage("User " + member.getEffectiveName() + " has been muted for " + formattedDuration + ".").queue();
                                    }
                                },
                                failure -> {
                                    TextChannel staffChannel = event.getGuild().getTextChannelById("1215793613225721938");
                                    event.getHook().editOriginal("Failed to mute user: " + failure.getMessage()).queue();
                                    if (staffChannel != null) {
                                        staffChannel.sendMessage("Failed to mute user: " + failure.getMessage()).queue();
                                    }
                                }
                        );

                        // Update the database or perform any other required actions
                        dbManager.resetWarnings(member.getId());
                        dbManager.recordInfraction(member.getId(), "mute");

                    } catch (IllegalArgumentException e) {
                        event.reply("Invalid duration format. Use formats like `5m`, `1h`, `2d`, etc.").setEphemeral(true).queue();
                    }
                } else {
                    event.reply("Member or duration not specified.").setEphemeral(true).queue();
                }
                break;
            }// Handle other cases
            case "tempban": {
                event.reply("Processing your request...").setEphemeral(true).queue();

                Member member = event.getOption("member") != null ? event.getOption("member").getAsMember() : null;
                String durationString = event.getOption("duration") != null ? event.getOption("duration").getAsString() : null;
                System.out.println("Member: " + member + "duration: " + durationString);
                if (member != null && durationString != null) {
                    Duration duration;
                    try {
                        duration = parse(durationString); // Parse the duration string
                    } catch (Exception e) {
                        event.getHook().sendMessage("Invalid duration format. Please use a format like '5m', '1h', or '3d'.").queue();
                        return;
                    }

                    Instant unbanTime = Instant.now().plus(duration);

                    // Ban the member
                    member.ban(0, TimeUnit.DAYS).queue(
                            success -> {
                                // Record to database
                                dbManager.recordDiscordTempBan(member.getId(), unbanTime);

                                // Notify staff channel
                                TextChannel staffChannel = member.getGuild().getTextChannelById("1215793613225721938"); // Replace with your staff channel ID
                                if (staffChannel != null) {
                                    staffChannel.sendMessage("User " + member.getEffectiveName() + " has been banned temporarily until " + unbanTime).queue();
                                }
                            },
                            failure -> event.getHook().sendMessage("Failed to ban user: " + failure.getMessage()).queue()
                    );
                } else {
                    event.getHook().sendMessage("Invalid member or duration.").queue();
                }


                break;
            }
            case "execute": {
                event.reply("Working on it...").setEphemeral(true).queue();
                String command = event.getOption("command").getAsString();

                // Validate the command if needed
                if (command == null || command.isEmpty()) {
                    event.reply("Invalid command. Please provide a valid Minecraft command.").setEphemeral(true).queue();
                    return;
                }

                // Execute the command on the main server thread
                Bukkit.getScheduler().runTask(Bukkit.getPluginManager().getPlugin("messaging"), () -> {
                    ConsoleCommandSender consoleSender = Bukkit.getServer().getConsoleSender();
                    Bukkit.getServer().dispatchCommand(consoleSender, command);

                    // Acknowledge the command execution to the user
                    event.getHook().editOriginal("Command executed: `" + command + "`").queue();
                });
            }
            case "removeinfractions": {
                event.reply("Working on it...").setEphemeral(true).queue();
                User user = event.getOption("user").getAsUser();

                dbManager.clearUserInfractions(user.getId());
            }
            default:
                return;

        }
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

    public void startUnbanCheckTask() {
        scheduler.scheduleAtFixedRate(() -> {
            checkDiscordUnbans();
        }, 0, 10, TimeUnit.MINUTES);
    }


    private void checkDiscordUnbans() {
        ConfigManager configManager = arbiter.getConfigManager();
        BotClass bot = getInstance(); // Ensure getInstance() returns the correct botClass instance

        if (bot.getJda() == null) {
            System.err.println("JDA instance is not set.");
            return;
        }

        try {
            List<TempBanRecord> records = dbManager.getUnbannedDiscordUsers();
            for (TempBanRecord record : records) {
                String userId = record.getUserId();
                Guild guild = bot.getJda().getGuildById(configManager.getProperty("server_id"));
                UserSnowflake userSnowflake = UserSnowflake.fromId(userId);
                if (guild == null) {
                    System.err.println("Guild not found.");
                    continue;
                }

                // Unban user directly by userId
                guild.unban(userSnowflake).queue(
                        success -> {
                            System.out.println("Successfully unbanned user: " + userId);
                            dbManager.removeDiscordTempBan(userId); // Remove the record from the database after unbanning
                        },
                        failure -> System.err.println("Failed to unban user: " + userId + ". Error: " + failure.getMessage())
                );
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public String format(Duration duration) {
        long seconds = duration.getSeconds();
        long days = seconds / (24 * 3600);
        seconds %= (24 * 3600);
        long hours = seconds / 3600;
        seconds %= 3600;
        long minutes = seconds / 60;
        seconds %= 60;

        StringBuilder builder = new StringBuilder();
        if (days > 0) builder.append(days).append("d ");
        if (hours > 0) builder.append(hours).append("h ");
        if (minutes > 0) builder.append(minutes).append("m ");
        if (seconds > 0) builder.append(seconds).append("s");

        return builder.toString().trim();
    }
}
