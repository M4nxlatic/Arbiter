package com.Manxlatic.arbiter.Ranks;

import com.Manxlatic.arbiter.Arbiter;
import com.Manxlatic.arbiter.Managers.ColourManager;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.permissions.PermissionAttachment;

public class RankListener implements Listener {

    private Arbiter arbiter;

    public RankListener(Arbiter arbiter) {
        this.arbiter = arbiter;
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent e) {
        Player player = e.getPlayer();

        if (!player.hasPlayedBefore()) {
            arbiter.getDbManager().Setrank(false, player.getUniqueId(), Ranks.TRAINER.name());
            arbiter.getDbManager().Setrank(true, player.getUniqueId(), StaffRanks.PLAYER.name());
            System.out.println("set rank & staffrank for player at ln 30 RankListener.java");
        } else if (arbiter.getDbManager().GetStaffRank(player.getUniqueId()) == null) {
            arbiter.getDbManager().Setrank(true, player.getUniqueId(), StaffRanks.PLAYER.name());
            System.out.println("set rank for player at ln 32 RankListener.java");
        } else if (arbiter.getDbManager().GetRank(player.getUniqueId()) == null) {
            arbiter.getDbManager().Setrank(false, player.getUniqueId(), Ranks.TRAINER.name());
            System.out.println("set staffrank for player at ln 34 RankListener.java");
        }

        if (arbiter.getDbManager().GetRank(player.getUniqueId()) == null) {
            System.out.println("RANK IS NULL");
        }

        if (arbiter.getDbManager().GetStaffRank(player.getUniqueId()) == null) {
            System.out.println("STAFFRANK IS NULL");
        }


        arbiter.getNameTagManager().setNameTag(player);
        arbiter.getNameTagManager().newNameTag(player);

        // Attach base rank permissions
        PermissionAttachment attachment = player.addAttachment(arbiter);
        arbiter.getRankManager().getPerms().put(player.getUniqueId(), attachment);
        for (String perm : arbiter.getDbManager().GetRank(player.getUniqueId()).getPermissions()) {
            attachment.setPermission(perm, true);
        }

        // Attach staff rank permissions
        PermissionAttachment staffAttachment = player.addAttachment(arbiter);
        arbiter.getRankManager().getStaffPerms().put(player.getUniqueId(), staffAttachment);
        for (String staffPerm : arbiter.getDbManager().GetStaffRank(player.getUniqueId()).getStaffPermissions()) {
            staffAttachment.setPermission(staffPerm, true);
        }

    }

    @EventHandler
    public void onQuit(PlayerQuitEvent e) {
        arbiter.getNameTagManager().removeNameTag(e.getPlayer());
    }

    @EventHandler
    public void onChat(AsyncPlayerChatEvent e) {
        Player player = e.getPlayer();
        if (arbiter.getDbManager().getMutes().containsKey(player.getUniqueId().toString())) {
            if (arbiter.getDbManager().getMutes().containsValue("TEMPMUTE")) {
                e.setCancelled(true);
                player.sendMessage("You are currently muted");

            } else if (arbiter.getDbManager().getMutes().containsValue("MUTE")) {
                e.setCancelled(true);
                player.sendMessage("You are permanently muted");
            }
        } else {
            e.setCancelled(true);
            String worldName = player.getWorld().getName();
            if (arbiter.getDbManager().GetStaffRank(player.getUniqueId()) == StaffRanks.PLAYER) {
                Bukkit.broadcastMessage(ColourManager.replaceFormat("[" + worldName + "] " + "[" + arbiter.getDbManager().GetRank(player.getUniqueId()).getDisplay() + ChatColor.WHITE + "] " + player.getName() + ChatColor.WHITE + " -> " + e.getMessage()));
            } else {
                Bukkit.broadcastMessage(ColourManager.replaceFormat("[" + worldName + "] " + "[" + arbiter.getDbManager().GetStaffRank(player.getUniqueId()).getDisplay() + ChatColor.WHITE + "] " + player.getName() + ChatColor.WHITE + " -> " + e.getMessage()));
            }
        }
        System.out.println("sent message because id isnt in MuteMap");
    }
}