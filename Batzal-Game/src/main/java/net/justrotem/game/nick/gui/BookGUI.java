package net.justrotem.game.nick.gui;

import net.kyori.adventure.text.Component;
import org.bukkit.entity.Player;

public interface BookGUI {
    default Component title() {
        return Component.text("Book GUI");
    }

    void open(Player player);
}