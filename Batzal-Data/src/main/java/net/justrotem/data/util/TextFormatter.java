package net.justrotem.data.util;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.jetbrains.annotations.NotNull;

import java.util.regex.Pattern;

public class TextFormatter {

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

    private static final Pattern VALID_PATTERN = Pattern.compile("^[a-zA-Z0-9_]+$");

    public static boolean containsSpecialChars(String str) {
        if (str == null || str.isEmpty()) return false;
        return !VALID_PATTERN.matcher(str).matches();
    }
}
