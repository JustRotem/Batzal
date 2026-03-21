package net.justrotem.lobby.menu;

import net.justrotem.lobby.utils.ItemUtility;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.List;

public abstract class PaginatedMenu extends Menu {
    protected List<Object> data;
    protected int page = 0;
    protected int maxItemsPerPage = 28;
    protected int index = 0;

    public PaginatedMenu(PlayerMenuUtility playerMenuUtility) {
        super(playerMenuUtility);
    }

    //public abstract List<?> getData();
    public abstract List<Object> getData();

    public abstract void loopCode(Object var1);

    public abstract @Nullable HashMap<Integer, ItemStack> getCustomMenuBorderItems();

    protected void addMenuBorder() {
        this.inventory.setItem(48, ItemUtility.createItem(Material.OAK_BUTTON, "&aLeft"));
        this.inventory.setItem(49, ItemUtility.createItem(Material.BARRIER, "&cClose"));
        this.inventory.setItem(50, ItemUtility.createItem(Material.OAK_BUTTON, "&aRight"));

        int i;
        for (i = 0; i < 10; ++i) {
            if (this.inventory.getItem(i) == null) {
                this.inventory.setItem(i, super.FILLER_GLASS);
            }
        }

        this.inventory.setItem(17, super.FILLER_GLASS);
        this.inventory.setItem(18, super.FILLER_GLASS);
        this.inventory.setItem(26, super.FILLER_GLASS);
        this.inventory.setItem(27, super.FILLER_GLASS);
        this.inventory.setItem(35, super.FILLER_GLASS);
        this.inventory.setItem(36, super.FILLER_GLASS);

        for (i = 44; i < 54; ++i) {
            if (this.inventory.getItem(i) == null) {
                this.inventory.setItem(i, super.FILLER_GLASS);
            }
        }

        if (this.getCustomMenuBorderItems() != null) {
            this.getCustomMenuBorderItems().forEach((integer, itemStack) -> {
                this.inventory.setItem(integer, itemStack);
            });
        }

    }

    public void setMenuItems() {
        this.addMenuBorder();
        List<Object> data = this.getData();
        if (data != null && !data.isEmpty()) {
            for (int i = 0; i < this.getMaxItemsPerPage(); ++i) {
                this.index = this.getMaxItemsPerPage() * this.page + i;
                Bukkit.getLogger().info(String.valueOf(this.index));
                if (this.index >= data.size()) {
                    break;
                }

                if (data.get(this.index) != null) {
                    this.loopCode(data.get(this.index));
                }
            }
        }

    }

    public boolean prevPage() {
        if (this.page == 0) {
            return false;
        } else {
            --this.page;
            this.reloadItems();
            return true;
        }
    }

    public boolean nextPage() {
        if (this.index + 1 < this.getData().size()) {
            ++this.page;
            this.reloadItems();
            return true;
        } else {
            return false;
        }
    }

    public int getMaxItemsPerPage() {
        return this.maxItemsPerPage;
    }
}

