package net.justrotem.lobby.menu.menus;

import net.justrotem.lobby.menu.Menu;
import net.justrotem.lobby.menu.MenuManager;
import net.justrotem.lobby.menu.PlayerMenuUtility;
import net.justrotem.lobby.utils.ItemUtility;
import org.bukkit.Material;
import org.bukkit.event.inventory.InventoryClickEvent;

import java.util.List;

public class CustomizeAppearances extends Menu {

    public CustomizeAppearances(PlayerMenuUtility playerMenuUtility) {
        super(playerMenuUtility);
    }

    @Override
    public String getMenuName() {
        return "Customize Appearances";
    }

    @Override
    public int getSlots() {
        return 36;
    }

    @Override
    public boolean cancelAllClicks() {
        return false;
    }

    @Override
    public void handleMenu(InventoryClickEvent event) {
        if (event.getSlot() == 31) {
            if (playerMenuUtility.peekMenu() != null) this.back();
            else this.close();
            return;
        }

        if (event.getSlot() == 10) {
            MenuManager.openMenu(RankColor.class, player,this);
            return;
        }

        if (event.getSlot() == 12) {
            MenuManager.openMenu(PunchMessages.class, player,this);
            return;
        }

        if (event.getSlot() == 14) {

            return;
        }

        if (event.getSlot() == 16) {

        }
    }

    @Override
    public void setMenuItems() {
        inventory.setItem(10, ItemUtility.createItem(Material.INK_SAC, "&aMVP+ Rank Color", List.of("&7Players ranked &bMVP&c+ &7can", "&7unlock and switch the color of", "&7their +.", "", "&eClick to change!"), false));
        inventory.setItem(12, ItemUtility.createItem(Material.MAP, "&aPunch Messages", List.of("&7Customize the message when you use", "&7your punch ability!", "", "&eClick to change!"), false));
        inventory.setItem(14, ItemUtility.createItem(Material.PRISMARINE_CRYSTALS, "&aGlow", List.of("&7Gives your in-game character an", "&7outline in the color of your rank.", "", "&cNote: &7The glowing effect will only be", "&7visible to players using Minecraft  1.9", "&7or later!", "", "&cUnlocked in Any Season Battle Pass!"), false));
        inventory.setItem(16, ItemUtility.createItem(Material.NAME_TAG, "&aStatus", List.of("&7Set a stataus to be displayed above", "&7your head in lobbies.", "", "&eClick to change!"), false));

        inventory.setItem(31, playerMenuUtility.peekMenu() == null ? ItemUtility.createItem(Material.BARRIER, "&cClose", null, false) : ItemUtility.createItem(Material.ARROW, "&aGo Back", List.of("&7To " + playerMenuUtility.peekMenu().getMenuName()), false));
    }
}
