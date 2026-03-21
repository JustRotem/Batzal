package net.justrotem.lobby.utils;

import org.bukkit.plugin.java.JavaPlugin;

import java.io.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public class WorldResetService {

    public static void resetWorldOnLoad(JavaPlugin plugin) {
        String zipName = plugin.getConfig().getString("backup-world", "Backup World.zip");

        File worldFolder = new File("world");
        File zipFile = new File(zipName);

        plugin.getLogger().info("Running onLoad(): worlds are NOT loaded yet.");

        // 1. Delete old world folder
        if (worldFolder.exists()) {
            plugin.getLogger().info("Deleting world folder: " + worldFolder.getPath());
            deleteFolder(worldFolder);
        }

        // 2. Extract backup world
        if (zipFile.exists()) {
            plugin.getLogger().info("Extracting from zip: " + zipFile.getName());
            unzip(zipFile, new File("world")); // extract into world directory
            File playerDataFolder = new File("world/playerdata");
            if (!playerDataFolder.exists()) {
                playerDataFolder.mkdirs();
            }
        } else {
            plugin.getLogger().severe("ERROR: backup zip missing: " + zipFile.getPath());
        }

        plugin.getLogger().info("World reset complete BEFORE world load.");
    }

    private static void deleteFolder(File file) {
        if (!file.exists()) return;
        File[] children = file.listFiles();
        if (children != null) {
            for (File f : children) deleteFolder(f);
        }
        file.delete();
    }

    private static void unzip(File zipFile, File dest) {
        try (ZipInputStream zis = new ZipInputStream(new FileInputStream(zipFile))) {
            ZipEntry entry;
            while ((entry = zis.getNextEntry()) != null) {
                File newFile = new File(dest, entry.getName());
                if (entry.isDirectory()) {
                    newFile.mkdirs();
                } else {
                    newFile.getParentFile().mkdirs();
                    try (FileOutputStream fos = new FileOutputStream(newFile)) {
                        zis.transferTo(fos);
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
