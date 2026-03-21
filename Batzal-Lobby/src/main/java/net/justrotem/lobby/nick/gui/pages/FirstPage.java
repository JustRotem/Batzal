package net.justrotem.lobby.nick.gui.pages;

import net.justrotem.data.util.TextUtility;
import net.justrotem.lobby.nick.gui.BookGUI;
import net.justrotem.lobby.nick.gui.BookManager;
import net.kyori.adventure.text.Component;
import org.bukkit.entity.Player;

public class FirstPage implements BookGUI {

    @Override
    public void open(Player player) {
        Component page = TextUtility.color("Nicknames allow you to\nplay with different\nusername to no get\nrecognized.\n\nAll rules still apply.\nYou can still be\nreported and all name\nhistory is stored.\n\n➤ ")
                .append(BookManager.clickable("&nI understand, set\nup my nickname", player, "Click here to proceed", () ->
                        BookManager.openBook(player, "rank")
                ));

        BookManager.openBook(player, title(), page);
    }

    @Override
    public Component title() {
        return TextUtility.color("NickName Menu");
    }
}
