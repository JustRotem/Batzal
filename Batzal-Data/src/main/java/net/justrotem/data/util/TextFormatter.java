package net.justrotem.data.util;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.jetbrains.annotations.NotNull;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Pattern;

public final class TextFormatter {

    private static final MiniMessage MINI_MESSAGE = MiniMessage.miniMessage();
    private static final PlainTextComponentSerializer PLAIN_TEXT = PlainTextComponentSerializer.plainText();
    private static final Map<String, Component> COMPONENT_CACHE = new ConcurrentHashMap<>();

    private static final Pattern VALID_PATTERN = Pattern.compile("^[a-zA-Z0-9_]+$");

    private static final Map<String, String> LEGACY_TO_MINI = Map.ofEntries(
            Map.entry("&0", "<black>"),
            Map.entry("&1", "<dark_blue>"),
            Map.entry("&2", "<dark_green>"),
            Map.entry("&3", "<dark_aqua>"),
            Map.entry("&4", "<dark_red>"),
            Map.entry("&5", "<dark_purple>"),
            Map.entry("&6", "<gold>"),
            Map.entry("&7", "<gray>"),
            Map.entry("&8", "<dark_gray>"),
            Map.entry("&9", "<blue>"),
            Map.entry("&a", "<green>"),
            Map.entry("&b", "<aqua>"),
            Map.entry("&c", "<red>"),
            Map.entry("&d", "<light_purple>"),
            Map.entry("&e", "<yellow>"),
            Map.entry("&f", "<white>"),
            Map.entry("&l", "<bold>"),
            Map.entry("&n", "<underlined>"),
            Map.entry("&o", "<italic>"),
            Map.entry("&m", "<strikethrough>"),
            Map.entry("&k", "<obf>"),
            Map.entry("&r", "<reset>"),

            Map.entry("/&0", "</black>"),
            Map.entry("/&1", "</dark_blue>"),
            Map.entry("/&2", "</dark_green>"),
            Map.entry("/&3", "</dark_aqua>"),
            Map.entry("/&4", "</dark_red>"),
            Map.entry("/&5", "</dark_purple>"),
            Map.entry("/&6", "</gold>"),
            Map.entry("/&7", "</gray>"),
            Map.entry("/&8", "</dark_gray>"),
            Map.entry("/&9", "</blue>"),
            Map.entry("/&a", "</green>"),
            Map.entry("/&b", "</aqua>"),
            Map.entry("/&c", "</red>"),
            Map.entry("/&d", "</light_purple>"),
            Map.entry("/&e", "</yellow>"),
            Map.entry("/&f", "</white>"),
            Map.entry("/&l", "</bold>"),
            Map.entry("/&n", "</underlined>"),
            Map.entry("/&o", "</italic>"),
            Map.entry("/&m", "</strikethrough>"),
            Map.entry("/&k", "</obf>"),
            Map.entry("/&r", "</reset>")
    );

    private TextFormatter() {
    }

    public static String getText(Component component) {
        return component == null ? "" : PLAIN_TEXT.serialize(component);
    }

    public static String escapeTags(final @NotNull String input) {
        return MINI_MESSAGE.escapeTags(input);
    }

    public static Component color(String text) {
        if (text == null || text.isEmpty()) {
            return Component.empty();
        }

        return COMPONENT_CACHE.computeIfAbsent(text, key ->
                MINI_MESSAGE.deserialize(legacyToMiniMessage(key))
        );
    }

    public static String legacyToMiniMessage(String text) {
        if (text == null || text.isEmpty()) {
            return "";
        }

        String result = text;

        for (Map.Entry<String, String> entry : LEGACY_TO_MINI.entrySet()) {
            result = result.replace(entry.getKey(), entry.getValue());
        }

        return result;
    }

    public static boolean containsSpecialChars(String str) {
        if (str == null || str.isEmpty()) {
            return false;
        }

        return !VALID_PATTERN.matcher(str).matches();
    }

    public static void clearCache() {
        COMPONENT_CACHE.clear();
    }
}