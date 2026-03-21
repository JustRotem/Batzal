package net.justrotem.data.enums;

import net.justrotem.data.cache.PlayerManager;

import java.util.UUID;

public class Visibility {

    public enum State {
        VISIBLE, HIDDEN
    }

    public static boolean isVisible(UUID uuid) {
        return PlayerManager.get(uuid).getVisibilityState() == State.VISIBLE;
    }
}
