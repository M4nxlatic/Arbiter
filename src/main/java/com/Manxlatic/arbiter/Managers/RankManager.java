package com.Manxlatic.arbiter.Managers;


import com.Manxlatic.arbiter.Arbiter;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.permissions.PermissionAttachment;

import java.io.File;
import java.util.HashMap;
import java.util.UUID;

public class RankManager {

    private File file;
    private YamlConfiguration config;

    private final Arbiter arbiter;

    private final DbManager dbManager;


    public final HashMap<UUID, PermissionAttachment> perms = new HashMap<>();

    private HashMap<UUID, PermissionAttachment> staffPerms = new HashMap<>();

    public RankManager(Arbiter arbiter, DbManager dbManager) {
        this.arbiter = arbiter;
        this.dbManager = dbManager;
    }

   /* public void setRank(UUID uuid, Ranks rank, Boolean firstJoin) {
        if (Bukkit.getOfflinePlayer(uuid).isOnline() && !firstJoin) {
            Player player = Bukkit.getPlayer(uuid);
            PermissionAttachment attachment;
            if (perms.containsKey(uuid)) {
                attachment = perms.get(uuid);
            } else {
                attachment = player.addAttachment(main);
                perms.put(uuid, attachment);
            }
            for (String perm : getRank(uuid).getPermissions()) {
                if (player.hasPermission(perm)) {
                    attachment.unsetPermission(perm);
                }
            }
            for (String perm : rank.getPermissions()) {
                attachment.setPermission(perm, true);
            }
        }

        config.set("PLAYER" + uuid.toString(), rank.name());
        main.getDbManager().Setrank(false, uuid, rank.name());
        //dbManager.Setrank(false, uuid, rank.name());
        try {
            config.save(file);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void setStaffRank(UUID uuid, StaffRanks staffRanks, Boolean firstJoin) {
        if (Bukkit.getOfflinePlayer(uuid).isOnline() && !firstJoin) {
            Player player = Bukkit.getPlayer(uuid);
            PermissionAttachment staffAttachment;
            if (staffPerms.containsKey(uuid)) {
                staffAttachment = staffPerms.get(uuid);
            } else {
                staffAttachment = player.addAttachment(main);
                staffPerms.put(uuid, staffAttachment);
            }
            for (String staffPerm : getStaffRank(uuid).getStaffPermissions()) {
                if (player.hasPermission(staffPerm)) {
                    staffAttachment.unsetPermission(staffPerm);
                }
            }
            for (String staffPerm : staffRanks.getStaffPermissions()) {
                staffAttachment.setPermission(staffPerm, true);
            }
        }


        config.set("STAFF" + uuid.toString(), staffRanks.name());
        dbManager.Setrank(true, uuid, staffRanks.name());
        try {
            config.save(file);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }



    public Ranks getRank(UUID uuid) {
        String rankValue = config.getString("PLAYER" + uuid.toString());

        if (rankValue == null) {
            // Handle case where rankValue is null (key does not exist in config or value is null)
            return Ranks.TRAINER; // Or return a default rank if appropriate
        } else {
            try {
                return Ranks.valueOf(rankValue); // Ensure rankValue is not null here
            } catch (IllegalArgumentException e) {
                // Handle case where rankValue is not a valid enum constant
                // Log the error or handle as needed
                return Ranks.TRAINER; // Or return a default rank if appropriate
            }
        }
    }

    public StaffRanks getStaffRank(UUID uuid) {
        String rankValue = config.getString("STAFF" + uuid.toString());

        if (rankValue == null) {
            // Return the default rank if rankValue is null
            return StaffRanks.PLAYER; // Assuming "." corresponds to StaffRanks.PLAYER
        } else {
            try {
                return StaffRanks.valueOf(rankValue); // Ensure rankValue is not null here
            } catch (IllegalArgumentException e) {
                // Handle case where rankValue is not a valid enum constant
                return StaffRanks.PLAYER; // Return default rank or handle as needed
            }
        }
    }

    public String getPermissions(UUID uuid) {
        Ranks rank = getRank(uuid);
        // Add logic to retrieve permissions based on rank if needed
        return "Permissions for rank: " + rank.getDisplay();
    }*/

    public HashMap<UUID, PermissionAttachment> getPerms() { return perms; }
    public HashMap<UUID, PermissionAttachment>  getStaffPerms() { return staffPerms; }
}

