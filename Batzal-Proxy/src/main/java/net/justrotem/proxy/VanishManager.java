package net.justrotem.proxy;

import com.velocitypowered.api.proxy.Player;
import net.justrotem.data.model.PlayerData;

import java.util.List;
import java.util.UUID;

public class VanishManager extends net.justrotem.data.cache.VanishManager {

    public static boolean isInvisible(Player player) {
        return isInvisible(player.getUniqueId());
    }

    public static boolean canSee(Player viewer, Player viewed) {
        return canSee(viewer.hasPermission("batzal.vanish.see"), viewed != null ? viewed.getUniqueId() : null);
    }

    public static boolean canSee(Player viewer) {
        return canSee(viewer, null);
    }

    public static List<UUID> getAllVanishedPlayers() {
        return VelocityPlayerManager.getAll().stream().filter(PlayerData::isVanished).map(PlayerData::getUniqueId).toList();
    }

    public static List<Player> getOnlineVanishedPlayers() {
        return getAllVanishedPlayers().stream().filter(uuid -> Main.getInstance().getProxy().getPlayer(uuid).isPresent()).map(uuid -> Main.getInstance().getProxy().getPlayer(uuid).get()).toList();
    }

    public static List<UUID> getOfflineVanishedPlayers() {
        return getAllVanishedPlayers().stream().filter(uuid -> Main.getInstance().getProxy().getPlayer(uuid).isEmpty()).toList();
    }
}
