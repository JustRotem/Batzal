package net.justrotem.data.util;

import net.kyori.adventure.text.Component;

public final class PlayerMessages {

    private PlayerMessages() {
    }

    public static Component playerNotFound(String name) {
        return message("&cCan't find a player by the name '%player%'!", name);
    }

    public static Component playerNotOnline(String name) {
        return message("&c%player%'s not online!", name);
    }

    public static Component message(String template, String player) {
        return TextFormatter.color(template.replace("%player%", player));
    }
}
