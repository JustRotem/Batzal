package net.justrotem.lobby.nick.gui.pages;

import net.justrotem.data.util.TextUtility;
import net.justrotem.lobby.hooks.LuckPermsManager;
import net.justrotem.lobby.nick.RankManager;
import net.justrotem.lobby.nick.gui.BookGUI;
import net.justrotem.lobby.nick.gui.BookManager;
import net.kyori.adventure.text.Component;
import org.bukkit.entity.Player;

public class RankPage implements BookGUI {

    @Override
    public void open(Player player) {
        Component page = TextUtility.color("Let's get you set up\nwith your nickname!\nFirst, you'll need to\nchoose which &lRANK&r\nyou would like to be\nshown as when nicked.\n");

        for (String rank : RankManager.getRanks()) {
            Component displayname = LuckPermsManager.getGroupDisplayName(rank);
            page = page.append(TextUtility.color("\n➤ "))
                    .append(BookManager.clickable(displayname, player, TextUtility.color("Click here to be shown as ").append(displayname), () -> {
                        BookManager.setBookData(player, "rank", rank);
                        BookManager.openBook(player, "skin");

                        player.sendMessage(TextUtility.color("&aSet your nick rank to ").append(displayname, TextUtility.color("&a!")));
                    }));
        }

        BookManager.openBook(player, title(), page);
    }

    @Override
    public Component title() {
        return TextUtility.color("NickName Menu");
    }
}
