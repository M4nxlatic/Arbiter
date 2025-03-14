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
    private static String username = "root";
    private static String password = "";

    private static String url1 = "jdbc:mysqlite:punishments.db";

    public DbManager(Arbiter arbiter) throws IOException {
        this.arbiter = arbiter;

        configFile = new File(arbiter.getDataFolder(), "punishments.db");

        // Load the configuration. If the file doesn't exist, create a default one.
        if (!configFile.exists()) {
            if (configFile.createNewFile()) {
                System.out.println("Created default config.properties file.");
                createTablesIfNotExist();
            }
        }


    }

    public void createTablesIfNotExist() {
        String infractionsTable = "CREATE TABLE IF NOT EXISTS infractions (" +
                "id INTEGER PRIMARY KEY AUTO_INCREMENT, " +
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
                "id INTEGER PRIMARY KEY AUTO_INCREMENT, " +
                "playerid VARCHAR(255) NOT NULL, " +
                "type VARCHAR(50) NOT NULL, " +
                "end_time DATETIME" +
                ");";

        Connection connection = null;
        Statement statement = null;

        try {
            // Load the MySQL driver
            Class.forName("com.mysql.cj.jdbc.Driver");

            // Establish the connection
            connection = DriverManager.getConnection(url1, username, password);
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
    public void recordInfraction(String userId, String type) {
        if (!type.equals("warning") && !type.equals("mute") && !type.equals("ban")) {
            throw new IllegalArgumentException("Invalid infraction type: " + type);
        }

        Connection connection = null;
        PreparedStatement preparedStatement = null;
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            connection = DriverManager.getConnection(url1, username, password);

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

        try (Connection connection = DriverManager.getConnection(url1, username, password);
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
            Class.forName("com.mysql.cj.jdbc.Driver");
            connection = DriverManager.getConnection(url1, username, password);
            Statement statement = connection.createStatement();
            String sqlQuery = "DELETE FROM Infractions WHERE user_id = '" + userId + "'";
            statement.executeUpdate(sqlQuery);
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

    public void resetWarnings(String userId) {
        String sqlQuery = "DELETE FROM infractions WHERE user_id = ? AND type = 'warning'";

        try (Connection connection = DriverManager.getConnection(url1, username, password);
             PreparedStatement statement = connection.prepareStatement(sqlQuery)) {

            statement.setString(1, userId);
            statement.executeUpdate();

            System.out.println("Warnings reset for user: " + userId);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }


    public void insertUser(long userId, String Username) throws SQLException {
        Connection connection = null;
        String sqlQuery = "INSERT INTO users (user_id, username) VALUES (?, ?)";

        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            connection = DriverManager.getConnection(url1, username, password);
            try (PreparedStatement statement = connection.prepareStatement(sqlQuery)) {
                statement.setLong(1, userId);
                statement.setString(2, Username);
                statement.executeUpdate();
            } catch (SQLException e) {
                e.printStackTrace();
                throw e;
            }
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
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

    public void deleteUser(long userId) throws SQLException {
        Connection connection = null;
        String sqlQuery = "DELETE FROM users WHERE user_id = " + userId;

        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            connection = DriverManager.getConnection(url1, username, password);
            Statement statement = connection.createStatement();
            statement.execute(sqlQuery);
        } catch (SQLException | ClassNotFoundException e) {
            e.printStackTrace();
            try {
                throw e;
            } catch (ClassNotFoundException ex) {
                throw new RuntimeException(ex);
            } finally {
                try {
                    if (connection != null) {
                        connection.close();
                    }
                } catch (SQLException exc) {
                    e.printStackTrace();
                }
            }
        }

    }

    public void recordDiscordTempBan(String userId, Instant unbanTime) {
        Connection connection = null;
        String sql = "INSERT INTO temp_bans (user_id, unban_time) VALUES (?, ?) ON DUPLICATE KEY UPDATE unban_time = VALUES(unban_time)";
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            connection = DriverManager.getConnection(url1, username, password);
            PreparedStatement pstmt = connection.prepareStatement(sql);
            pstmt.setString(1, userId);
            pstmt.setTimestamp(2, Timestamp.from(unbanTime));
            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
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

    public List<TempBanRecord> getUnbannedDiscordUsers() {
        Connection connection = null;
        List<TempBanRecord> records = new ArrayList<>();
        String sql = "SELECT user_id, unban_time FROM temp_bans WHERE unban_time <= ?";
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            connection = DriverManager.getConnection(url1, username, password);
            PreparedStatement pstmt = connection.prepareStatement(sql);
            pstmt.setTimestamp(1, Timestamp.from(Instant.now()));
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                String userId = rs.getString("user_id");
                Instant unbanTime = rs.getTimestamp("unban_time").toInstant();
                records.add(new TempBanRecord(userId, unbanTime));
                System.out.println("Retrieved Record: " + userId + " " + unbanTime);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
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


    public void removeDiscordTempBan(String userId) {
        Connection connection = null;
        String sql = "DELETE FROM temp_bans WHERE user_id = ?";
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            connection = DriverManager.getConnection(url1, username, password);
            PreparedStatement pstmt = connection.prepareStatement(sql);
            pstmt.setString(1, userId);
            int rowsAffected = pstmt.executeUpdate();
            if (rowsAffected > 0) {
                System.out.println("Removed temp ban for user: " + userId);
            } else {
                System.err.println("Failed to remove temp ban for user: " + userId + ". No rows affected.");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
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


    //MINECRAFT PUNISHMENTS

    private static String url2 = "jdbc:mysql://localhost:3306/punishments";

    public void recordGameTempMute(String playerId, Instant unmuteTime) {
        Connection connection = null;
        // Format Instant to SQL TIMESTAMP format
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss").withZone(ZoneId.systemDefault());
        String formattedUnmuteTime = formatter.format(unmuteTime);

        // Construct the SQL statement
        String sql = "INSERT INTO punishments (playerid, type, end_time) VALUES ('"
                + playerId + "', 'TEMPMUTE', '" + formattedUnmuteTime + "')";

        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            connection = DriverManager.getConnection(url2, username, password);
            Statement statement = connection.createStatement();
            statement.execute(sql);
        } catch (SQLException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
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

    public void recordGameMute(String playerId, Instant unmuteTime) {
        Connection connection = null;
        // Format Instant to SQL TIMESTAMP format
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss").withZone(ZoneId.systemDefault());
        String formattedUnmuteTime = formatter.format(unmuteTime);

        // Construct the SQL statement
        String sql = "INSERT INTO punishments (playerid, type, end_time) VALUES ('"
                + playerId + "', 'MUTE', '" + formattedUnmuteTime + "')";

        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            connection = DriverManager.getConnection(url2, username, password);
            Statement statement = connection.createStatement();
            statement.execute(sql);
        } catch (SQLException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
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

    public Map<String, String> getBans() {
        Connection connection = null;
        Map<String, String> records = new HashMap<>();
        String sql = "SELECT playerid, type FROM punishments WHERE type = 'BAN' OR type = 'TEMPBAN'";
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            connection = DriverManager.getConnection(url2, username, password);
            Statement statement = connection.createStatement();
            ResultSet rs = statement.executeQuery(sql);
            while (rs.next()) {
                String userId = rs.getString("playerid");
                String type = rs.getString("type");
                records.put(userId, type);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
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

    public void recordGameTempBan(String playerId, Instant unbanTime) {
        Connection connection = null;
        // Format Instant to SQL TIMESTAMP format
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss").withZone(ZoneId.systemDefault());
        String formattedUnbanTime = formatter.format(unbanTime);

        // Construct the SQL statement
        String sql = "INSERT INTO punishments (playerid, type, end_time) VALUES ('"
                + playerId + "', 'TEMPBAN', '" + formattedUnbanTime + "')";

        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            connection = DriverManager.getConnection(url2, username, password);
            Statement statement = connection.createStatement();
            statement.execute(sql);
        } catch (SQLException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
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

    public void recordGameBan(String playerId, Instant unbanTime) {
        Connection connection = null;
        // Format Instant to SQL TIMESTAMP format
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss").withZone(ZoneId.systemDefault());
        String formattedUnbanTime = formatter.format(unbanTime);

        // Construct the SQL statement
        String sql = "INSERT INTO punishments (playerid, type, end_time) VALUES ('"
                + playerId + "', 'BAN', '" + formattedUnbanTime + "')";

        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            connection = DriverManager.getConnection(url2, username, password);
            Statement statement = connection.createStatement();
            statement.execute(sql);
        } catch (SQLException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
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

    public Map<String, String> getMutes() {
        Connection connection = null;
        Map<String, String> records = new HashMap<>();
        String sql = "SELECT playerid, type FROM punishments WHERE type = 'MUTE' OR type = 'TEMPMUTE'";
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            connection = DriverManager.getConnection(url2, username, password);
            Statement statement = connection.createStatement();
            ResultSet rs = statement.executeQuery(sql);
            while (rs.next()) {
                String userId = rs.getString("playerid");
                String type = rs.getString("type");
                records.put(userId, type);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
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

    public List<String> getUnbannedGameUsers() {
        Connection connection = null;
        List<String> records = new ArrayList<>();
        String sql = "SELECT playerid FROM punishments WHERE type = 'TEMPBAN' AND end_time <= ?";
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            connection = DriverManager.getConnection(url2, username, password);

            PreparedStatement preparedStatement = connection.prepareStatement(sql);
            // Set the current timestamp as a parameter
            preparedStatement.setTimestamp(1, Timestamp.from(Instant.now()));

            ResultSet rs = preparedStatement.executeQuery();
            while (rs.next()) {
                String userId = rs.getString("playerid");
                records.add(userId);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
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



    public List<String> getUnmutedGameUsers() {
        Connection connection = null;
        List<String> records = new ArrayList<>();
        String sql = "SELECT playerid FROM punishments WHERE type = 'TEMPMUTE' AND end_time <= ?";
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            connection = DriverManager.getConnection(url2, username, password);

            PreparedStatement preparedStatement = connection.prepareStatement(sql);
            // Set the current timestamp as a parameter
            preparedStatement.setTimestamp(1, Timestamp.from(Instant.now()));

            ResultSet rs = preparedStatement.executeQuery();
            while (rs.next()) {
                String userId = rs.getString("playerid");
                records.add(userId);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
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

    public void removeGameTempMutes(String playerId) {
        Connection connection = null;
        String sql = "DELETE FROM punishments WHERE type = 'TEMPMUTE' AND playerid = ?";
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            connection = DriverManager.getConnection(url2, username, password);
            PreparedStatement pstmt = connection.prepareStatement(sql);
            pstmt.setString(1, playerId);
            int rowsAffected = pstmt.executeUpdate();
            if (rowsAffected > 0) {
                System.out.println("Removed tempmute for user: " + playerId);
            } else {
                System.err.println("Failed to remove tempmute for user: " + playerId + ". No rows affected.");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
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

    public void removeGameMutes(String playerId) {
        Connection connection = null;
        String sql = "DELETE FROM punishments WHERE type = 'MUTE' AND playerid = '" + playerId + "'";
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            connection = DriverManager.getConnection(url2, username, password);
            PreparedStatement pstmt = connection.prepareStatement(sql);
            pstmt.setString(1, playerId);
            int rowsAffected = pstmt.executeUpdate();
            if (rowsAffected > 0) {
                System.out.println("Removed tempmute for user: " + playerId);
            } else {
                System.err.println("Failed to remove tempmute for user: " + playerId + ". No rows affected.");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
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
}
