package net.justrotem.data.bukkit;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

public interface RankColor {

    enum Color {
        Red('c', 0, 0),
        Gold('6', 1, 35),
        Green('a', 2, 45),
        Yellow('e', 3, 55),
        Light_Purple('d', 4, 65),
        White('f', 5, 75),
        Blue('9', 6, 85),
        Dark_Green('2', 7, 95),
        Dark_Red('4', 8, 150),
        Dark_Aqua('3', 9, 150),
        Dark_Purple('5', 10, 200),
        Dark_Gray('8', 11, 200),
        Black('0', 12, 250),
        Dark_Blue('1', 13, -1);

        private final char colorCode;
        private final int weight;
        private final int level;

        Color(char colorCode, int weight, int level) {
            this.colorCode = colorCode;
            this.weight = weight;
            this.level = level;
        }

        public String getColorCode() {
            return "&" + this.colorCode;
        }

        public int getWeight() {
            return this.weight;
        }

        public int getLevel() {
            return this.level;
        }
    }

    enum PrefixColor {
        Gold('6'),
        Aqua('b');

        private final char colorCode;

        PrefixColor(char colorCode) {
            this.colorCode = colorCode;
        }

        public String getColorCode() {
            return "&" + colorCode;
        }
    }

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
