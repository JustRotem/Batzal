package net.justrotem.lobby.nick.gui.pages;

import net.justrotem.data.utils.TextUtility;
import net.justrotem.lobby.nick.gui.BookGUI;
import net.justrotem.lobby.nick.gui.BookManager;
import net.justrotem.lobby.nick.gui.NickSignGUI;
import net.kyori.adventure.text.Component;
import org.bukkit.entity.Player;

public class NotAllowedPage implements BookGUI {

    @Override
    public void open(Player player) {
        String nickname = BookManager.getBookData(player, "name");
        String error = BookManager.getBookData(player, "notallowed");

        Component page = TextUtility.color("Hold up! You can't use\n%nickname%'s as your nickname!\n\n".replace("%nickname%", nickname))
                .append(TextUtility.color(error))
                .append(TextUtility.color("\n\n      "))
                .append(BookManager.clickable("&c&l&nTRY AGAIN", player, "Click here to generate another name", () -> NickSignGUI.openBookSign(player)));

        BookManager.openBook(player, title(), page);
    }

    @Override
    public Component title() {
        return TextUtility.color("NickName Menu");
    }
}
