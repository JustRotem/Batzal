package net.justrotem.data.utils;

import net.justrotem.data.PlayerManager;
import net.justrotem.data.hooks.LuckPermsManager;
import net.justrotem.data.sql.MySQL;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.*;
import java.util.logging.Level;

public abstract class Utility {

    public static void initialize(JavaPlugin plugin) {}

    public static void shutdown(JavaPlugin plugin) {}

    /**
     * Should have 'Debug: true/false' in config.yml
     * @return whether Loggers warnings will be shown or not.
     */
    public static boolean isDebug(JavaPlugin plugin) {
        return plugin.getConfig().getBoolean("Debug");
    }

    public static void saveResource(JavaPlugin plugin, String resourcePath, boolean replace) {
        if (resourcePath == null || resourcePath.equals("")) {
            throw new IllegalArgumentException("ResourcePath cannot be null or empty");
        }

        resourcePath = resourcePath.replace('\\', '/');
        InputStream in = plugin.getResource(resourcePath);
        if (in == null) {
            throw new IllegalArgumentException("The embedded resource '" + resourcePath + "' cannot be found");
        }

        File outFile = new File(plugin.getDataFolder(), resourcePath);
        int lastIndex = resourcePath.lastIndexOf('/');
        File outDir = new File(plugin.getDataFolder(), resourcePath.substring(0, lastIndex >= 0 ? lastIndex : 0));

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
            plugin.getLogger().log(Level.SEVERE, "Could not save " + outFile.getName() + " to " + outFile, ex);
        }
    }
}
