package com.Manxlatic.arbiter.Managers;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Properties;
import org.bukkit.plugin.java.JavaPlugin;

public class ConfigManager {
    private final File configFile;
    private final Properties configProperties;

    public ConfigManager(JavaPlugin plugin) {
        // Define the config file location within the plugin's data folder
        configFile = new File(plugin.getDataFolder(), "config.properties");

        // Properties object to handle key-value pairs
        configProperties = new Properties();

        // Load the configuration. If the file doesn't exist, create a default one.
        if (!configFile.exists()) {
            try {
                if (configFile.getParentFile().mkdirs()) {
                    System.out.println("Created plugin data folder.");
                }
                if (configFile.createNewFile()) {
                    System.out.println("Created default config.properties file.");
                    createDefaultConfig();
                }
            } catch (IOException e) {
                System.err.println("Failed to create config file: " + e.getMessage());
            }
        }

        // Load existing configurations from the file
        loadConfig();
    }

    private void createDefaultConfig() {
        configProperties.setProperty("botToken", "your-bot-token-here");
        configProperties.setProperty("channelId1", "000000000000000000");
        configProperties.setProperty("channelId2", "000000000000000000");
        saveConfig();
    }

    public void loadConfig() {
        try (FileInputStream input = new FileInputStream(configFile)) {
            configProperties.load(input);
            System.out.println("Config loaded successfully.");
        } catch (IOException e) {
            System.err.println("Failed to load config file: " + e.getMessage());
        }
    }

    public void saveConfig() {
        try (FileOutputStream output = new FileOutputStream(configFile)) {
            configProperties.store(output, "Minecraft Plugin Configuration File");
            System.out.println("Config saved successfully.");
        } catch (IOException e) {
            System.err.println("Failed to save config file: " + e.getMessage());
        }
    }

    public String getProperty(String key) {
        return configProperties.getProperty(key);
    }

    public void setProperty(String key, String value) {
        configProperties.setProperty(key, value);
    }

    public void removeProperty(String key) {
        configProperties.remove(key);
    }

    public void printProperties() {
        configProperties.forEach((key, value) -> System.out.println(key + " = " + value));
    }
}