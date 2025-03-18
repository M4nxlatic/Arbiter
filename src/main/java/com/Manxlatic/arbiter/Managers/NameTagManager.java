package com.Manxlatic.arbiter.Managers;

import com.Manxlatic.arbiter.Arbiter;
import com.Manxlatic.arbiter.Ranks.Ranks;
import com.Manxlatic.arbiter.Ranks.StaffRanks;
import com.Manxlatic.arbiter.commands.FlyCommand;
import com.Manxlatic.arbiter.commands.Ranks.SidebarCommand;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.*;

import java.text.DecimalFormat;
import java.util.Objects;
import java.util.UUID;
import java.util.logging.Logger;


public class NameTagManager {

    private final Arbiter arbiter;

    public NameTagManager(Arbiter arbiter) {
        this.arbiter = arbiter;
    }

    public void setNameTag(Player player) {

        player.setScoreboard(Bukkit.getScoreboardManager().getNewScoreboard());

        SidebarCommand sidebarCommand = new SidebarCommand(arbiter.getDbManager(), arbiter);

        // Create an individual scoreboard for the player's sidebar
        Scoreboard scoreboard = Bukkit.getScoreboardManager().getNewScoreboard();

        for (OfflinePlayer target : Bukkit.getOfflinePlayers()) {
            scoreboard.resetScores(target);
        }

        for (Ranks ranks : Ranks.values()) {
            Team team = player.getScoreboard().registerNewTeam(ranks.name());
            team.setPrefix(ColourManager.replaceFormat(ranks.getDisplay()));
        }
        for (StaffRanks staffRanks : StaffRanks.values()) {
            if (staffRanks != StaffRanks.PLAYER) {
                Team team = player.getScoreboard().registerNewTeam(staffRanks.name());
                team.setPrefix(ColourManager.replaceFormat(staffRanks.getDisplay()));
            }
        }



        // Register a sidebar objective for the player's unique scoreboard
        Objective obj = scoreboard.registerNewObjective("Pulse-Pixel", "dummy");
        if (obj == null) {
            obj = scoreboard.registerNewObjective("Pulse-Pixel", "dummy");
        }



        // Set the display name of the sidebar
        obj.setDisplayName(ColourManager.replaceFormat(
                "&#1F90E9&lP&#145ADA&lu&#0824CA&ll&#041FA8&ls&#0824CA&le&#001986&l-&#1F90E9&lP&#1975E1&li&#145ADA&lx&#0E3FD2&le&#0824CA&ll"));

        // Configure sidebar contents (specific to the player)
        configureSidebarContents(player, obj, scoreboard);

        // Assign this sidebar scoreboard to the player
        if (sidebarCommand.isSidebarDisabled(player.getUniqueId())) {
            obj.setDisplaySlot(null); // Hide sidebar if disabled
        } else {
            obj.setDisplaySlot(DisplaySlot.SIDEBAR);
            player.setScoreboard(scoreboard);
        }



    }

