package net.justrotem.data;

import net.justrotem.data.player.PlayerData;
import net.justrotem.data.player.PlayerManager;

import java.util.List;
import java.util.UUID;

public class VanishManager {

    public static boolean isInvisible(UUID uuid) {
        return PlayerManager.get(uuid).isVanished();
    }

    public static boolean canSee(boolean hasPermission, UUID viewed) {
        if (hasPermission) return true;
        if (viewed != null) return !isInvisible(viewed);

        return false;
    }

    public static boolean canSee(boolean hasPermission) {
        return canSee(hasPermission, null);
    }

    public static List<UUID> getAllVanishedPlayers() {
        return PlayerManager.getAll().stream().filter(PlayerData::isVanished).map(PlayerData::getUniqueId).toList();
    }
}
