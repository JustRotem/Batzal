package net.justrotem.lobby.utils;

import com.google.common.primitives.Chars;
import net.justrotem.data.PlayerManager;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public final class TextUtils {

    public enum Format {
        PLAIN,
        LEGACY,
        MINIMESSAGE
    }

    private TextUtils() {} // prevent instantiation

    public static String serialize(Component component, Format format) {
        return switch (format) {
            case LEGACY -> LegacyComponentSerializer.legacySection().serialize(component);
            case MINIMESSAGE -> MiniMessage.miniMessage().serialize(component);
            default -> PlainTextComponentSerializer.plainText().serialize(component);
        };
    }

    public static Component deserialize(String input, Format format) {
        return switch (format) {
            case LEGACY -> LegacyComponentSerializer.legacySection().deserialize(input);
            case MINIMESSAGE -> MiniMessage.miniMessage().deserialize(input);
            default -> Component.text(input);
        };
    }
    
    public static String escapeTags(final @NotNull String input) {
        return MiniMessage.miniMessage().escapeTags(input);
    }

    public static Component color(String text) {
        final MiniMessage mm = MiniMessage.miniMessage();

        // Convert & codes to MiniMessage-compatible tags
        String replaced = text
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
                .replace("&r", "<reset>");
        return mm.deserialize(replaced);
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
        return color("&c%player%'s not online!".replace("%player%", PlayerManager.getName(name)));
    }
}
