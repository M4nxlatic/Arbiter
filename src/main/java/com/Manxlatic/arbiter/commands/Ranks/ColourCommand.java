package com.Manxlatic.arbiter.commands.Ranks;


import com.Manxlatic.arbiter.Managers.ColourManager;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

public class ColourCommand implements CommandExecutor {
    @Override
    public boolean onCommand(CommandSender commandSender, Command command, String s, String[] args) {

        if (!(args.length == 2)) {
            commandSender.sendMessage("wrong, try again");
        }

        String hexC1 = args[0];

        String hexC2 = args[1];

        Bukkit.broadcastMessage(ColourManager.BuildColorString(hexC1, hexC2, commandSender.getName()));
        return true;
    }
}
