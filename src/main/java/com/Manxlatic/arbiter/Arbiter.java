package com.Manxlatic.arbiter;

import com.Manxlatic.arbiter.commands.*;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public final class Arbiter extends JavaPlugin {

    private List<UUID> frozenPlayers = new ArrayList<>();

    @Override
    public void onEnable() {
        getCommand("gmc").setExecutor(new GmcCommand());
        getCommand("gms").setExecutor(new GmsCommand());
        getCommand("freeze").setExecutor(new FreezeCommand(this));
        getCommand("unfreeze").setExecutor(new UnFreezeCommand(this));
        getCommand("speed").setExecutor(new SpeedCommand());



        getServer().getPluginManager().registerEvents(new FreezeCommand(this), this);
    }

    public List<UUID> getFrozenPlayers() {
        return frozenPlayers;
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
    }
}
