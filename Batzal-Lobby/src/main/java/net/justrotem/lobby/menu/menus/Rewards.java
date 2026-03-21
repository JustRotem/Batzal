package net.justrotem.lobby.menu.menus;

import net.justrotem.lobby.menu.Menu;
import net.justrotem.lobby.menu.PlayerMenuUtility;
import net.justrotem.lobby.utils.ItemUtility;
import org.bukkit.Material;
import org.bukkit.event.inventory.InventoryClickEvent;

import java.util.List;

public class Rewards extends Menu {

    public Rewards(PlayerMenuUtility playerMenuUtility) {
        super(playerMenuUtility);
    }

    @Override
    public String getMenuName() {
        return "Batzal Leveling";
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
        if (event.getSlot() == 48) {
            if (playerMenuUtility.peekMenu() != null) this.back();
            else this.close();
        }
    }

    @Override
    public void setMenuItems() {
        inventory.setItem(48, playerMenuUtility.peekMenu() == null ? ItemUtility.createItem(Material.BARRIER, "&cClose", null, false) : ItemUtility.createItem(Material.ARROW, "&aGo Back", List.of("&7To " + playerMenuUtility.peekMenu().getMenuName()), false));
        inventory.setItem(49, Profile.getLevelingItem(player));
        inventory.setItem(50, ItemUtility.createItem(Material.ENCHANTED_BOOK, "&aQuest Log", List.of("&7Completing quests will reward you", "&7with &6Coins&7, &3Batzal Experience &7and", "&7more!", "", "&7Talk to &bQuest Masters &7located in", "&7games lobbies to accept quests."), false));
    }
}
