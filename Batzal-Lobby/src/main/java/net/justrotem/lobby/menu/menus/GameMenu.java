package net.justrotem.lobby.menu.menus;

import net.justrotem.lobby.menu.Menu;
import net.justrotem.lobby.menu.PlayerMenuUtility;
import org.bukkit.event.inventory.InventoryClickEvent;

public class GameMenu extends Menu {

    public GameMenu(PlayerMenuUtility playerMenuUtility) {
        super(playerMenuUtility);
    }

    @Override
    public String getMenuName() {
        return "Game Menu";
    }

    @Override
    public int getSlots() {
        return 54;
    }

    @Override
    public boolean cancelAllClicks() {
        return false;
    }

    @Override
    public void handleMenu(InventoryClickEvent event) {

    }

    @Override
    public void setMenuItems() {

    }
}
