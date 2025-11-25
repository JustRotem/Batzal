package net.justrotem.game.nick.gui.pages;

import net.justrotem.data.hooks.LuckPermsManager;
import net.justrotem.game.Main;
import net.justrotem.game.nick.gui.BookGUI;
import net.justrotem.game.nick.gui.BookManager;
import net.justrotem.game.utils.TextUtils;
import net.kyori.adventure.text.Component;
import org.bukkit.entity.Player;

public class RankPage implements BookGUI {

    @Override
    public void open(Player player) {
        Component page = TextUtils.color("Let's get you set up\nwith your nickname!\nFirst, you'll need to\nchoose which &lRANK&r\nyou would like to be\nshown as when nicked.\n");

        for (String rank : Main.getInstance().getConfig().getStringList("Nick.Ranks")) {
            Component displayname = LuckPermsManager.getGroupDisplayName(rank);
            page = page.append(TextUtils.color("\n➤ "))
                    .append(BookManager.clickable(displayname, player, TextUtils.color("Click here to be shown as ").append(displayname), () -> {
                        BookManager.setBookData(player, "rank", rank);
                        BookManager.openBook(player, "skin");

                        player.sendMessage(TextUtils.color("&aSet your nick rank to ").append(displayname, TextUtils.color("&a!")));
                    }));
        }

        BookManager.openBook(player, title(), page);
    }

    @Override
    public Component title() {
        return TextUtils.color("NickName Menu");
    }
}
