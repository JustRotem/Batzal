package net.justrotem.data;

import net.justrotem.data.hooks.LuckPermsManager;
import net.justrotem.data.sql.MySQLConfig;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.*;
import java.util.logging.Level;

public final class Main extends JavaPlugin {

    @Override
    public void onLoad() {
        instance = this;

        // Saves config.yml if it doesn't exists
        //getLogger().info("Loading configuration!");
        saveDefaultConfig();
    }

    private MySQLConfig mySQLConfig;

    @Override
    public void onEnable() {
        if (isDebug()) getSLF4JLogger().info("".repeat(5) + " __       ___ ___            ");
        if (isDebug()) getSLF4JLogger().info("".repeat(5) + "|  )  /\\   |   /   /\\  |   ");
        if (isDebug()) getSLF4JLogger().info("".repeat(5) + "|__) /--\\  |  /__ /--\\ |___");
        if (isDebug()) getSLF4JLogger().info("".repeat(5) + "Data Library has been loaded!");
        
        // Initialize MySQLConfig
        getLogger().info("Loading MySQL!");
        mySQLConfig = new MySQLConfig(this);

        // Initialize LuckPerms safely
        LuckPermsManager.init(this);

        getServer().getPluginManager().registerEvents(new EventListeners(), this);

        if (isDebug()) getLogger().info("Enabled successfully!");
    }

    @Override
    public void onDisable() {
        if (isDebug()) getLogger().info("Starting shutdown process..");

        if (isDebug()) getLogger().info("Saving players data..");
        PlayerManager.saveAllPlayers();

        getLogger().info("Closing MySQL..");
        mySQLConfig.disconnect();

        if (isDebug()) getLogger().info("Disabled successfully :)");
    }

    private static Main instance;

    public static Main getInstance() {
        return instance;
    }

    public boolean isDebug() {
        return getConfig().getBoolean("Debugging");
    }

    public MySQLConfig getMySQLConfig() {
        return mySQLConfig;
    }

    @Override
    public void saveResource(String resourcePath, boolean replace) {
        if (resourcePath == null || resourcePath.equals("")) {
            throw new IllegalArgumentException("ResourcePath cannot be null or empty");
        }

        resourcePath = resourcePath.replace('\\', '/');
        InputStream in = getResource(resourcePath);
        if (in == null) {
            throw new IllegalArgumentException("The embedded resource '" + resourcePath + "' cannot be found");
        }

        File outFile = new File(getDataFolder(), resourcePath);
        int lastIndex = resourcePath.lastIndexOf('/');
        File outDir = new File(getDataFolder(), resourcePath.substring(0, lastIndex >= 0 ? lastIndex : 0));

        if (!outDir.exists()) {
            outDir.mkdirs();
        }

        try {
            if (!outFile.exists() || replace) {
                OutputStream out = new FileOutputStream(outFile);
                byte[] buf = new byte[1024];
                int len;
                while ((len = in.read(buf)) > 0) {
                    out.write(buf, 0, len);
                }
                out.close();
                in.close();
            }
        } catch (IOException ex) {
            getLogger().log(Level.SEVERE, "Could not save " + outFile.getName() + " to " + outFile, ex);
        }
    }
}
