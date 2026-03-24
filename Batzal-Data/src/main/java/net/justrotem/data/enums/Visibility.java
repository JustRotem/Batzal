package net.justrotem.data.enums;

import net.justrotem.data.model.PlayerData;
import net.justrotem.data.service.PlayerLookupService;

import java.util.Optional;
import java.util.UUID;

/**
 * Handles player visibility state.
 */
public class Visibility {

    /**
     * Represents visibility state.
     */
    public enum State {
        VISIBLE,
        HIDDEN
    }

    /**
     * Checks whether a player is visible.
     *
     * @param uuid player UUID
     * @return true if visible, false otherwise
     */
    public static boolean isVisible(UUID uuid) {
        Optional<PlayerData> playerData = PlayerLookupService.get(uuid);
        return playerData.map(data -> data.getVisibilityState() == State.VISIBLE).orElse(true);
    }
}
