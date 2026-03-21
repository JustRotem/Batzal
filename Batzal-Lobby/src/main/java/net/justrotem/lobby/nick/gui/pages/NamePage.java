package net.justrotem.lobby.nick.gui.pages;

import net.justrotem.data.util.TextUtility;
import net.justrotem.lobby.nick.NickManager;
import net.justrotem.lobby.nick.gui.BookGUI;
import net.justrotem.lobby.nick.gui.BookManager;
import net.justrotem.lobby.nick.gui.NickSignGUI;
import net.kyori.adventure.text.Component;
import org.bukkit.entity.Player;

public class NamePage implements BookGUI {

    @Override
    public void open(Player player) {
        String arrow = "➤ ";
        Component page = TextUtility.color("Alright, now you'll\nneed to choose the\n&lNAME &rto use!\n");

        if (player.hasPermission("batzal.nick.customname")) page = page.append(TextUtility.color("\n" + arrow))
                .append(BookManager.clickable("enter a name", player, "Click here to enter a name", () -> NickSignGUI.openBookSign(player)));

        page = page.append(TextUtility.color("\n" + arrow))
                .append(BookManager.clickable("Use a random name", player, "Click here to use a random name", () -> {
                    BookManager.setBookData(player, "name", "random");
                    BookManager.openBook(player, "randomname");
                }));

        String nickname = NickManager.getNickName(player);
        if (nickname != null && !NickManager.isNameRestricted(player, nickname, true)) {
            page = page.append(TextUtility.color("\n" + arrow))
                    .append(BookManager.clickable("Reuse %nickname%".replace("%nickname%", nickname), player, "Click here to reuse '%nickname%'".replace("%nickname%", nickname), () -> {
                        BookManager.setBookData(player, "name", nickname);
                        BookManager.openBook(player, "finished");
                    }));
        }

        page = page.append(TextUtility.color("\n\nTo go back to being\nyour usual self, type:\n&l/nick reset"));

        BookManager.openBook(player, title(), page);
    }

    @Override
    public Component title() {
        return TextUtility.color("NickName Menu");
    }
}
