package net.justrotem.lobby.vanish;

import net.justrotem.data.player.PlayerData;
import net.justrotem.lobby.Main;
import net.justrotem.lobby.hooks.PlayerManager;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.Objects;
import java.util.UUID;

public class VanishManager extends net.justrotem.data.VanishManager {

    private static void setVanished(Player player, boolean vanished) {
        PlayerManager.get(player.getUniqueId()).setVanished(vanished);
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

    public static boolean isInvisible(Player player) {
        return isInvisible(player.getUniqueId());
    }

    public static boolean canSee(CommandSender viewer, Player viewed) {
        return canSee(!(viewer instanceof Player player) || player.hasPermission("batzal.vanish.see"), viewed != null ? viewed.getUniqueId() : null);
    }

    public static boolean canSee(CommandSender viewer) {
        return canSee(viewer, null);
    }

    public static List<UUID> getAllVanishedPlayers() {
        return PlayerManager.getAll().stream().filter(PlayerData::isVanished).map(PlayerData::getUniqueId).toList();
    }

    public static List<Player> getOnlineVanishedPlayers() {
        return getAllVanishedPlayers().stream().map(Bukkit::getPlayer).filter(Objects::nonNull).toList();
    }

    public static List<UUID> getOfflineVanishedPlayers() {
        return getAllVanishedPlayers().stream().filter(uuid -> Bukkit.getPlayer(uuid) == null).toList();
    }

    public static void updateVanishedPlayers(Player player) {
        if (isInvisible(player)) hidePlayer(player);

        getOnlineVanishedPlayers().forEach(target -> {
            if (target == player) return;
            if (isInvisible(target)) hidePlayer(target);
        });
    }
}
