package net.justrotem.lobby.utils;

import io.papermc.paper.command.brigadier.BasicCommand;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import net.justrotem.data.util.TextUtility;
import net.justrotem.lobby.hooks.PlayerManager;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jspecify.annotations.Nullable;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.UUID;

/**
 * Utility class for managing total player experience independent of Minecraft's built-in behavior.
 * <p>
 * Minecraft stores XP as:
 *  - Level (integer)
 *  - Progress within the current level (0.0 – 1.0 float)
 * <p>
 * But servers often need **total accumulated XP**, so these methods convert between both systems.
 */
public class ExperienceManager {

    public static class LevelCommand implements BasicCommand {

        private final String command;

        public LevelCommand(String command) {
            this.command = command;
        }

        @Override
        public void execute(CommandSourceStack source, String[] args) {
            CommandSender sender = source.getSender();

            if (args.length >= 2) {
                int amount;
                try {
                    amount = Integer.parseInt(args[1]);
                } catch (NumberFormatException e) {
                    sender.sendMessage(TextUtility.color("&cThis is an invalid number!"));
                    return;
                }

                String command = this.command.equals("level") ? "Level" : "Experience";
                if (PlayerUtility.runTarget(sender, args, 3, permission() + ".others", target -> {
                    switch (args[0].toLowerCase()) {
                        case "set" -> {
                            if (this.command.equals("level")) setLevel(target, amount);
                            else setExp(target, amount);
                        }

                        case "give" -> {
                            if (this.command.equals("level")) giveLevel(target, amount);
                            else giveExp(target, amount);
                        }

                        case "take" -> {
                            if (this.command.equals("level")) takeLevel(target, amount);
                            else takeExp(target, amount);
                        }

                        default -> {
                            return new PlayerUtility.TargetException(null, "&cUsage: /level <set/give/take> <amount> <player>");
                        }
                    }

                    int level = target.getLevel();
                    String xp = target.getExp() != 0 ? (String.format("%,d", getCurrentExperience(target)) + " (" + getCurrentLevelPercentageAsText(target) + "%)") : "";

                    return List.of(String.valueOf(level), (sender instanceof Player player && target == player) ? xp : "");
                }, "&aChanged Network " + command + " to &3%value-1%&a!%staff%", "&aChanged %target% Network " + command + " to &3%value-1%&a!")) return;
            }

            sender.sendMessage(TextUtility.color("&cUsage: /level <set/give/take> <amount> <player>"));
        }

        @Override
        public @NotNull Collection<String> suggest(@NotNull CommandSourceStack source, String[] args) {
            List<String> arguments = new ArrayList<>();

            PlayerUtility.addCompletion(args, 1, arguments, "set", "give", "take");
            PlayerUtility.addPlayerCompletion(args, 3, arguments, source, permission() + ".others");

            return arguments;
        }

        @Override
        public @Nullable String permission() {
            return "batzal.level";
        }
    }

    /**
     * Gets the total experience value stored in PlayerData (server-side storage).
     */
    public static int getTotalExp(Player player) {
        return PlayerManager.get(player.getUniqueId()).getTotalExperience();
    }

    /**
     * Converts a Minecraft level into the total XP required to reach that level.
     * Uses vanilla XP curve formulas:
     *  - 0–15:   xp = level² + 6·level
     *  - 16–30:  xp = 2.5·level² − 40.5·level + 360
     *  - 31+:    xp = 4.5·level² − 162.5·level + 2220
     */
    public static int getTotalExperience(int level) {
        if (level >= 0 && level <= 15) {
            return (int) Math.round(Math.pow(level, 2) + 6 * level);
        } else if (level > 15 && level <= 30) {
            return (int) Math.round((2.5 * Math.pow(level, 2) - 40.5 * level + 360));
        } else if (level > 30) {
            return (int) Math.round(((4.5 * Math.pow(level, 2) - 162.5 * level + 2220)));
        }

        return 0;
    }

    /**
     * Converts stored total XP into the player's exact level.
     * Works downward: subtracts required XP per level until the remaining XP is negative.
     */
    public static int getLevel(UUID uuid) {
        var points = PlayerManager.get(uuid).getTotalExperience();
        var level = 0;

        while (points >= 0) {
            if (level < 16) points -= (2 * level) + 7;
            else if (level < 31) points -= (5 * level) - 38;
            else points -= (9 * level) - 158;

            level++;
        }
        return level - 1;
    }