    private void configureSidebarContents(Player player, Objective sidebarObjective, Scoreboard scoreboard) {
        String displayRank = arbiter.getDbManager().GetStaffRank(player.getUniqueId()) == StaffRanks.PLAYER
                ? arbiter.getDbManager().GetRank(player.getUniqueId()).getDisplay()
                : arbiter.getDbManager().GetStaffRank(player.getUniqueId()).getDisplay();

        Team rankscore = scoreboard.registerNewTeam("rankies");
        rankscore.addEntry(ChatColor.RED.toString());
        rankscore.setPrefix(ColourManager.replaceFormat("&#145ADA\u25CF &#145ADAR&#0E3FD2a&#0824CAn&#145ADAk&#1F90E9: "));
        rankscore.setSuffix(ColourManager.replaceFormat(displayRank));
        sidebarObjective.getScore(ChatColor.RED.toString()).setScore(13);

        Team website = scoreboard.registerNewTeam("website");
        website.addEntry(ChatColor.BLUE.toString());
        website.setPrefix(ColourManager.replaceFormat("&#084CFB\u25CF &#1054FBp&#195DFBl&#2165FBa&#296DFBy&#3176FC.&#3A7EFCp&#4286FCu&#4A8FFCl&#5297FCs&#5BA0FCe&#63A8FC-&#6BB0FCp&#73B9FCi&#7CC1FCx&#84C9FDe&#8CD2FDl&#94DAFD.&#9DE2FDn&#A5EBFDe&#ADF3FDt"));
        website.setSuffix(" ");
        sidebarObjective.getScore(ChatColor.BLUE.toString()).setScore(1);

        Team info = scoreboard.registerNewTeam("INFO");
        info.addEntry(ChatColor.GREEN.toString());
        info.setPrefix(ColourManager.replaceFormat("&#145ADA&lI&#0E3FD2&lN&#0824CA&lF&#145ADA&lO&#1F90E9&l:"));
        info.setSuffix(" ");
        sidebarObjective.getScore(ChatColor.GREEN.toString()).setScore(14);

        if (arbiter.getDbManager().GetStaffRank(player.getUniqueId()) == StaffRanks.PLAYER) {
            sidebarObjective.getScore("        ").setScore(12);
        } else {
            String canFly = "";
            if (player.getAllowFlight() || player.getGameMode().equals(GameMode.CREATIVE)) {
                canFly = ChatColor.GREEN + " Enabled";
            } else if (!(player.getAllowFlight()) || player.getGameMode().equals(GameMode.SURVIVAL)) {
                canFly = ChatColor.RED + " Disabled";
            }


            Team fly = scoreboard.registerNewTeam("fly");
            fly.addEntry(ChatColor.AQUA.toString());
            fly.setPrefix(ColourManager.replaceFormat("&#145ADA\u25CF &#145ADAF&#0E3FD2l&#0824CAy&#1F90E9:"));
            fly.setSuffix(canFly);
            sidebarObjective.getScore(ChatColor.AQUA.toString()).setScore(11);
        }

        sidebarObjective.getScore("").setScore(15);
        sidebarObjective.getScore(" ").setScore(11);
        sidebarObjective.getScore("  ").setScore(10);
        sidebarObjective.getScore("   ").setScore(9);
        sidebarObjective.getScore("    ").setScore(8);
        sidebarObjective.getScore("     ").setScore(7);
        sidebarObjective.getScore("      ").setScore(6);
        sidebarObjective.getScore("       ").setScore(5);


    }

