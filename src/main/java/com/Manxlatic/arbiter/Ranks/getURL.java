package com.Manxlatic.arbiter.Ranks;

import com.Manxlatic.arbiter.Arbiter;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

public class getURL implements CommandExecutor {

    private Arbiter arbiter;

    public getURL(Arbiter arbiter){
        this.arbiter = arbiter;
    }

    @Override
    public boolean onCommand(CommandSender commandSender, Command command, String s, String[] strings) {



        return false;
    }
}
