package net.justrotem.lobby.menu;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.InventoryHolder;

public class MenuListener implements Listener {

    @EventHandler
    public void onMenuClick(InventoryClickEvent event) {
        InventoryHolder holder = event.getInventory().getHolder();
        if (holder instanceof Menu menu) {
            if (event.getCurrentItem() == null) return;

            if (menu.cancelAllClicks()) event.setCancelled(true);

            menu.handleMenu(event);
        }
    }

    /*@EventHandler
    public void onMenuClose(InventoryCloseEvent event) {
        try {
            if (event.getReason() == InventoryCloseEvent.Reason.PLUGIN) {
                event.getPlayer().sendMessage("plugin");
                return;
            }

            Menu menu = MenuManager.getPlayerMenuUtility((Player) event.getPlayer()).lastMenu();
            if (menu == null) return;

            if (event.getInventory() == menu.getInventory() && menu.playerMenuUtility.getData("target") != null)
                menu.playerMenuUtility.setData("target", null);
        } catch (MenuManagerNotSetupException e) {
            e.printStackTrace();
        }
    }*/
}

