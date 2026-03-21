package net.justrotem.lobby.nick.gui;

import de.rapha149.signgui.SignGUI;
import de.rapha149.signgui.exception.SignGUIVersionException;
import net.justrotem.data.utils.TextUtility;
import net.justrotem.lobby.hooks.PlayerManager;
import net.justrotem.lobby.nick.NickManager;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;

import java.util.function.Consumer;

public class NickSignGUI {

    /**
     * Opens a sign GUI where the player can input a nickname on line 3.
     * Other lines are optional and can be provided as Components.
     *
     * @param player       The player to open the sign GUI for
     * @param initialLines Initial lines (Components). Lines 0-2 will be used, line 3 is for nickname input
     * @param callback     Called when the player submits the sign. Returns the nickname as Component.
     */
    private static void open(Player player, Component[] initialLines, Consumer<Component> callback) {
        String[] lines = new String[4];

        // Line 0 is empty for player input
        lines[0] = "";

        // Lines 1-3 are preset lines
        for (int i = 1; i <= 3; i++) {
            if (initialLines != null && i - 1 < initialLines.length && initialLines[i - 1] != null) {
                lines[i] = LegacyComponentSerializer.legacySection().serialize(initialLines[i - 1]);
            } else {
                lines[i] = "";
            }
        }

        try {
            SignGUI gui = SignGUI.builder()
                    .setType(Material.OAK_SIGN)
                    .setLines(lines)
                    .setHandler((p, result) -> {
                        if (!p.equals(player)) return null;

                        // Get the nickname from line 0
                        String nickLine = result.getLine(0);
                        Component nickComponent = nickLine != null ?
                                LegacyComponentSerializer.legacySection().deserialize(nickLine) :
                                Component.empty();

                        callback.accept(nickComponent); // return the nickname as Component
                        return null;
                    })
                    .build();

            gui.open(player);
        } catch (SignGUIVersionException e) {

        }
    }

    public static void openBookSign(Player player) {
        Component[] initial = new Component[] {
                TextUtility.color("^".repeat(15)),
                TextUtility.color("Enter your"),
                TextUtility.color("username here")
        };

        open(player, initial, nick -> {
            player.sendMessage(TextUtility.color("&eProcessing request. Please wait..."));

            String nickname = TextUtility.getText(nick);
            BookManager.setBookData(player, "name", nickname);
            isNameAllowed(player, nickname);

            String message = NickManager.getNameAllowedMessage(player, nickname);
            if (!message.isEmpty()) {
                BookManager.setBookData(player, "notallowed", message + "!");
                BookManager.openBook(player, "notallowed");
                return;
            }

            BookManager.openBook(player, "finished");
        });
    }

    private static void isNameAllowed(Player player, String name) {
        String message = "";

        if (name.length() < 3) message = "This name cannot be\nless than 3 letters";
        else if (name.length() > 16) message = "This name cannot be\nmore than 16 letters";
        else if (TextUtility.containsSpecialChars(name)) message = "This name can contain\nonly 0-9, a-z and A-Z";
        else if (player.getName().equalsIgnoreCase(name)) message = "You can't nick\nas yourself";
        else if (PlayerManager.isRegisteredOffline(name) || Bukkit.getPlayer(name) != null || NickManager.isNicknameUsed(player.getUniqueId(), name)) message = "This name belongs to\na known player!";

        if (!message.isEmpty()) {
            BookManager.setBookData(player, "notallowed", message);
            BookManager.openBook(player, "notallowed");
            return;
        }

        BookManager.openBook(player, "finished");
    }
}
