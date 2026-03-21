package net.justrotem.lobby.nick.gui.pages;

import net.justrotem.data.util.TextUtility;
import net.justrotem.lobby.nick.NickManager;
import net.justrotem.lobby.nick.gui.BookGUI;
import net.justrotem.lobby.nick.gui.BookManager;
import net.justrotem.lobby.nick.gui.NickSignGUI;
import net.kyori.adventure.text.Component;
import org.bukkit.entity.Player;

public class RandomNamePage implements BookGUI {

    @Override
    public void open(Player player) {
        player.sendMessage(TextUtility.color("&eGenerating a unique random name. Please wait..."));
        String nickname = NickManager.getRandomNick(player);

        Component page = TextUtility.color("We've generated a\nrandom username for\nyou:\n&l%nickname%\n".replace("%nickname%", nickname))
                .append(TextUtility.color("\n      "))
                .append(BookManager.clickable("&a&nUSE NAME", player, "Click here to this name", () -> {
                    BookManager.setBookData(player, "name", nickname);
                    BookManager.openBook(player, "finished");
                }))
                .append(TextUtility.color("\n      "))
                .append(BookManager.clickable("&c&nTRY AGAIN", player, "Click here to generate another name", () -> {
                    BookManager.setBookData(player, "randomname", "again");
                    BookManager.openBook(player, "randomname");
                }));

        if (player.hasPermission("batzal.nick.customname")) page = page.append(BookManager.clickable("\n\n&nOr enter a name to\nuse.", player, "Click here to enter a name", () -> NickSignGUI.openBookSign(player)));

        BookManager.openBook(player, title(), page);
    }

    @Override
    public Component title() {
        return TextUtility.color("NickName Menu");
    }
}
