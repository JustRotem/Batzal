package net.justrotem.data.cache;

import net.justrotem.data.model.PlayerData;
import net.justrotem.data.service.PlayerLookupService;

import java.util.List;
import java.util.UUID;

/**
 * Utility class for handling vanish-related visibility logic.
 *
 * <p>This class does not store state directly. It relies on {@link PlayerLookupService}
 * to retrieve player data and determine vanish status.</p>
 *
 * <p>All methods are stateless and operate on current data snapshots.</p>
 */
public final class VanishManager {

    private VanishManager() {
    }

    /**
     * Checks whether a player is currently vanished.
     *
     * <p>If the player is not found, this method returns {@code false}.</p>
     *
     * @param uuid the player's UUID
     * @return true if the player is vanished, false otherwise
     */
    public static boolean isInvisible(UUID uuid) {
        if (uuid == null) return false;

        return PlayerLookupService.get(uuid)
                .map(PlayerData::isVanished)
                .orElse(false);
    }

    /**
     * Determines whether a viewer can see a target player, considering vanish state.
     *
     * <p>Visibility rules:
     * <ul>
     *     <li>If {@code bypassVanish} is true → always visible</li>
     *     <li>If {@code viewed} is null → not visible</li>
     *     <li>Otherwise → visible only if the target is not vanished</li>
     * </ul>
     * </p>
     *
     * @param viewed        the UUID of the player being viewed
     * @param bypassVanish  whether the viewer has permission to bypass vanish
     * @return true if the viewer can see the target player
     */
    public static boolean canSee(UUID viewed, boolean bypassVanish) {
        if (bypassVanish) return true;
        if (viewed == null) return false;

        return !isInvisible(viewed);
    }

    /**
     * Indicates whether a player has vanish bypass permission.
     *
     * <p>This method exists for semantic clarity in calling code, even though it currently
     * returns the provided value directly.</p>
     *
     * @param hasPermission result of a permission check
     * @return true if the player can bypass vanish restrictions
     */
    public static boolean hasVanishBypass(boolean hasPermission) {
        return hasPermission;
    }

    /**
     * Retrieves all players that are currently vanished.
     *
     * <p>This method queries all loaded {@link PlayerData} instances and filters
     * those marked as vanished.</p>
     *
     * @return list of UUIDs of vanished players
     */
    public static List<UUID> getAllVanishedPlayers() {
        return PlayerLookupService.getAll()
                .stream()
                .filter(PlayerData::isVanished)
                .map(PlayerData::getUniqueId)
                .toList();
    }
}