    /**
     * Gets the total XP the player currently has (computed live from level + progress).
     */
    public static int getTotalExperience(Player player) {
        return Math.round(player.getExp() * player.getExpToLevel()) + getTotalExperience(player.getLevel());
    }

    /**
     * Sets a player's total XP by calculating correct level and bar progress.
     */
    public static void setTotalExperience(Player player, int amount) {
        float a = 0, b = 0, c = -amount;

        // Solve XP curve using quadratic formula to find level from total XP
        if (amount > getTotalExperience(0) && amount <= getTotalExperience(15)) {
            a = 1;
            b = 6;
        } else if (amount > getTotalExperience(15) && amount <= getTotalExperience(30)) {
            a = 2.5f;
            b = -40.5f;
            c += 360;
        } else if (amount > getTotalExperience(30)) {
            a = 4.5f;
            b = -162.5f;
            c += 2220;
        }

        int level = (int) Math.floor((-b + Math.sqrt(Math.pow(b, 2) - (4 * a * c))) / (2 * a));

        player.setLevel(level);
        player.setExp(0);
        player.giveExp(amount - getTotalExperience(level));
    }

    /**
     * Syncs and stores the player's live XP into PlayerData.
     */
    public static void updateTotalExperience(Player player) {
        PlayerManager.get(player.getUniqueId()).setTotalExperience(getTotalExperience(player));
    }

    /**
     * XP needed to complete current level (client-side progress).
     */
    public static int getExpToLevelUp(Player player) {
        return Math.round((1 - player.getExp()) * player.getExpToLevel());
    }

    /**
     * Percentage of current level progress (0–100).
     */
    public static float getCurrentLevelPercentage(Player player) {
        return player.getExp() * 100;
    }

    /**
     * A scaled conversion of current level progress (useful for progress bars).
     */
    public static int getCurrentLevelPercentage(Player player, int times) {
        return Math.round(times * (getCurrentLevelPercentage(player) / 100));
    }

    public static int getCurrentLevelUpPercentage(Player player, int times) {
        return Math.round(times * (1 - (getCurrentLevelPercentage(player) / 100)));
    }

    public static String getCurrentLevelPercentageAsText(Player player) {
        return new DecimalFormat("#").format(getCurrentLevelPercentage(player));
    }

    /**
     * Sets progress on current level and automatically levels up if full.
     */
    public static void setExp(Player player, int amount) {
        float exp = (float) amount / levelPlaceholder(player);

        if (exp >= 1) {
            player.setExp(exp - 1);
            player.setLevel(player.getLevel() + 1);
        } else player.setExp(exp);

        updateTotalExperience(player);
    }

    /**
     * Adds progress on current level.
     */
    public static void giveExp(Player player, int amount) {
        setExp(player, getCurrentExperience(player) + amount);
    }

    /**
     * Takes progress on current level.
     */
    public static void takeExp(Player player, int amount) {
        setExp(player, Math.max(getCurrentExperience(player) - amount, 0));
    }

    /**
     * Sets exact level and updates total XP.
     */
    public static void setLevel(Player player, int level) {
        player.setLevel(level);
        updateTotalExperience(player);
    }

    /**
     * Adds levels and syncs XP storage.
     */
    public static void giveLevel(Player player, int level) {
        player.giveExpLevels(level);
        updateTotalExperience(player);
    }

    /**
     * Takes levels and syncs XP storage.
     */
    public static void takeLevel(Player player, int level) {
        player.giveExpLevels(-level);
        updateTotalExperience(player);
    }

    /**
     * Stores the player's current total XP into PlayerData
     * (useful after modifications from Bukkit API).
     */
    public static void setCurrentTotalExperience(Player player) {
        setTotalExperience(player, getTotalExp(player));
    }

    /**
     * Custom UI display XP formula (your server’s leveling system).
     * Used for showing a progress bar unrelated to vanilla leveling.
     */
    public static int levelPlaceholder(int level) {
        return Math.max(((level - 1) * 2500) + 10000, 10000);
    }

    /**
     * Custom UI display XP formula (your server’s leveling system).
     * Used for showing a progress bar unrelated to vanilla leveling.
     */
    public static int levelPlaceholder(Player player) {
        return levelPlaceholder(player.getLevel());
    }

    /**
     * XP required to level up in your custom UI system.
     */
    public static int getExperienceToLevelUp(Player player) {
        return Math.round((1 - player.getExp()) * levelPlaceholder(player));
    }

    /**
     * XP for current level in your custom UI system.
     */
    public static int getCurrentExperience(Player player) {
        return Math.round(player.getExp() * levelPlaceholder(player));
    }
}