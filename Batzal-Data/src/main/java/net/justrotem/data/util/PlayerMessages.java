package net.justrotem.data.util;

import net.kyori.adventure.text.Component;

public final class PlayerMessages {

    private PlayerMessages() {
    }

    public static Component playerNotFound(String name) {
        return TextFormatter.color("&cCan't find a player by the name '%player%'!".replace("%player%", name));
    }

    public static Component playerNotOnline(String name) {
        return TextFormatter.color("&c%player%'s not online!".replace("%player%", name));
    }
}
