package net.justrotem.lobby.vanish;

import net.justrotem.data.PlayerData;
import net.justrotem.data.PlayerManager;
import net.justrotem.lobby.Main;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.Objects;
import java.util.UUID;

public class VanishManager {

    private static void setVanished(Player player, boolean vanished) {
        PlayerManager.updatePlayer(player, PlayerManager.getData(player).setVanished(vanished));
    }

    public static boolean isInvisible(Player player) {
        return PlayerManager.getData(player).isVanished();
    }

    public static void hidePlayer(Player viewed) {
        setVanished(viewed, true);

        Bukkit.getOnlinePlayers().stream()
                .filter(viewer -> viewer != viewed)
                .filter(viewer -> !canSee(viewer, viewed))
                .forEach(viewer -> viewer.hidePlayer(Main.getInstance(), viewed));
    }

    public static void showPlayer(Player viewed) {
        setVanished(viewed, false);

        Bukkit.getOnlinePlayers().stream()
                .filter(viewer -> !viewer.equals(viewed))
                .forEach(viewer -> viewer.showPlayer(Main.getInstance(), viewed));
    }

    public static boolean canSee(CommandSender viewer, Player viewed) {
        if (viewer.hasPermission("batzal.vanish.see")) return true;
        if (viewed != null) return !isInvisible(viewed);

        return false;
    }

    public static boolean canSee(CommandSender viewer) {
        return canSee(viewer, null);
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
        getOnlineVanishedPlayers().forEach(VanishManager::hidePlayer);
    }
}
