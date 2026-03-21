package net.justrotem.lobby;

import net.justrotem.data.cache.PlayerManager;
import net.justrotem.data.cache.LuckPermsManager;
import net.justrotem.data.model.PlayerData;
import net.justrotem.data.util.TextUtility;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.*;

public class BukkitPlayerManager extends PlayerManager {

    public static void register(Player player) {
        PlayerData playerData = PlayerManager.get(player.getUniqueId());
        if (playerData == null) playerData = BukkitPlayerData.create(player);

        PlayerManager.update(BukkitPlayerData.checkForUpdates(player, playerData));
    }

    public static void startAutoSave(JavaPlugin plugin) {
        Bukkit.getScheduler().runTaskTimerAsynchronously(plugin, () -> {
            for (UUID uuid : PlayerManager.CACHE.keySet()) {
                PlayerData playerData = PlayerManager.CACHE.get(uuid);
                if (!playerData.isDirty()) continue;

                PlayerManager.sql.update(playerData);
                playerData.setDirty(false);
            }
        }, 0, 20 * 30); // every 30 seconds
    }

    //<editor-fold desc="Bukkit methods">
    public static boolean isRegisteredOffline(String name) {
        try {
            return Arrays.stream(Bukkit.getOfflinePlayers())
                    .anyMatch(p -> p.getName() != null && p.getName().equalsIgnoreCase(name));
        } catch (Exception e) {
            return false;
        }
    }

    public static boolean isRegistered(String name) {
        return PlayerManager.isRegistered(getUniqueId(name));
    }

    public static UUID getUniqueId(String name) {
        try {
            try {
                return Objects.requireNonNull(Arrays.stream(Bukkit.getOfflinePlayers()).filter(p -> p.getName() != null && p.getName().equalsIgnoreCase(name)).findFirst().orElse(null)).getUniqueId();
            } catch (NoSuchElementException | NullPointerException e) {
                return Objects.requireNonNull(PlayerManager.CACHE.values().stream().filter(playerData -> playerData.getName().equalsIgnoreCase(name)).findFirst().orElse(null)).getUniqueId();
            }
        } catch (NullPointerException e) {
            return null;
        }
    }

    public static String getName(String name) {
        try {
            return Objects.requireNonNull(PlayerManager.getName(getUniqueId(name)));
        } catch (NullPointerException e) {
            return null;
        }
    }

    public static Component getDisplayName(Player player) {
        return LuckPermsManager.getPrefix(player.getUniqueId()).append(player.displayName());
    }

    public static String getLegacyRealDisplayName(UUID uuid) {
        return LuckPermsManager.getLegacyGroupPrefix(LuckPermsManager.getPrimaryGroup(uuid)) + PlayerManager.getName(uuid);
    }

    public static String getLegacyRealDisplayName(Player player) {
        return LuckPermsManager.getLegacyGroupPrefix(LuckPermsManager.getPrimaryGroup(player.getUniqueId())) + player.getName();
    }

    public static String getLegacyDisplayName(Player player) {
        return LuckPermsManager.getLegacyPrefix(player.getUniqueId()) + TextUtility.getText(player.displayName());
    }
    //</editor-fold>
}
