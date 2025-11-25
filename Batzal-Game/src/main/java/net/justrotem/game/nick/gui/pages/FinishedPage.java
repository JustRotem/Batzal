package net.justrotem.game.nick.gui.pages;

import net.justrotem.game.Main;
import net.justrotem.game.nick.NickManager;
import net.justrotem.game.nick.gui.BookGUI;
import net.justrotem.game.nick.gui.BookManager;
import net.justrotem.game.utils.TextUtils;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

public class FinishedPage implements BookGUI {

    @Override
    public void open(Player player) {
        String nickname = BookManager.getBookData(player, "name");
        String skin = BookManager.getBookData(player, "skin");
        String rank = BookManager.getBookData(player, "rank");

        NickManager.setNick(player, nickname, skin, rank);

        Component page = TextUtils.color("You have finished setting up your nickname!\n\nYou are now nicked as %nick%&r.\n\nTo go back to being your  usual self, type:\n&l/nick reset"
                .replace("%nick%", NickManager.getLegacyDisplayName(nickname, rank))
        );

        player.sendMessage(TextUtils.color("&aYou have finished setting up your nickname!"));

        Bukkit.getScheduler().runTaskLater(Main.getInstance(), () -> BookManager.openBook(player, title(), page), 20L);
    }

    @Override
    public Component title() {
        return TextUtils.color("NickName Menu");
    }
}
