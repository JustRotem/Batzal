package net.justrotem.game.vanish;

import net.justrotem.data.PlayerData;
import net.justrotem.data.PlayerManager;
import net.justrotem.game.Main;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.*;

public class VanishManager {

    private static void setVanished(Player player, boolean vanished) {
        PlayerManager.updatePlayer(player, PlayerManager.getData(player).setVanished(vanished));
    }

    public static boolean isVanished(Player player) {
        return PlayerManager.getData(player).isVanished();
    }

    public static void hidePlayer(Player player) {
        Bukkit.getOnlinePlayers().stream()
                .filter(p -> !p.equals(player))
                .filter(p -> !p.hasPermission("batzal.vanish.see"))
                .forEach(p -> p.hidePlayer(Main.getInstance(), player));

        setVanished(player, true);
    }

    public static void showPlayer(Player player) {
        Bukkit.getOnlinePlayers().stream()
                .filter(p -> !p.equals(player))
                .forEach(p -> p.showPlayer(Main.getInstance(), player));

        setVanished(player, false);
    }

    public static List<UUID> getAllVanishedPlayers() {
        return PlayerManager.getAllPlayers().stream().filter(PlayerData::isVanished).map(PlayerData::getUniqueId).toList();
    }

    public static List<Player> getOnlineVanishedPlayers() {
        return getAllVanishedPlayers().stream().map(Bukkit::getPlayer).filter(Objects::nonNull).toList();
    }

    public static List<UUID> getOfflineVanishedPlayers() {
        return getAllVanishedPlayers().stream().filter(uuid -> Bukkit.getPlayer(uuid) == null).toList();
    }

    public static void updateVanishedPlayers() {
        getOnlineVanishedPlayers().stream().filter(player -> player.hasPermission("batzal.vanish.ingame")).forEach(VanishManager::hidePlayer);
    }
}
