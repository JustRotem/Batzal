package net.justrotem.proxy.utils;

import com.velocitypowered.api.plugin.PluginContainer;
import org.slf4j.Logger;

import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Properties;

public class ConfigManager {

    private final Path dataFolder;
    private final Logger logger;
    private Properties config;

    public ConfigManager(PluginContainer container, Logger logger) {
        this.dataFolder = container.getDataDirectory();
        this.logger = logger;
    }

    public void saveDefaultConfig() {
        try {
            if (!Files.exists(dataFolder)) {
                Files.createDirectories(dataFolder);
            }

            Path configPath = dataFolder.resolve("config.yml");

            if (!Files.exists(configPath)) {
                try (InputStream in = getClass().getClassLoader().getResourceAsStream("config.yml")) {
                    if (in == null) {
                        logger.error("Default config.yml not found inside JAR!");
                        return;
                    }

                    Files.copy(in, configPath);
                    logger.info("Created default config.yml");
                }
            } else {
                logger.info("config.yml already exists");
            }
        } catch (Exception e) {
            logger.error("Failed to save default config.yml", e);
        }
    }

    public String readRaw() {
        Path configPath = dataFolder.resolve("config.yml");

        try {
            return Files.readString(configPath);
        } catch (IOException e) {
            logger.error("Failed to read config.yml", e);
            return "";
        }
    }
}
