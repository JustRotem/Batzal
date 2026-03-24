package net.justrotem.data.enums;

/**
 * Defines rank color configurations.
 */
public class RankColor {

    /**
     * Represents main rank colors with metadata.
     */
    public enum Color {
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

        /**
         * Returns legacy color code.
         */
        public String getColorCode() {
            return "&" + this.colorCode;
        }

        /**
         * Returns weight for sorting.
         */
        public int getWeight() {
            return this.weight;
        }

        /**
         * Returns required level for this color.
         */
        public int getLevel() {
            return this.level;
        }
    }

    /**
     * Represents prefix colors.
     */
    public enum PrefixColor {
        Gold('6'),
        Aqua('b');

        private final char colorCode;

        PrefixColor(char colorCode) {
            this.colorCode = colorCode;
        }

        /**
         * Returns legacy color code.
         */
        public String getColorCode() {
            return "&" + colorCode;
        }
    }
}
