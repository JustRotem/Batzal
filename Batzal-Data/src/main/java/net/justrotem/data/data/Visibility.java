package net.justrotem.data.data;

import net.justrotem.data.player.PlayerManager;

import java.util.UUID;

public class Visibility {

    public enum State {
        VISIBLE, HIDDEN
    }

    public static boolean isVisible(UUID uuid) {
        return PlayerManager.get(uuid).getVisibilityState() == State.VISIBLE;
    }
}
