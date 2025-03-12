package com.Manxlatic.arbiter;

import com.Manxlatic.arbiter.commands.FreezeCommand;
import com.Manxlatic.arbiter.commands.GmcCommand;
import com.Manxlatic.arbiter.commands.GmsCommand;
import com.Manxlatic.arbiter.commands.SpeedCommand;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;
import java.util.List;

public final class Arbiter extends JavaPlugin {

    private List<Player> frozenPlayers = new ArrayList<>();

    @Override
    public void onEnable() {
        getCommand("gmc").setExecutor(new GmcCommand());
        getCommand("gms").setExecutor(new GmsCommand());
        getCommand("freeze").setExecutor(new FreezeCommand(this));
        getCommand("unfreeze").setExecutor(new FreezeCommand(this));
        getCommand("speed").setExecutor(new SpeedCommand());



        getServer().getPluginManager().registerEvents(new FreezeCommand(this), this);
    }

    public List<Player> getFrozenPlayers() {
        return frozenPlayers;
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
    }
}
