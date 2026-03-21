package net.justrotem.data.player;

import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.ProxyServer;
import net.justrotem.data.hooks.LuckPermsManager;
import net.justrotem.data.utils.TextUtility;
import net.kyori.adventure.text.Component;

import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

public class VelocityPlayerManager extends PlayerManager {

    public static void register(Player player) {
        PlayerData playerData = get(player.getUniqueId());
        if (playerData == null) /*playerData = VelocityPlayerData.create(player);*/return;

        update(VelocityPlayerData.checkForUpdates(player, playerData));
    }

    public static void startAutoSave(ProxyServer server, Object plugin) {
        server.getScheduler().buildTask(plugin, () -> {
            for (UUID uuid : CACHE.keySet()) {
                PlayerData playerData = CACHE.get(uuid);
                if (!playerData.isDirty()) continue;

                sql.update(playerData);
                playerData.setDirty(false);
            }
        }).repeat(30, TimeUnit.SECONDS).schedule(); // every 30 seconds
    }

    //<editor-fold desc="Velocity methods">
    public static boolean isRegistered(String name) {
        return isRegistered(getUniqueId(name));
    }

    public static Component getDisplayName(Player player) {
        return LuckPermsManager.getPrefix(player.getUniqueId()).append(TextUtility.color(player.getUsername()));
    }

    public static String getLegacyDisplayName(UUID uuid) {
        return LuckPermsManager.getLegacyGroupPrefix(LuckPermsManager.getPrimaryGroup(uuid)) + getName(uuid);
    }

    public static String getPrefixColor(UUID uuid) {
        String prefix = LuckPermsManager.getLegacyPrefix(uuid);

        return prefix.substring(prefix.length() - 2);
    }

    public static String getColoredName(UUID uuid) {
        return getPrefixColor(uuid) + getName(uuid);
    }
    //</editor-fold>
}
