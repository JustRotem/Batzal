package net.justrotem.lobby;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

public interface RankColor {

    enum ColorItem {
        Red(new ItemStack(Material.RED_DYE)),
        Gold(new ItemStack(Material.ORANGE_DYE)),
        Green(new ItemStack(Material.LIME_DYE)),
        Yellow(new ItemStack(Material.YELLOW_DYE)),
        Light_Purple(new ItemStack(Material.PINK_DYE)),
        White(new ItemStack(Material.WHITE_DYE)),
        Blue(new ItemStack(Material.LIGHT_BLUE_DYE)),
        Dark_Green(new ItemStack(Material.GREEN_DYE)),
        Dark_Red(new ItemStack(Material.REDSTONE)),
        Dark_Aqua(new ItemStack(Material.CYAN_DYE)),
        Dark_Purple(new ItemStack(Material.PURPLE_DYE)),
        Dark_Gray(new ItemStack(Material.GRAY_DYE)),
        Black(new ItemStack(Material.INK_SAC)),
        Dark_Blue(new ItemStack(Material.BLUE_DYE));

        private final ItemStack item;

        ColorItem(ItemStack item) {
            this.item = item;
        }

        public ItemStack getItem() {
            return this.item;
        }
    }
}
