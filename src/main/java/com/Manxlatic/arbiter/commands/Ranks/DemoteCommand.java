package com.Manxlatic.arbiter.commands.Ranks;


import com.Manxlatic.arbiter.Arbiter;
import com.Manxlatic.arbiter.Ranks.Ranks;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

public class DemoteCommand implements CommandExecutor {

    private Arbiter arbiter;

    private Ranks ranks;

    public DemoteCommand(Arbiter arbiter){
        this.arbiter = arbiter;
    }
    @Override
    public boolean onCommand(CommandSender commandSender, Command command, String s, String[] strings) {


        return false;
    }
}
