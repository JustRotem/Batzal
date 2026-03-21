package net.justrotem.lobby.listeners;

import net.justrotem.data.util.TextUtility;
import net.justrotem.lobby.Main;
import net.justrotem.lobby.nick.NickManager;
import net.justrotem.lobby.vanish.VanishManager;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

public class ActionBarManager {

    public static void startActionBarTask() {
        Bukkit.getScheduler().runTaskTimer(Main.getInstance(), () -> {
            for (Player player : Bukkit.getOnlinePlayers()) {
                List<String> states = new ArrayList<>();

                if (VanishManager.isInvisible(player)) states.add("&cVANISHED");

                if (NickManager.isLobbyNicked(player) || NickManager.hasDifferentName(player)) states.add("&cNICKED");
                else if (NickManager.isNicked(player) && NickManager.canChange(player)) states.add("&cNICKED (rejoin to update)");
                else if (NickManager.isNicked(player)) states.add("&cNICKED (in games only)");

                if (states.isEmpty()) continue;

                StringBuilder state = new StringBuilder();
                for (int i = 0;  i < states.size(); i++) {
                    state.append(states.get(i));
                    if (i < states.size() - 1) state.append("&f, ");
                }

                String actionbar = "&fYou are currently %state%&f!".replace("%state%", state.toString());
                player.sendActionBar(TextUtility.color(actionbar));
            }
        }, 0L, 20L); // Runs every 20 ticks = 1 second
    }
}
