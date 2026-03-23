package net.justrotem.data.enums;

import net.justrotem.data.model.PlayerData;
import net.justrotem.data.service.PlayerLookupService;

import java.util.Optional;
import java.util.UUID;

public class Visibility {

    public enum State {
        VISIBLE, HIDDEN
    }

    public static boolean isVisible(UUID uuid) {
        Optional<PlayerData> playerData = PlayerLookupService.get(uuid);
        return playerData.map(data -> data.getVisibilityState() == State.VISIBLE).orElse(true);
    }
}
