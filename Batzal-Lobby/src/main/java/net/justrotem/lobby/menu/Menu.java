package net.justrotem.lobby.menu;

import net.justrotem.data.player.PlayerData;
import net.justrotem.data.util.TextUtility;
import net.justrotem.lobby.hooks.PlayerManager;
import net.justrotem.lobby.utils.ItemUtility;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

public abstract class Menu implements InventoryHolder {

    protected final PlayerMenuUtility playerMenuUtility;
    protected final @NotNull Player player;
    protected PlayerData playerData;
    protected Inventory inventory;
    protected final ItemStack FILLER_GLASS;

    public Menu(PlayerMenuUtility playerMenuUtility) {
        this.FILLER_GLASS = ItemUtility.createItem(Material.GRAY_STAINED_GLASS_PANE, " ");
        this.playerMenuUtility = playerMenuUtility;
        this.player = this.playerMenuUtility.getOwner();
        this.playerData = PlayerManager.get(this.player.getUniqueId());
    }

    public abstract String getMenuName();

    public abstract int getSlots();

    public abstract boolean cancelAllClicks();

    public abstract void handleMenu(InventoryClickEvent event);

    public abstract void setMenuItems();

    public void open(Menu lastMenu) {
        this.inventory = Bukkit.createInventory(this, this.getSlots(), TextUtility.color(this.getMenuName()));
        this.setMenuItems();
        this.player.openInventory(this.inventory);
        if (lastMenu != null) {
            this.playerMenuUtility.pushMenu(lastMenu);
            this.reloadItems();
        }
    }

    public void back() {
        MenuManager.openMenu(this.playerMenuUtility.lastMenu().getClass(), this.player, null);
    }

    public void close() {
        playerMenuUtility.lastMenu();
        this.player.closeInventory();
    }

    protected void reloadItems() {
        for (int i = 0; i < this.inventory.getSize(); ++i) {
            this.inventory.setItem(i, null);
        }

        this.setMenuItems();
    }

    protected void reload() {
        this.player.closeInventory();
        MenuManager.openMenu(this.getClass(), this.player, playerMenuUtility.peekMenu());
    }

    public @NotNull Inventory getInventory() {
        return this.inventory;
    }

    public void setFillerGlass() {
        for (int i = 0; i < this.getSlots(); ++i) {
            if (this.inventory.getItem(i) == null) {
                this.inventory.setItem(i, this.FILLER_GLASS);
            }
        }

    }

    public void setFillerGlass(ItemStack itemStack) {
        for (int i = 0; i < this.getSlots(); ++i) {
            if (this.inventory.getItem(i) == null) {
                this.inventory.setItem(i, itemStack);
            }
        }

    }
}

