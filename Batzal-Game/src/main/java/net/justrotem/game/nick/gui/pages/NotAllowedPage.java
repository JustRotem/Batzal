package net.justrotem.game.nick.gui.pages;

import net.justrotem.game.nick.gui.BookGUI;
import net.justrotem.game.nick.gui.BookManager;
import net.justrotem.game.nick.gui.NickSignGUI;
import net.justrotem.game.utils.TextUtils;
import net.kyori.adventure.text.Component;
import org.bukkit.entity.Player;

public class NotAllowedPage implements BookGUI {

    @Override
    public void open(Player player) {
        String nickname = BookManager.getBookData(player, "name");
        String error = BookManager.getBookData(player, "notallowed");

        Component page = TextUtils.color("Hold up! You can't use\n%nickname%'s as your nickname!\n\n".replace("%nickname%", nickname))
                .append(TextUtils.color(error))
                .append(TextUtils.color("\n\n      "))
                .append(BookManager.clickable("&c&l&nTRY AGAIN", player, "Click here to generate another name", () -> NickSignGUI.openBookSign(player)));

        BookManager.openBook(player, title(), page);
    }

    @Override
    public Component title() {
        return TextUtils.color("NickName Menu");
    }
}
