package net.justrotem.data.util;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.jetbrains.annotations.NotNull;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Pattern;

/**
 * Utility class for formatting and converting text using MiniMessage.
 *
 * <p>This class provides:
 * <ul>
 *     <li>Legacy color code (&) conversion to MiniMessage</li>
 *     <li>Component parsing and caching</li>
 *     <li>Plain text extraction</li>
 *     <li>Basic validation utilities</li>
 * </ul>
 * </p>
 *
 * <p>All methods are static and thread-safe.</p>
 */
public final class TextFormatter {

    private static final MiniMessage MINI_MESSAGE = MiniMessage.miniMessage();
    private static final PlainTextComponentSerializer PLAIN_TEXT = PlainTextComponentSerializer.plainText();

    /**
     * Cache for parsed components to avoid repeated MiniMessage parsing.
     */
    private static final Map<String, Component> COMPONENT_CACHE = new ConcurrentHashMap<>();

    /**
     * Pattern for validating simple strings (letters, numbers, underscore).
     */
    private static final Pattern VALID_PATTERN = Pattern.compile("^[a-zA-Z0-9_]+$");

    /**
     * Mapping of legacy color codes (&) to MiniMessage tags.
     */
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

    /**
     * Converts a {@link Component} to plain text.
     *
     * @param component the component
     * @return plain text representation, or empty string if null
     */
    public static String getText(Component component) {
        return component == null ? "" : PLAIN_TEXT.serialize(component);
    }

    /**
     * Escapes MiniMessage tags in the input string.
     *
     * @param input input string
     * @return escaped string safe for MiniMessage parsing
     */
    public static String escapeTags(final @NotNull String input) {
        return MINI_MESSAGE.escapeTags(input);
    }

    /**
     * Parses a string into a {@link Component}, supporting legacy color codes.
     *
     * <p>Results are cached to improve performance.</p>
     *
     * @param text input text
     * @return parsed Component (empty if input is null or empty)
     */
    public static Component color(String text) {
        if (text == null || text.isEmpty()) {
            return Component.empty();
        }

        return COMPONENT_CACHE.computeIfAbsent(text, key ->
                MINI_MESSAGE.deserialize(legacyToMiniMessage(key))
        );
    }

    /**
     * Converts legacy color codes (&) into MiniMessage format.
     *
     * @param text input text
     * @return converted MiniMessage string
     */
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

    /**
     * Checks whether a string contains characters outside of [a-zA-Z0-9_].
     *
     * <p>This is commonly used for validating usernames or identifiers.</p>
     *
     * @param str input string
     * @return true if contains invalid characters, false otherwise
     */
    public static boolean containsSpecialChars(String str) {
        if (str == null || str.isEmpty()) {
            return false;
        }

        return !VALID_PATTERN.matcher(str).matches();
    }

    /**
     * Clears the internal component cache.
     *
     * <p>Useful if memory needs to be reclaimed or formatting rules change.</p>
     */
    public static void clearCache() {
        COMPONENT_CACHE.clear();
    }
}