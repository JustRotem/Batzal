package net.justrotem.lobby.utils;

import net.justrotem.data.player.PlayerData;
import net.justrotem.lobby.Main;
import net.justrotem.lobby.hooks.PlayerManager;
import net.justrotem.lobby.listeners.LobbyHandler;
import net.justrotem.lobby.nick.NickManager;
import net.justrotem.lobby.vanish.VanishManager;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

public class Visibility extends net.justrotem.data.data.Visibility {

    public static void show(Player player) {
        Bukkit.getOnlinePlayers().forEach(target -> {
            if (!VanishManager.canSee(player, target)) return;

            player.showPlayer(Main.getInstance(), target);
        });
        PlayerManager.get(player.getUniqueId()).setVisibilityState(State.VISIBLE);
    }

    public static void hide(Player player) {
        Bukkit.getOnlinePlayers().forEach(target -> {
            if (!VanishManager.canSee(player, target)) return;

            if (!NickManager.isLobbyNicked(target)) {
                // Check if friends: if (FriendManager.isFriend(target.getUniqueId()) return;

                // if (StaffManager.isStaff(target.getUniqueId())) return;
                if (target.hasPermission("batzal.visibility.always")) return;
            }

            player.hidePlayer(Main.getInstance(), target);
        });
        PlayerManager.get(player.getUniqueId()).setVisibilityState(State.HIDDEN);
    }

    public static void updatePlayers(Player player) {
        PlayerData playerData = PlayerManager.get(player.getUniqueId());
        if (playerData == null) return;

        State state = playerData.getVisibilityState();

        if (state == State.VISIBLE || state == State.HIDDEN) {
            if (state == State.VISIBLE) Visibility.show(player);
            else Visibility.hide(player);

            player.getInventory().setItem(7, state == State.VISIBLE ? LobbyHandler.VISIBLE_PLAYERS : LobbyHandler.HIDDEN_PLAYERS);
        }
    }
}
