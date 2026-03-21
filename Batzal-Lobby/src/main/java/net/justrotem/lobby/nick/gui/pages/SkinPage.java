package net.justrotem.lobby.nick.gui.pages;

import net.justrotem.data.utils.TextUtility;
import net.justrotem.lobby.nick.NickManager;
import net.justrotem.lobby.nick.gui.BookGUI;
import net.justrotem.lobby.nick.gui.BookManager;
import net.justrotem.lobby.skins.SkinManager;
import net.kyori.adventure.text.Component;
import org.bukkit.entity.Player;

import java.util.Random;

public class SkinPage implements BookGUI {

    @Override
    public void open(Player player) {
        String arrow = "➤ ";
        Component page = TextUtility.color("Awesome! Now, which\n&lSKIN &rwould you like to\nhave while nicked?\n")
                .append(TextUtility.color("\n" + arrow))
                .append(BookManager.clickable("My normal skin", player, "Click here to use your normal skin", () -> {
                    BookManager.setBookData(player, "skin", player.getName());
                    BookManager.openBook(player, "name");
                    player.sendMessage(TextUtility.color("&aYou will now have your Minecraft character's skin even when nicked!"));
                }))
                .append(TextUtility.color("\n" + arrow))
                .append(BookManager.clickable("Steve/Alex skin", player, "Click here to use Steve/Alex skin", () -> {
                    String skin = new Random().nextBoolean() ? "Steve" : "Alex";
                    BookManager.setBookData(player, "skin", skin);
                    BookManager.openBook(player, "name");
                    player.sendMessage(TextUtility.color("&aYour skin has been set to %skin%!".replace("%skin%", skin.toUpperCase())));
                }))
                .append(TextUtility.color("\n" + arrow))
                .append(BookManager.clickable("Random skin", player, "Click here to a random skin", () -> {
                    String skin = SkinManager.getRandomSkin(player).getName();
                    BookManager.setBookData(player, "skin", skin);
                    BookManager.openBook(player, "name");
                    player.sendMessage(TextUtility.color("&aYour skin has been set to %skin%!".replace("%skin%", skin.toUpperCase())));
                }));

        String skin = NickManager.getSkin(player);
        if (skin != null) {
            page = page.append(TextUtility.color("\n" + arrow))
                    .append(BookManager.clickable("Reuse %skin%".replace("%skin%", skin), player, "Click here to use your previous skin", () -> {
                        BookManager.setBookData(player, "skin", skin);
                        BookManager.openBook(player, "name");
                        player.sendMessage(TextUtility.color("&aYour skin has been set to %skin%!".replace("%skin%", skin.toUpperCase())));
                    }));
        }

        BookManager.openBook(player, title(), page);
    }

    @Override
    public Component title() {
        return TextUtility.color("NickName Menu");
    }
}
