package net.justrotem.data.cache;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * A generic toggle manager that stores boolean states per player (UUID) and per category.
 *
 * <p>This class is designed to be flexible and reusable across different modules (e.g. Lobby, Games),
 * allowing each module to define its own enum categories without coupling to a shared enum.</p>
 *
 * <p>Thread-safe: Uses {@link java.util.concurrent.ConcurrentHashMap} internally.</p>
 *
 * <p>Example usage:
 * <pre>
 *     ToggleManager.toggle(MyToggle.FIREWORKS, playerUUID);
 *     boolean enabled = ToggleManager.isOn(MyToggle.FIREWORKS, playerUUID);
 * </pre>
 * </p>
 */
public final class ToggleManager {

    private static final Map<Enum<?>, Map<UUID, Boolean>> TOGGLES = new ConcurrentHashMap<>();

    private ToggleManager() {
    }

    /**
     * Checks whether a toggle is enabled for a given player and category.
     *
     * @param category The toggle category (must not be null)
     * @param uuid     The player's UUID (must not be null)
     * @return true if enabled, false otherwise (default = false)
     */
    public static boolean isOn(Enum<?> category, UUID uuid) {
        if (category == null || uuid == null) return false;

        return TOGGLES
                .computeIfAbsent(category, key -> new ConcurrentHashMap<>())
                .getOrDefault(uuid, false);
    }

    /**
     * Toggles the current state for a given player and category.
     *
     * @param category The toggle category
     * @param uuid     The player's UUID
     * @return The new state after toggling
     */
    public static boolean toggle(Enum<?> category, UUID uuid) {
        if (category == null || uuid == null) return false;

        Map<UUID, Boolean> map = TOGGLES.computeIfAbsent(category, key -> new ConcurrentHashMap<>());
        boolean newState = !map.getOrDefault(uuid, false);
        map.put(uuid, newState);
        return newState;
    }

    /**
     * Explicitly sets the toggle value for a player.
     *
     * @param category The toggle category
     * @param uuid     The player's UUID
     * @param value    true to enable, false to disable
     */
    public static void set(Enum<?> category, UUID uuid, boolean value) {
        if (category == null || uuid == null) return;

        TOGGLES
                .computeIfAbsent(category, key -> new ConcurrentHashMap<>())
                .put(uuid, value);
    }

    /**
     * Removes all toggle entries associated with a player.
     * Typically used on player disconnect to prevent memory leaks.
     *
     * @param uuid The player's UUID
     */
    public static void removePlayer(UUID uuid) {
        if (uuid == null) return;

        TOGGLES.values().forEach(map -> map.remove(uuid));
    }

    /**
     * Retrieves all players with a specific toggle state in a category.
     *
     * @param category The toggle category
     * @param value    The desired state (true/false)
     * @return List of player UUIDs matching the state
     */
    public static List<UUID> getAll(Enum<?> category, boolean value) {
        if (category == null) return Collections.emptyList();

        Map<UUID, Boolean> map = TOGGLES.get(category);
        if (map == null) return Collections.emptyList();

        return map.entrySet()
                .stream()
                .filter(entry -> entry.getValue() == value)
                .map(Map.Entry::getKey)
                .toList();
    }

    /**
     * Clears all toggle data for a specific category.
     *
     * @param category The toggle category
     */
    public static void clearCategory(Enum<?> category) {
        if (category == null) return;
        TOGGLES.remove(category);
    }

    /**
     * Clears all toggle data across all categories.
     * Use with caution.
     */
    public static void clearAll() {
        TOGGLES.clear();
    }
}