    public void newNameTag(Player player) {
        Ranks playerRanks = arbiter.getDbManager().GetRank(player.getUniqueId());
        StaffRanks staffRanks = arbiter.getDbManager().GetStaffRank(player.getUniqueId());
        String playerName = player.getName();

        Team team;
        if (staffRanks == StaffRanks.PLAYER) {
            team = player.getScoreboard().getTeam(staffRanks.getWeight() +playerRanks.name());
            if (team == null) {
                team = player.getScoreboard().registerNewTeam(staffRanks.getWeight() + playerRanks.name());
                team.setPrefix(ColourManager.replaceFormat("[" + playerRanks.getDisplay() + ChatColor.WHITE + "] "));
            }
        } else {
            team = player.getScoreboard().getTeam(staffRanks.name());
            if (team == null) {
                team = player.getScoreboard().registerNewTeam(staffRanks.getWeight() + staffRanks.name());
                team.setPrefix("[" + ColourManager.replaceFormat(staffRanks.getDisplay()  + ChatColor.WHITE + "] "));
            }
        }

        team.addEntry(playerName);

        for (Player onlinePlayer : Bukkit.getOnlinePlayers()) {
            if (onlinePlayer.equals(player)) continue; // Skip self

            Scoreboard onlineScoreboard = player.getScoreboard(); // Use the joining player's scoreboard

            // Get the existing player's rank
            Ranks onlineRank = arbiter.getDbManager().GetRank(onlinePlayer.getUniqueId());
            StaffRanks onlineStaffRank = arbiter.getDbManager().GetStaffRank(onlinePlayer.getUniqueId());

            Team onlineTeam;
            if (onlineStaffRank == StaffRanks.PLAYER) {
                onlineTeam = onlineScoreboard.getTeam(onlineRank.name());
                if (onlineTeam == null) {
                    onlineTeam = onlineScoreboard.registerNewTeam(onlineRank.getWeight() + onlineRank.name());
                    onlineTeam.setPrefix(ColourManager.replaceFormat("[" + onlineRank.getDisplay() + ChatColor.WHITE + "] "));
                }
            } else {
                onlineTeam = onlineScoreboard.getTeam(onlineStaffRank.name());
                if (onlineTeam == null) {
                    onlineTeam = onlineScoreboard.registerNewTeam(onlineStaffRank.getWeight() + onlineStaffRank.name());
                    onlineTeam.setPrefix(ColourManager.replaceFormat("[" + onlineStaffRank.getDisplay() + ChatColor.WHITE + "] "));
                }
            }

            onlineTeam.addEntry(onlinePlayer.getName()); // Add existing player to their rank team

            // Now, update the existing player's scoreboard with the new player
            Scoreboard existingPlayerScoreboard = onlinePlayer.getScoreboard();

            Team newPlayerTeam = existingPlayerScoreboard.getTeam(team.getName());
            if (newPlayerTeam == null) {
                newPlayerTeam = existingPlayerScoreboard.registerNewTeam(team.getName());
                newPlayerTeam.setPrefix(team.getPrefix());
            }

            newPlayerTeam.addEntry(playerName); // Add the new player to existing players' scoreboards
        }
    }


    public void removeNameTag(Player player) {
        Ranks playerRanks = arbiter.getDbManager().GetRank(player.getUniqueId());
        StaffRanks staffRanks = arbiter.getDbManager().GetStaffRank(player.getUniqueId());
        String playerName = player.getName();

        if (staffRanks == StaffRanks.PLAYER) {
            Team team = player.getScoreboard().getTeam(playerRanks.name());
                if (team.hasEntry(playerName)) {
                    team.removeEntry(playerName);
                }
        } else {
            Team team = player.getScoreboard().getTeam(staffRanks.name());
            if (team.hasEntry(playerName)) {
                team.removeEntry(playerName);
            }
        }

    }

    public Boolean removeScoreboard(Scoreboard scoreboard) {
        Objective obj = scoreboard.getObjective("Pulse-Pixel");
        if (obj != null) {
            obj.unregister();
            return true;
        }
        return false;
    }
}



