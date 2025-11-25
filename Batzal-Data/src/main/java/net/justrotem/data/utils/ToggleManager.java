package net.justrotem.data.utils;

import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ToggleManager {
    // Stores per-category toggles for each player
    private static final Map<Enum<?>, Map<Player, Boolean>> toggles = new HashMap<>();

    /**
     * Check if a player's toggle is ON for a given category.
     * Automatically registers the player if not present.
     *
     * @param category The toggle category (can be command name, e.g., "FIREWORK")
     * @param player   The player
     * @return true if toggle is ON, false otherwise
     */
    public static boolean isOn(Enum<?> category, Player player) {
        Map<Player, Boolean> map = toggles.computeIfAbsent(category, c -> new HashMap<>());

        return map.computeIfAbsent(player, p -> false);
    }

    /**
     * Toggle a player's state for a category.
     * Automatically registers the player if not present.
     *
     * @param category The toggle category
     * @param player   The player
     * @return The new state after toggling
     */
    public static boolean toggle(Enum<?> category, Player player) {
        Map<Player, Boolean> map = toggles.computeIfAbsent(category, c -> new HashMap<>());

        boolean newState = !map.getOrDefault(player, false);
        map.put(player, newState);
        return newState;
    }

    /**
     * Manually set a toggle for a player and category
     *
     * @param category The toggle category
     * @param player   The player
     * @param value    true = ON, false = OFF
     */
    public static void set(Enum<?> category, Player player, boolean value) {
        Map<Player, Boolean> map = toggles.computeIfAbsent(category, c -> new HashMap<>());
        map.put(player, value);
    }

    /**
     * Remove a player from all toggles (e.g., on logout)
     */
    public static void removePlayer(Player player) {
        for (Map<Player, Boolean> map : toggles.values()) {
            map.remove(player);
        }
    }

    public static List<Player> getAll(Enum<?> category, boolean on) {
        Map<Player, Boolean> map = toggles.computeIfAbsent(category, c -> new HashMap<>());
        return map.keySet().stream().filter(player -> map.get(player) == on).toList();
    }
}
