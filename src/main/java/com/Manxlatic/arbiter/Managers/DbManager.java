package com.Manxlatic.arbiter.Managers;

import com.Manxlatic.arbiter.Arbiter;
import com.Manxlatic.arbiter.Bot.TempBanRecord;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.permissions.PermissionAttachment;
import org.bukkit.plugin.Plugin;

import java.io.File;
import java.io.IOException;
import java.sql.*;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.*;

public class DbManager {

    private final File configFile;
    private final Arbiter arbiter;
    private static String url1;

    public DbManager(Arbiter arbiter) {
        this.arbiter = arbiter;

         url1 = "jdbc:sqlite:" + arbiter.getDataFolder() + File.separator + "punishments.db";

        configFile = new File(arbiter.getDataFolder(), "punishments.db");

        try {
            // Load the configuration. If the file doesn't exist, create a default one.
            if (!configFile.exists()) {
                if (configFile.createNewFile()) {
                    System.out.println("Created default config.properties file.");
                    createTablesIfNotExist();
                }
            }
        } catch (IOException e) {
            // Handle the IOException internally
            System.err.println("An error occurred while creating the database file: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public void createTablesIfNotExist() {
        String infractionsTable = "CREATE TABLE IF NOT EXISTS infractions (" +
                "id INTEGER PRIMARY KEY, " +  // Changed AUTO_INCREMENT to INTEGER PRIMARY KEY
                "user_id VARCHAR(255) NOT NULL, " +
                "type VARCHAR(50) NOT NULL, " +
                "timestamp DATETIME NOT NULL" +
                ");";

        String usersTable = "CREATE TABLE IF NOT EXISTS users (" +
                "user_id BIGINT PRIMARY KEY, " +
                "username VARCHAR(255) NOT NULL" +
                ");";

        String tempBansTable = "CREATE TABLE IF NOT EXISTS temp_bans (" +
                "user_id VARCHAR(255) PRIMARY KEY, " +
                "unban_time DATETIME NOT NULL" +
                ");";

        String punishmentsTable = "CREATE TABLE IF NOT EXISTS punishments (" +
                "id INTEGER PRIMARY KEY, " +  // Changed AUTO_INCREMENT to INTEGER PRIMARY KEY
                "playerid VARCHAR(255) NOT NULL, " +
                "type VARCHAR(50) NOT NULL, " +
                "end_time DATETIME" +
                ");";

        Connection connection = null;
        Statement statement = null;

        try {
            // Load the SQLite driver
            Class.forName("org.sqlite.JDBC");

            // Establish the connection (no username and password needed for SQLite)
            connection = DriverManager.getConnection(url1);
            statement = connection.createStatement();

            // Execute the CREATE TABLE queries
            statement.execute(infractionsTable);
            statement.execute(usersTable);
            statement.execute(tempBansTable);
            statement.execute(punishmentsTable);

            System.out.println("Tables created or verified successfully.");
        } catch (SQLException | ClassNotFoundException e) {
            e.printStackTrace();
            throw new RuntimeException("Failed to create tables", e);
        } finally {
            // Close resources in the finally block
            try {
                if (statement != null) {
                    statement.close();
                }
                if (connection != null) {
                    connection.close();
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }


    // Record an infraction with an expiration timestamp
    // Record an infraction with an expiration timestamp
    public void recordInfraction(String userId, String type) {
        if (!type.equals("warning") && !type.equals("mute") && !type.equals("ban")) {
            throw new IllegalArgumentException("Invalid infraction type: " + type);
        }

        Connection connection = null;
        PreparedStatement preparedStatement = null;
        try {
            Class.forName("org.sqlite.JDBC");  // SQLite JDBC Driver
            connection = DriverManager.getConnection(url1);  // SQLite does not require username/password

            long currentTimestamp = System.currentTimeMillis();
            OffsetDateTime now = OffsetDateTime.now(ZoneOffset.UTC);

            String sqlQuery = "INSERT INTO infractions (user_id, type, timestamp) VALUES (?, ?, ?)";

            preparedStatement = connection.prepareStatement(sqlQuery);
            preparedStatement.setString(1, userId);
            preparedStatement.setString(2, type);
            preparedStatement.setString(3, now.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));

            preparedStatement.executeUpdate();

        } catch (SQLException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        } finally {
            try {
                if (preparedStatement != null) {
                    preparedStatement.close();
                }
                if (connection != null) {
                    connection.close();
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    // Count active infractions for a user
    public int countUserInfractions(String userId, String type) {
        int count = 0;
        String sqlQuery = "SELECT COUNT(*) FROM infractions WHERE user_id = ? AND type = ?";

        try (Connection connection = DriverManager.getConnection(url1);  // SQLite does not require username/password
             PreparedStatement statement = connection.prepareStatement(sqlQuery)) {

            statement.setString(1, userId);
            statement.setString(2, type);

            ResultSet resultSet = statement.executeQuery();
            if (resultSet.next()) {
                count = resultSet.getInt(1);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return count;
    }

    // Clear all infractions for a user
    public void clearUserInfractions(String userId) {
        Connection connection = null;
        try {
            Class.forName("org.sqlite.JDBC");  // SQLite JDBC Driver
            connection = DriverManager.getConnection(url1);  // SQLite does not require username/password
            Statement statement = connection.createStatement();
            String sqlQuery = "DELETE FROM infractions WHERE user_id = ?";  // Updated to use PreparedStatement
            PreparedStatement preparedStatement = connection.prepareStatement(sqlQuery);
            preparedStatement.setString(1, userId);
            preparedStatement.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } finally {
            try {
                if (connection != null) connection.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }


    // Reset warnings for a user
    public void resetWarnings(String userId) {
        String sqlQuery = "DELETE FROM infractions WHERE user_id = ? AND type = 'warning'";

        try (Connection connection = DriverManager.getConnection(url1);  // SQLite does not require username/password
             PreparedStatement statement = connection.prepareStatement(sqlQuery)) {

            statement.setString(1, userId);
            statement.executeUpdate();

            System.out.println("Warnings reset for user: " + userId);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // Insert a new user into the 'users' table
    public void insertUser(long userId, String username) throws SQLException {
        Connection connection = null;
        String sqlQuery = "INSERT INTO users (user_id, username) VALUES (?, ?)";

        try {
            connection = DriverManager.getConnection(url1);  // SQLite does not require username/password
            try (PreparedStatement statement = connection.prepareStatement(sqlQuery)) {
                statement.setLong(1, userId);
                statement.setString(2, username);
                statement.executeUpdate();
            }
        } catch (SQLException e) {
            e.printStackTrace();
            throw e;
        } finally {
            try {
                if (connection != null) {
                    connection.close();
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    // Delete a user from the 'users' table
    public void deleteUser(long userId) throws SQLException {
        Connection connection = null;
        String sqlQuery = "DELETE FROM users WHERE user_id = ?";

        try {
            connection = DriverManager.getConnection(url1);  // SQLite does not require username/password
            try (PreparedStatement statement = connection.prepareStatement(sqlQuery)) {
                statement.setLong(1, userId);
                statement.executeUpdate();
            }
        } catch (SQLException e) {
            e.printStackTrace();
            throw e;
        } finally {
            try {
                if (connection != null) {
                    connection.close();
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    // Record a Discord temporary ban
    public void recordDiscordTempBan(String userId, Instant unbanTime) {
        Connection connection = null;
        String sql = "INSERT OR REPLACE INTO temp_bans (user_id, unban_time) VALUES (?, ?)";  // SQLite uses INSERT OR REPLACE

        try {
            connection = DriverManager.getConnection(url1);  // SQLite does not require username/password
            try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
                pstmt.setString(1, userId);
                pstmt.setTimestamp(2, Timestamp.from(unbanTime));
                pstmt.executeUpdate();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            try {
                if (connection != null) {
                    connection.close();
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    // Retrieve users who have been unbanned
    public List<TempBanRecord> getUnbannedDiscordUsers() {
        Connection connection = null;
        List<TempBanRecord> records = new ArrayList<>();
        String sql = "SELECT user_id, unban_time FROM temp_bans WHERE unban_time <= ?";

        try {
            connection = DriverManager.getConnection(url1);  // SQLite does not require username/password
            try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
                pstmt.setTimestamp(1, Timestamp.from(Instant.now()));
                ResultSet rs = pstmt.executeQuery();

                while (rs.next()) {
                    String userId = rs.getString("user_id");
                    Instant unbanTime = rs.getTimestamp("unban_time").toInstant();
                    records.add(new TempBanRecord(userId, unbanTime));
                    System.out.println("Retrieved Record: " + userId + " " + unbanTime);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            try {
                if (connection != null) {
                    connection.close();
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        return records;
    }



    // Remove a user's Discord temporary ban
    public void removeDiscordTempBan(String userId) {
        String sql = "DELETE FROM temp_bans WHERE user_id = ?";

        try (Connection connection = DriverManager.getConnection(url1); // SQLite does not require username/password
             PreparedStatement pstmt = connection.prepareStatement(sql)) {

            pstmt.setString(1, userId);
            int rowsAffected = pstmt.executeUpdate();
            if (rowsAffected > 0) {
                System.out.println("Removed temp ban for user: " + userId);
            } else {
                System.err.println("Failed to remove temp ban for user: " + userId + ". No rows affected.");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // Record a Minecraft temporary mute
    public void recordGameTempMute(String playerId, Instant unmuteTime) {
        Connection connection = null;
        // Format Instant to SQL TIMESTAMP format
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss").withZone(ZoneId.systemDefault());
        String formattedUnmuteTime = formatter.format(unmuteTime);

        // SQL query with parameters to avoid SQL injection
        String sql = "INSERT INTO punishments (playerid, type, end_time) VALUES (?, 'TEMPMUTE', ?)";

        try {
            connection = DriverManager.getConnection(url1); // SQLite does not require username/password
            try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
                pstmt.setString(1, playerId);
                pstmt.setString(2, formattedUnmuteTime);
                pstmt.executeUpdate();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            try {
                if (connection != null) {
                    connection.close();
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    // Record a Minecraft mute
    public void recordGameMute(String playerId, Instant unmuteTime) {
        Connection connection = null;

        // Validate or fallback to a safe date
        String formattedUnmuteTime = null;
        if (unmuteTime == null || unmuteTime.equals(Instant.MAX)) {
            // Permanent mute: Store NULL or a safe far-future date
            formattedUnmuteTime = null;
        } else {
            // Convert Instant to a valid SQL TIMESTAMP string
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss").withZone(ZoneId.systemDefault());
            formattedUnmuteTime = formatter.format(unmuteTime);
        }

        // SQL query that uses parameters to avoid SQL injection
        String sql = "INSERT INTO punishments (playerid, type, end_time) VALUES (?, 'MUTE', ?)";

        try {
            connection = DriverManager.getConnection(url1); // SQLite does not require a username/password
            try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
                pstmt.setString(1, playerId);

                // Set the correct value for the unmute time
                if (formattedUnmuteTime == null) {
                    pstmt.setNull(2, java.sql.Types.VARCHAR); // Set null for permanent mutes
                } else {
                    pstmt.setString(2, formattedUnmuteTime);
                }

                pstmt.executeUpdate();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            try {
                if (connection != null) {
                    connection.close();
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    // Get active bans from the punishments table
    public Map<String, String> getBans() {
        Connection connection = null;
        Map<String, String> records = new HashMap<>();
        String sql = "SELECT playerid, type FROM punishments WHERE type = 'BAN' OR type = 'TEMPBAN'";

        try {
            connection = DriverManager.getConnection(url1); // SQLite does not require username/password
            try (PreparedStatement pstmt = connection.prepareStatement(sql);
                 ResultSet rs = pstmt.executeQuery()) {

                while (rs.next()) {
                    String userId = rs.getString("playerid");
                    String type = rs.getString("type");
                    records.put(userId, type);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            try {
                if (connection != null) {
                    connection.close();
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        return records;
    }

    // Record a Minecraft temporary ban
    public void recordGameTempBan(String playerId, Instant unbanTime) {
        Connection connection = null;
        // Format Instant to SQL TIMESTAMP format
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss").withZone(ZoneId.systemDefault());
        String formattedUnbanTime = formatter.format(unbanTime);

        // SQL query with parameters to avoid SQL injection
        String sql = "INSERT INTO punishments (playerid, type, end_time) VALUES (?, 'TEMPBAN', ?)";

        try {
            connection = DriverManager.getConnection(url1); // SQLite does not require username/password
            try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
                pstmt.setString(1, playerId);
                pstmt.setString(2, formattedUnbanTime);
                pstmt.executeUpdate();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            try {
                if (connection != null) {
                    connection.close();
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }


    // Record a game ban
    public void recordGameBan(String playerId, Instant unbanTime) {
        // Format Instant to SQL TIMESTAMP format
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss").withZone(ZoneId.systemDefault());
        String formattedUnbanTime = formatter.format(unbanTime);

        // SQL query with parameters to avoid SQL injection
        String sql = "INSERT INTO punishments (playerid, type, end_time) VALUES (?, 'BAN', ?)";

        try (Connection connection = DriverManager.getConnection(url1); // SQLite does not require username/password
             PreparedStatement pstmt = connection.prepareStatement(sql)) {

            pstmt.setString(1, playerId);
            pstmt.setString(2, formattedUnbanTime);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // Get all active mutes
    public Map<String, String> getMutes() {
        Map<String, String> records = new HashMap<>();
        String sql = "SELECT playerid, type FROM punishments WHERE type = 'MUTE' OR type = 'TEMPMUTE'";

        try (Connection connection = DriverManager.getConnection(url1); // SQLite does not require username/password
             PreparedStatement pstmt = connection.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {

            while (rs.next()) {
                String userId = rs.getString("playerid");
                String type = rs.getString("type");
                records.put(userId, type);

            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        System.out.println(records);
        return records;
    }

    // Get all unbanned game users
    public List<String> getUnbannedGameUsers() {
        List<String> records = new ArrayList<>();
        String sql = "SELECT playerid FROM punishments WHERE type = 'TEMPBAN' AND end_time <= ?";

        try (Connection connection = DriverManager.getConnection(url1); // SQLite does not require username/password
             PreparedStatement pstmt = connection.prepareStatement(sql)) {

            pstmt.setTimestamp(1, Timestamp.from(Instant.now()));

            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    String userId = rs.getString("playerid");
                    records.add(userId);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return records;
    }

    // Get all unmuted game users
    public List<String> getUnmutedGameUsers() {
        List<String> records = new ArrayList<>();
        String sql = "SELECT playerid FROM punishments WHERE type = 'TEMPMUTE' AND end_time <= ?";

        try (Connection connection = DriverManager.getConnection(url1); // SQLite does not require username/password
             PreparedStatement pstmt = connection.prepareStatement(sql)) {

            pstmt.setTimestamp(1, Timestamp.from(Instant.now()));

            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    String userId = rs.getString("playerid");
                    records.add(userId);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return records;
    }

    // Remove a game temporary mute
    public void removeGameTempMutes(String playerId) {
        String sql = "DELETE FROM punishments WHERE type = 'TEMPMUTE' AND playerid = ?";

        try (Connection connection = DriverManager.getConnection(url1); // SQLite does not require username/password
             PreparedStatement pstmt = connection.prepareStatement(sql)) {

            pstmt.setString(1, playerId);
            int rowsAffected = pstmt.executeUpdate();
            if (rowsAffected > 0) {
                System.out.println("Removed tempmute for user: " + playerId);
            } else {
                System.err.println("Failed to remove tempmute for user: " + playerId + ". No rows affected.");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // Remove a game mute
    public void removeGameMutes(String playerId) {
        String checkSql = "SELECT * FROM punishments WHERE type = 'MUTE' AND playerid = ? AND (end_time IS NULL OR end_time > CURRENT_TIMESTAMP)";
        String deleteSql = "DELETE FROM punishments WHERE type = 'MUTE' AND playerid = ? AND (end_time IS NULL OR end_time > CURRENT_TIMESTAMP)";

        try (Connection connection = DriverManager.getConnection(url1)) {
            // Check if the player is muted
            try (PreparedStatement checkPstmt = connection.prepareStatement(checkSql)) {
                checkPstmt.setString(1, playerId);

                try (ResultSet rs = checkPstmt.executeQuery()) {
                    if (!rs.next()) {
                        System.err.println("Player " + playerId + " is not muted.");
                        return; // No need to continue if the player is not muted
                    }
                }
            }

            // Remove the mute
            try (PreparedStatement deletePstmt = connection.prepareStatement(deleteSql)) {
                deletePstmt.setString(1, playerId);

                int rowsAffected = deletePstmt.executeUpdate();
                if (rowsAffected > 0) {
                    System.out.println("Removed mute for user: " + playerId);
                } else {
                    System.err.println("Failed to remove mute for user: " + playerId + ". No rows affected.");
                }
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // Remove a game temporary ban
    public void removeGameTempBans(String playerId) {
        String sql = "DELETE FROM punishments WHERE type = 'TEMPBAN' AND playerid = ?";

        try (Connection connection = DriverManager.getConnection(url1); // SQLite does not require username/password
             PreparedStatement pstmt = connection.prepareStatement(sql)) {

            pstmt.setString(1, playerId);
            int rowsAffected = pstmt.executeUpdate();
            if (rowsAffected > 0) {
                System.out.println("Removed tempban for user: " + playerId);
            } else {
                System.err.println("Failed to remove tempban for user: " + playerId + ". No rows affected.");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // Remove a game ban
    public void removeGameBans(String playerId) {
        String deleteSql = "DELETE FROM punishments WHERE type = 'BAN' AND playerid = ?";

        try (Connection connection = DriverManager.getConnection("jdbc:sqlite:/path/to/your/database.db");
             PreparedStatement deletePstmt = connection.prepareStatement(deleteSql)) {

            // Set the playerId parameter in the SQL query
            deletePstmt.setString(1, playerId);

            // Execute the delete query
            int rowsAffected = deletePstmt.executeUpdate();
            if (rowsAffected > 0) {
                System.out.println("Removed ban for user: " + playerId);
            } else {
                System.err.println("Failed to remove ban for user: " + playerId + ". No rows affected.");
            }

        } catch (SQLException e) {
            // Log the exception for debugging purposes
            System.err.println("An error occurred while removing ban for user: " + playerId);
            e.printStackTrace();
        }
    }

}