/*public void setNameTag(Player player) {
        // Check if the player already has their own scoreboard
        Scoreboard scoreboard = player.getScoreboard();
        Objective obj = scoreboard.getObjective("Pulse-Pixel");

        // Unregister the previous objective if it exists
        if (obj != null) {
            obj.unregister();
        }

        // Register a new objective specifically for this player
        obj = scoreboard.registerNewObjective("Pulse-Pixel", "dummy");
        obj.setDisplayName(ColourManager.replaceFormat("&#1F90E9&lP&#145ADA&lu&#0824CA&ll&#041FA8&ls&#0824CA&le&#001986&l-&#1F90E9&lP&#1975E1&li&#145ADA&lx&#0E3FD2&le&#0824CA&ll"));

        // Get the player's rank and set the display
        String displayRank;
        if (arbiter.getDbManager().GetStaffRank(player.getUniqueId()) == StaffRanks.PLAYER) {
            Ranks playerRank = arbiter.getDbManager().GetRank(player.getUniqueId());
            displayRank = playerRank.getDisplay();
        } else {
            StaffRanks staffRank = arbiter.getDbManager().GetStaffRank(player.getUniqueId());
            displayRank = staffRank.getDisplay();
        }

        // Register the rank team
        Team rankscore = scoreboard.getTeam("ranks");
        if (rankscore == null) {
            rankscore = scoreboard.registerNewTeam("ranks");
        }

        // Set the player in their rank's team
        rankscore.addEntry(player.getName()); // Make sure the player is added to the team
        rankscore.setPrefix(ColourManager.replaceFormat("&#145ADA\u25CF &#145ADAR&#0E3FD2a&#0824CAn&#145ADAk&#1F90E9: "));
        rankscore.setSuffix(displayRank); // Set the rank as the suffix
        obj.getScore(player.getName()).setScore(13); // Place the player's rank in the scoreboard

        // Add static information (info, website, etc.) to the scoreboard for this player
        addStaticTeams(scoreboard, obj);

        // Manage sidebar visibility based on whether it is disabled for this player
        SidebarCommand sidebarCommand = new SidebarCommand(new DbManager(arbiter), arbiter);
        if (!sidebarCommand.isSidebarDisabled(player.getUniqueId())) {
            obj.setDisplaySlot(DisplaySlot.SIDEBAR);  // Show the sidebar only for this player
        } else {
            obj.setDisplaySlot(null);  // Hide the sidebar if it's disabled
        }

        // Apply the updated scoreboard to the player
        player.setScoreboard(scoreboard);
    }

    // This method adds static information like 'info' and 'website' to the player's scoreboard
    private void addStaticTeams(Scoreboard scoreboard, Objective obj) {
        Team info = scoreboard.getTeam("INFO");
        if (info == null) {
            info = scoreboard.registerNewTeam("INFO");
        }
        info.addEntry(ChatColor.GREEN.toString());
        info.setPrefix(ColourManager.replaceFormat("&#145ADA&lI&#0E3FD2&lN&#0824CA&lF&#145ADA&lO&#1F90E9&l:"));
        info.setSuffix(" ");
        obj.getScore(ChatColor.GREEN.toString()).setScore(14);

        Team website = scoreboard.getTeam("website");
        if (website == null) {
            website = scoreboard.registerNewTeam("website");
        }
        website.addEntry(ChatColor.BLUE.toString());
        website.setPrefix(ColourManager.replaceFormat("&#084CFB\u25CF &#1054FBp&#195DFBl&#2165FBa&#296DFBy&#3176FC.&#3A7EFCp&#4286FCu&#4A8FFCl&#5297FCs&#5BA0FCe&#63A8FC-&#6BB0FCp&#73B9FCi&#7CC1FCx&#84C9FDe&#8CD2FDl&#94DAFD.&#9DE2FDn&#A5EBFDe&#ADF3FDt"));
        website.setSuffix(" ");
        obj.getScore(ChatColor.BLUE.toString()).setScore(1);
    } */






    /*public void setNameTag(Player player) {
        player.setScoreboard(Bukkit.getScoreboardManager().getNewScoreboard());
        if (main.getRankManager().getStaffRank(player.getUniqueId()) == StaffRanks.PLAYER) {
            for (Ranks ranks : Ranks.values()) {
                Team team = player.getScoreboard().registerNewTeam(ranks.name());
                team.setPrefix("[" + ranks.getDisplay() + ChatColor.WHITE + "] ");
            }
            return;
        } else {
            for (StaffRanks staffRanks : StaffRanks.values()) {
                Team team = player.getScoreboard().registerNewTeam(staffRanks.name());
                team.setPrefix("[" + staffRanks.getDisplay() + ChatColor.WHITE + "] ");
            }
        }
        for (Player target : Bukkit.getOnlinePlayers()) {
            if (player.getUniqueId() != target.getUniqueId()) {
                player.getScoreboard().getTeam(main.getRankManager().getRank(target.getUniqueId()).name()).addEntry(target.getName());
            }
        }
    }*/
