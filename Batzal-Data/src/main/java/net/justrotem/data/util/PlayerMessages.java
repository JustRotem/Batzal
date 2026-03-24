package net.justrotem.data.util;

import net.kyori.adventure.text.Component;

/**
 * Utility class for generating common player-related messages.
 *
 * <p>All messages support MiniMessage formatting via {@link TextFormatter}.</p>
 */
public final class PlayerMessages {

    private PlayerMessages() {
    }

    /**
     * Message shown when a player cannot be found.
     *
     * @param name the player name
     * @return formatted message component
     */
    public static Component playerNotFound(String name) {
        return message("&cCan't find a player by the name '%player%'!", name);
    }

    /**
     * Message shown when a player is not online.
     *
     * @param name the player name
     * @return formatted message component
     */
    public static Component playerNotOnline(String name) {
        return message("&c%player%'s not online!", name);
    }

    /**
     * Formats a template message by replacing %player%.
     *
     * @param template message template
     * @param player   player name
     * @return formatted component
     */
    public static Component message(String template, String player) {
        return TextFormatter.color(template.replace("%player%", player));
    }
}