package net.justrotem.lobby.nick.gui.pages;

import net.justrotem.data.utils.TextUtility;
import net.justrotem.lobby.Main;
import net.justrotem.lobby.nick.NickManager;
import net.justrotem.lobby.nick.gui.BookGUI;
import net.justrotem.lobby.nick.gui.BookManager;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

public class FinishedPage implements BookGUI {

    @Override
    public void open(Player player) {
        String nickname = BookManager.getBookData(player, "name");
        String skin = BookManager.getBookData(player, "skin");
        String rank = BookManager.getBookData(player, "rank");

        NickManager.nick(player, nickname, skin, rank);

        Component page = TextUtility.color("You have finished setting up your nickname!\n\nYou are now nicked as %nick%&r.\n\nTo go back to being your  usual self, type:\n&l/nick reset"
                .replace("%nick%", NickManager.getLegacyDisplayName(nickname, rank))
        );

        player.sendMessage(TextUtility.color("&aYou have finished setting up your nickname!"));

        Bukkit.getScheduler().runTaskLater(Main.getInstance(), () -> BookManager.openBook(player, title(), page), 20L);
    }

    @Override
    public Component title() {
        return TextUtility.color("NickName Menu");
    }
}
