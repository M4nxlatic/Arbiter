package com.Manxlatic.arbiter;

import com.Manxlatic.arbiter.Bot.BotClass;
import com.Manxlatic.arbiter.Bot.MinecraftListener;
import com.Manxlatic.arbiter.Bot.SlashCommandListener;
import com.Manxlatic.arbiter.Managers.NameTagManager;
import com.Manxlatic.arbiter.Managers.ConfigManager;
import com.Manxlatic.arbiter.Managers.DbManager;
import com.Manxlatic.arbiter.Managers.RankManager;
import com.Manxlatic.arbiter.Ranks.RankListener;
import com.Manxlatic.arbiter.Ranks.Ranks;
import com.Manxlatic.arbiter.Ranks.StaffRanks;
import com.Manxlatic.arbiter.commands.*;
import com.Manxlatic.arbiter.commands.Punishment.*;
import com.Manxlatic.arbiter.commands.Ranks.*;
import org.bukkit.BanList;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public final class Arbiter extends JavaPlugin {

    private List<UUID> frozenPlayers = new ArrayList<>();
    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
    private BotClass botClass;
    private DbManager dbManager;
    private NameTagManager nameTagManager;
    private RankManager rankManager;

    @Override
    public void onEnable() {


        getConfigManager().loadConfig();

        try {
            Thread.sleep(500);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }

        getConfigManager().loadConfig();

        botClass = new BotClass(this);
        botClass.start();

        dbManager = new DbManager(this);

        nameTagManager = new NameTagManager(this);

        rankManager = new RankManager(this, dbManager);

        SlashCommandListener slashCommandListener = new SlashCommandListener(dbManager, botClass, this);
        slashCommandListener.startUnbanCheckTask();



        getCommand("gmc").setExecutor(new GmcCommand(this));
        getCommand("gms").setExecutor(new GmsCommand(this));
        getCommand("freeze").setExecutor(new FreezeCommand(this));
        getCommand("unfreeze").setExecutor(new UnFreezeCommand(this));
        getCommand("speed").setExecutor(new SpeedCommand());
        getCommand("invsee").setExecutor(new InvSeeCommand());
        getCommand("editinv").setExecutor(new EditInventoryCommand());
        getCommand("mute").setExecutor(new MuteCommand(this, dbManager, botClass.getJda()));
        getCommand("ban").setExecutor(new BanCommand(botClass.getJda(),this, dbManager));
        getCommand("tempban").setExecutor(new TempBanCommand(botClass.getJda(), this, dbManager));
        getCommand("tempmute").setExecutor(new TempMuteCommand(dbManager, this, botClass.getJda()));
        getCommand("unmute").setExecutor(new unmuteCommand(botClass.getJda(), dbManager, this));
        getCommand("unban").setExecutor(new unbanCommand(botClass.getJda(), this, dbManager));
        getCommand("punishments").setExecutor(new PunishmentGUICommand());
        getCommand("fly").setExecutor(new FlyCommand(this));
        getCommand("ranks").setExecutor(new RanksCommand());
        getCommand("setrank").setExecutor(new SetRankCommand(this));
        getCommand("setStaffRank").setExecutor(new SetStaffRankCommand(this));
        getCommand("colour").setExecutor(new ColourCommand());
        getCommand("sidebar").setExecutor(new SidebarCommand(dbManager, this));

        Ranks.setMain(this);
        StaffRanks.setMain(this);


        Bukkit.getPluginManager().registerEvents(new RankListener(this), this);
        Bukkit.getPluginManager().registerEvents(new RanksCommand(), this);




        getServer().getPluginManager().registerEvents(new FreezeCommand(this), this);
        getServer().getPluginManager().registerEvents(new InvSeeCommand(), this);
        getServer().getPluginManager().registerEvents(new EditInventoryCommand(), this);
        getServer().getPluginManager().registerEvents(new MinecraftListener(this, new DbManager(this), botClass.getJda()), this);


        startGamePunishmentCheckTask();
    }

    public List<UUID> getFrozenPlayers() {
        return frozenPlayers;
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
        BotClass botClass = new BotClass(this);
        botClass.stop();
    }

    public ConfigManager getConfigManager() {
        return new ConfigManager(this);
    }

    public void startGamePunishmentCheckTask() {
        // Combine both unban and unmute checks into a single scheduled task
        scheduler.scheduleAtFixedRate(() -> {
            try {
                checkGameUnbansAndUnmutes();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }, 0, 10, TimeUnit.SECONDS); // Runs every 10 seconds
    }

    private void checkGameUnbansAndUnmutes() {
        try {
            // Handle unban cases
            List<String> unbannedRecords = dbManager.getUnbannedGameUsers();
            processGameUnbans(unbannedRecords);

            // Handle unmute cases
            List<String> unmutedRecords = dbManager.getUnmutedGameUsers();
            processGameUnmutes(unmutedRecords);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void processGameUnbans(List<String> unbannedRecords) {
        BanList banList = Bukkit.getBanList(BanList.Type.NAME);
        for (String record : unbannedRecords) {
            try {
                UUID playerId = UUID.fromString(record);
                Player player = Bukkit.getPlayer(playerId);

                // Skip null players (player may be offline)
                if (player == null) {
                    continue;
                }

                // Batch-unban player names
                banList.pardon(player.getName());
            } catch (IllegalArgumentException | NullPointerException e) {
                System.err.println("Invalid unban record: " + record);
            }
        }
    }

    private void processGameUnmutes(List<String> unmutedRecords) {
        for (String record : unmutedRecords) {
            try {
                UUID playerId = UUID.fromString(record);

                // Remove temporary mutes from the database (batch operation)
                dbManager.removeGameTempMutes(playerId.toString());
            } catch (IllegalArgumentException e) {
                System.err.println("Invalid unmute record: " + record);
            }
        }
    }

    public DbManager getDbManager() {
        return dbManager;
    }

    public NameTagManager getNameTagManager() {
        return nameTagManager;
    }

    public RankManager getRankManager() {
        return rankManager;
    }
}
