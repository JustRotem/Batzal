package net.justrotem.data.utils;

import com.google.common.primitives.Chars;
import net.justrotem.data.player.PlayerManager;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

public class TextUtility {

    public static String getText(Component component) {
        return PlainTextComponentSerializer.plainText().serialize(component);
    }

    public static String escapeTags(final @NotNull String input) {
        return MiniMessage.miniMessage().escapeTags(input);
    }

    public static Component color(String text) {
        final MiniMessage mm = MiniMessage.miniMessage();

        return mm.deserialize(legacyToMiniMessage(text));
    }

    public static String legacyToMiniMessage(String text) {
        // Convert & color codes to MiniMessage-compatible tags
        return text
                .replace("&0", "<black>")
                .replace("&1", "<dark_blue>")
                .replace("&2", "<dark_green>")
                .replace("&3", "<dark_aqua>")
                .replace("&4", "<dark_red>")
                .replace("&5", "<dark_purple>")
                .replace("&6", "<gold>")
                .replace("&7", "<gray>")
                .replace("&8", "<dark_gray>")
                .replace("&9", "<blue>")
                .replace("&a", "<green>")
                .replace("&b", "<aqua>")
                .replace("&c", "<red>")
                .replace("&d", "<light_purple>")
                .replace("&e", "<yellow>")
                .replace("&f", "<white>")
                .replace("&l", "<bold>")
                .replace("&n", "<underlined>")
                .replace("&o", "<italic>")
                .replace("&m", "<strikethrough>")
                .replace("&k", "<obf>")
                .replace("&r", "<reset>")
                .replace("/&0", "</black>")
                .replace("/&1", "</dark_blue>")
                .replace("/&2", "</dark_green>")
                .replace("/&3", "</dark_aqua>")
                .replace("/&4", "</dark_red>")
                .replace("/&5", "</dark_purple>")
                .replace("/&6", "</gold>")
                .replace("/&7", "</gray>")
                .replace("/&8", "</dark_gray>")
                .replace("/&9", "</blue>")
                .replace("/&a", "</green>")
                .replace("/&b", "</aqua>")
                .replace("/&c", "</red>")
                .replace("/&d", "</light_purple>")
                .replace("/&e", "</yellow>")
                .replace("/&f", "</white>")
                .replace("/&l", "</bold>")
                .replace("/&n", "</underlined>")
                .replace("/&o", "</italic>")
                .replace("/&m", "</strikethrough>")
                .replace("/&k", "</obf>")
                .replace("/&r", "</reset>");
    }

    private static final Map<UUID, Map<String, Runnable>> ACTIONS = new HashMap<>();
    private static final AtomicInteger UNIQUE_ID = new AtomicInteger(0);

    public static void registerAction(UUID uuid, String key, Runnable action) {
        ACTIONS.computeIfAbsent(uuid, id -> new HashMap<>())
                .put(key.toLowerCase(), action);
    }

    public static Component clickable(Component label, UUID uuid, Component hoverText, Runnable action) {
        String actionKey = String.valueOf(UNIQUE_ID.getAndIncrement());
        registerAction(uuid, actionKey, action);

        return label.hoverEvent(hoverText)
                .clickEvent(ClickEvent.callback((audience) -> {
                    if (audience instanceof Player p && p.getUniqueId().equals(uuid)) {
                        action.run();
                    }
                }));
    }

    public static Component clickable(String label, UUID uuid, String hoverText, Runnable action) {
        return clickable(TextUtility.color(label), uuid, TextUtility.color(hoverText), action);
    }

    public static boolean containsSpecialChars(String str) {
        List<Character> allowCharacters = Chars.asList("abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789_".toCharArray());

        for (char c : str.toCharArray()) {
            if (!allowCharacters.contains(c))
                return true;
        }

        return false;
    }

    public static Component playerNotFound(String name) {
        return color("&cCan't find a player by the name '%player%'!".replace("%player%", name));
    }

    public static Component playerNotOnline(String name) {
        try {
            return color("&c%player%'s not online!".replace("%player%", Objects.requireNonNull(PlayerManager.getName(name))));
        } catch (NullPointerException e) {
            return playerNotFound(name);
        }
    }
}
