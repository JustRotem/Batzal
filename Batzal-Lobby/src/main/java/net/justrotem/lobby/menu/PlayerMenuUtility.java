package net.justrotem.lobby.menu;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.Stack;
import java.util.UUID;

public class PlayerMenuUtility {
    private final UUID uuid;
    private final Stack<Menu> history = new Stack<>();

    public PlayerMenuUtility(UUID uuid) {
        this.uuid = uuid;
    }

    public Player getOwner() {
        return Bukkit.getPlayer(this.uuid);
    }

    public Menu peekMenu() {
        return history.isEmpty() ? null : history.peek();
    }

    public Menu lastMenu() {
        return history.isEmpty() ? null : history.pop();
    }

    public void pushMenu(Menu menu) {
        if (menu == null) return;

        this.history.push(menu);
    }
}

