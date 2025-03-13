package com.Manxlatic.arbiter;

import com.Manxlatic.arbiter.Bot.BotClass;
import com.Manxlatic.arbiter.Bot.MinecraftListener;
import com.Manxlatic.arbiter.Bot.SlashCommandListener;
import com.Manxlatic.arbiter.Managers.ConfigManager;
import com.Manxlatic.arbiter.Managers.DbManager;
import com.Manxlatic.arbiter.commands.*;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public final class Arbiter extends JavaPlugin {

    private List<UUID> frozenPlayers = new ArrayList<>();

    @Override
    public void onEnable() {

        getConfigManager().loadConfig();

        try {
            Thread.sleep(500);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }

        getConfigManager().loadConfig();

        BotClass botClass = new BotClass(this);
        botClass.start();


        getCommand("gmc").setExecutor(new GmcCommand());
        getCommand("gms").setExecutor(new GmsCommand());
        getCommand("freeze").setExecutor(new FreezeCommand(this));
        getCommand("unfreeze").setExecutor(new UnFreezeCommand(this));
        getCommand("speed").setExecutor(new SpeedCommand());
        getCommand("invsee").setExecutor(new InvSeeCommand());
        getCommand("editinv").setExecutor(new EditInventoryCommand());



        getServer().getPluginManager().registerEvents(new FreezeCommand(this), this);
        getServer().getPluginManager().registerEvents(new InvSeeCommand(), this);
        getServer().getPluginManager().registerEvents(new EditInventoryCommand(), this);
        try {
            getServer().getPluginManager().registerEvents(new MinecraftListener(this, new DbManager(this), botClass.getJda()), this);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }


    }

    public List<UUID> getFrozenPlayers() {
        return frozenPlayers;
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
    }

    public ConfigManager getConfigManager() {
        System.out.println("getConfigManager() called from: " + Thread.currentThread().getStackTrace()[2]);
        return new ConfigManager(this);
    }
}
