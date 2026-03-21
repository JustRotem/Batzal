package net.justrotem.data.cache;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class ToggleManager {
    // Stores per-category toggles for each player
    private static final Map<Enum<?>, Map<UUID, Boolean>> toggles = new HashMap<>();

    /**
     * Check if a player's toggle is ON for a given category.
     * Automatically registers the player if not present.
     *
     * @param category The toggle category (can be command name, e.g., "FIREWORK")
     * @param uuid   The unique id of the player
     * @return true if toggle is ON, false otherwise
     */
    public static boolean isOn(Enum<?> category, UUID uuid) {
        Map<UUID, Boolean> map = toggles.computeIfAbsent(category, c -> new HashMap<>());

        return map.computeIfAbsent(uuid, p -> false);
    }

    /**
     * Toggle a player's state for a category.
     * Automatically registers the player if not present.
     *
     * @param category The toggle category
     * @param uuid     The unique id of the player
     * @return The new state after toggling
     */
    public static boolean toggle(Enum<?> category, UUID uuid) {
        Map<UUID, Boolean> map = toggles.computeIfAbsent(category, c -> new HashMap<>());

        boolean newState = !map.getOrDefault(uuid, false);
        map.put(uuid, newState);
        return newState;
    }

    /**
     * Manually set a toggle for a player and category
     *
     * @param category The toggle category
     * @param uuid     The unique id of the player
     * @param value    true = ON, false = OFF
     */
    public static void set(Enum<?> category, UUID uuid, boolean value) {
        Map<UUID, Boolean> map = toggles.computeIfAbsent(category, c -> new HashMap<>());
        map.put(uuid, value);
    }

    /**
     * Remove a player from all toggles (e.g., on logout)
     */
    public static void removePlayer(UUID uuid) {
        for (Map<UUID, Boolean> map : toggles.values()) {
            map.remove(uuid);
        }
    }

    public static List<UUID> getAll(Enum<?> category, boolean on) {
        Map<UUID, Boolean> map = toggles.computeIfAbsent(category, c -> new HashMap<>());
        return map.keySet().stream().filter(uuid -> map.get(uuid) == on).toList();
    }
}
