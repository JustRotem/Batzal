package net.justrotem.game;

import net.justrotem.game.commands.PingCommand;
import net.justrotem.game.commands.ToggleChatCommand;
import net.justrotem.game.hooks.NickExpansion;
import net.justrotem.game.nick.NickConfig;
import net.justrotem.game.nick.NickManager;
import net.justrotem.game.nick.gui.BookManager;
import net.justrotem.game.sql.MySQLConfig;
import net.justrotem.game.vanish.VanishCommand;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.*;
import java.util.Collections;
import java.util.logging.Level;
import java.util.logging.Logger;

public final class Main extends JavaPlugin {

    @Override
    public void onLoad() {
        instance = this;

        // Saves config.yml if it doesn't exists
        getLogger().info("Loading configuration!");
        saveDefaultConfig();
    }

    private MySQLConfig mySQLConfig;

    @Override
    public void onEnable() {
        Logger logger = Logger.getLogger("");
        logger.info("".repeat(5) + " __       ___ ___            ");
        logger.info("".repeat(5) + "|  )  /\\   |   /   /\\  |   ");
        logger.info("".repeat(5) + "|__) /--\\  |  /__ /--\\ |___");
        logger.info("".repeat(5));
        
        // Initialize MySQLConfig
        getLogger().info("Loading MySQL!");
        mySQLConfig = new MySQLConfig();

        // Initialize NameConfig
        NickConfig.initialize(this);

        // Initialize PlaceholderAPI safely
        NickExpansion.init(this);

        // Initialize Nick book gui
        BookManager.init();

        // Registering Commands
        registerCommand("batzal", new BatzalCommand());
        registerCommand("vanish", Collections.singleton("v"), new VanishCommand());
        registerCommand("togglechat", new ToggleChatCommand());
        registerCommand("ping", new PingCommand());

        getServer().getPluginManager().registerEvents(new EventListeners(), this);

        getLogger().info("Enabled successfully!");
    }

    @Override
    public void onDisable() {
        getLogger().info("Starting shutdown process..");

        getLogger().info("Saving players data..");
        NickManager.saveAllPlayers();

        getLogger().info("Disabled successfully :)");
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
