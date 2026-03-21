package net.justrotem.lobby.utils;

import io.papermc.paper.command.brigadier.CommandSourceStack;
import net.justrotem.data.player.PlayerData;
import net.justrotem.data.sql.DataServiceShutdownController;
import net.justrotem.data.bukkit.BukkitUtility;
import net.justrotem.data.util.TextUtility;
import net.justrotem.lobby.hooks.LuckPermsManager;
import net.justrotem.lobby.hooks.PlayerManager;
import net.justrotem.lobby.nick.NickManager;
import net.justrotem.lobby.skins.SkinManager;
import net.justrotem.lobby.sql.MySQL;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.LightningStrike;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.*;
import java.util.function.Consumer;
import java.util.function.Function;

public class PlayerUtility extends BukkitUtility {

    /**
     * Initializes the plugin utilities, connecting MySQL and initializing LuckPerms.
     *
     * @param plugin the JavaPlugin instance
     */
    public static void initialize(JavaPlugin plugin) {
        plugin.getLogger().info("Loading MySQL!");
        MySQL.connect(plugin);

        plugin.getLogger().info("Loading Skins!");
        SkinManager.loadSkinsFromYaml(plugin);

        // Initialize LuckPerms safely
        LuckPermsManager.init(plugin);

        PlayerManager.startAutoSave(plugin);
        NickManager.startAutoSave(plugin);
        SkinManager.startAutoSave(plugin);
    }

    /**
     * Shuts down the plugin utilities, saving player data and disconnecting MySQL.
     *
     * @param plugin the JavaPlugin instance
     */
    public static void shutdown(JavaPlugin plugin) {
        plugin.getLogger().info("Saving players data..");
        PlayerManager.saveAll();
        NickManager.saveAll();
        SkinManager.saveAll();

        DataServiceShutdownController.shutdownAndAwait();

        plugin.getLogger().info("Closing MySQL..");
        net.justrotem.data.sql.MySQL.disconnect();
    }

    /**
     * Converts a Mojang UUID string without dashes into a proper {@link UUID}.
     *
     * @param mojangUUID the UUID string (32 characters, no dashes)
     * @return the parsed {@link UUID}
     * @throws IllegalArgumentException if the UUID string length is invalid
     */
    public static UUID parseMojangUUID(String mojangUUID) {
        if (mojangUUID.length() != 32) {
            throw new IllegalArgumentException("Invalid UUID length: " + mojangUUID);
        }
        String dashed = mojangUUID.replaceFirst(
                "(\\w{8})(\\w{4})(\\w{4})(\\w{4})(\\w{12})",
                "$1-$2-$3-$4-$5"
        );
        return UUID.fromString(dashed);
    }

    /**
     * Checks whether a {@link CommandSourceStack} is the console.
     * Sends a message if it is not a player.
     *
     * @param source the command source
     * @return true if the source is not a player (console), false otherwise
     */
    public static boolean isConsole(CommandSourceStack source) {
        if (source.getSender() instanceof Player) return false;

        source.getSender().sendMessage(TextUtility.color("&cOnly in game players can use this command"));
        return true;
    }

    /**
     * Checks whether a {@link CommandSender} is the console.
     * Sends a message if it is not a player.
     *
     * @param sender the command source
     * @return true if the source is not a player (console), false otherwise
     */
    public static boolean isConsole(CommandSender sender) {
        return !(sender instanceof Player);
    }

    /**
     * Retrieves a {@link Player} by name if registered and online.
     *
     * @param sender the executor checking the target
     * @param name   the target player name
     * @return the online {@link Player} instance, or null if not found or not online
     */
    public static Player getTarget(CommandSender sender, String name) {
        if (!PlayerManager.isRegistered(name)) {
            if (sender != null) sender.sendMessage(TextUtility.playerNotFound(name));
            return null;
        }

        Player target = Bukkit.getPlayer(name);
        if (target == null) {
            if (sender != null) sender.sendMessage(TextUtility.playerNotOnline(name));
            return null;
        }
        return target;
    }

    /**
     * Retrieves a {@link Player} by name if registered and online.
     *
     * @param name   the target player name
     * @return the online {@link Player} instance, or null if not found or not online
     */
    public static Player getTarget(String name) {
        return getTarget(null, name);
    }

    /**
     * Retrieves a {@link PlayerData} by name if registered and online.
     *
     * @param player the executor checking the target
     * @param name   the target player name
     * @return {@link PlayerData} instance, or null if not found
     */
    public static PlayerData getOfflineTarget(Player player, String name) {
        if (!PlayerManager.isRegistered(name)) {
            if (player != null) player.sendMessage(TextUtility.playerNotFound(name));
            return null;
        }

        return PlayerManager.get(PlayerManager.getUniqueId(name));
    }

    /**
     * Retrieves a {@link PlayerData} by name if registered and online.
     *
     * @param name   the target player name
     * @return {@link PlayerData} instance, or null if not found
     */
    public static PlayerData getOfflineTarget(String name) {
        return getOfflineTarget(null, name);
    }

    public record TargetException(String message, String executorMessage) {
    }

    /**
     * Resolve the input arg to a list of Player targets.
     * index is 1-based as in your code.
     */
    private static Collection<Player> resolveTargets(CommandSender executor, String[] args, int index, String permission) {
        Collection<Player> targets = new ArrayList<>();

        String input = null;
        if (args != null && index >= 1 && permission != null && !permission.isEmpty()) {
            // Only use the provided argument if length == index AND executor has the permission
            input = args.length == index && executor.hasPermission(permission) ? args[index - 1] : "";
        }

        if ((input == null || input.isEmpty()) && !isConsole(executor)) {
            targets.add((Player) executor);
            return targets;
        }

        if (input != null && (input.equalsIgnoreCase("*") || input.equalsIgnoreCase("all"))) {
            targets.addAll(Bukkit.getOnlinePlayers());
            return targets;
        }

        Player target = getTarget(!isConsole(executor) ? null : executor, input);
        if (target == null) return Collections.emptyList();
        targets.add(target);
        return targets;
    }

    /**
     * Replace special placeholders and insert a staff Component where %staff% is found.
     * This returns an Adventure Component (so the message can contain hover events for staff).
     * <p>
     * Behavior:
     *  - Replaces %target%, %target-name%, %player%, %player-name% first.
     *  - Splits on literal "%staff%" and inserts a hoverable staff Component when
     *    target != executor. If the target does not have 'batzal.staff.see', the
     *    hover is NOT added (still inserts the text).
     */
    private static Component replaceStaffPlaceholder(CommandSender executor, Player target, String message) {
        if (message == null) return Component.empty();

        Component staffComponent = MiniMessage.miniMessage().deserialize(" <dark_aqua>(by a Staff Member)</dark_aqua>");
        if (target != null && target.hasPermission("batzal.staff.see")) {
            staffComponent = staffComponent.hoverEvent(HoverEvent.showText(TextUtility.color(isConsole(executor) ? "&c&lCONSOLE" : PlayerManager.getLegacyRealDisplayName((Player) executor))));
        }

        String replaced = message
                .replace("%target%", target == null ? "&c&lCONSOLE" : PlayerManager.getLegacyRealDisplayName(target))
                .replace("%target-name%", target == null ? "&c&lCONSOLE" : target.getName())
                .replace("%player%", isConsole(executor) ? "&c&lCONSOLE" : PlayerManager.getLegacyRealDisplayName((Player) executor))
                .replace("%player-name%", isConsole(executor) ? "&c&lCONSOLE" : executor.getName());

        String[] parts = replaced.split("%staff%", -1);
        if (parts.length == 0) return TextUtility.color(replaced);

        // Initialize result with the first part
        Component result = TextUtility.color(parts[0]);

        // Append remaining parts with staff components interleaved
        for (int i = 1; i < parts.length; i++) {
            if (target != null && !target.equals(executor)) {
                result = result.append(staffComponent);
            }
            if (!parts[i].isEmpty()) {
                result = result.append(TextUtility.color(parts[i]));
            }
        }

        return result;
    }

    /**
     * Replaces placeholders in a message with provided values.
     * <p>
     * Placeholders:
     * <ul>
     *     <li>%value% → first value if exists, or empty string if no values</li>
     *     <li>%value-1%, %value-2%, … → replaced in order according to values list</li>
     *     <li>If there are fewer values than indexed placeholders, remaining placeholders are cleared</li>
     * </ul>
     *
     * @param message The message containing placeholders
     * @param values  List of values to insert into the placeholders
     * @return The message with placeholders replaced
     */
    public static String fillValues(String message, List<String> values) {
        if (message == null) return "";

        String filled = message;

        if (values != null && !values.isEmpty()) {
            // Single value placeholder
            filled = filled.replace("%value%", values.getFirst());

            // Indexed placeholders
            for (int i = 0; i < values.size(); i++) {
                String placeholder = "%value-" + (i + 1) + "%";
                filled = filled.replace(placeholder, values.get(i));
            }
        }

        return filled;
    }


    /* ------------------------------------------------------------------ */
    /* Generic runTarget: supports Function<Player, T> where T can be:
     *  - String  -> single replacement into %value%
     *  - List<String> -> multiple replacements into %value-1% ... %value-N%
     *  - TargetException -> override messages for this target
     *
     * Rules for placeholders:
     *  - If there is exactly 1 value: placeholder is %value%
     *  - If >1 values: placeholders are %value-1%, %value-2%, ...
     */
    /**
     * Generic runTarget that accepts a Function<Player, T>.
     *
     * @param executor        The player executing the command or action
     * @param args            The command arguments array
     * @param index           The 1-based index in args to check for a target argument
     * @param permission      Permission required to use the argument as target
     * @param action          A function that takes the target Player and returns T (String or List<String> or TargetException)
     * @param message         Message sent to each target (supports placeholders)
     * @param executorMessage Message sent to executor if target != executor (supports placeholders)
     * @param <T>             Return type from action
     */
    public static <T> boolean runTarget(CommandSender executor, String[] args, int index, String permission, Function<Player, T> action, String message, String executorMessage) {
        Collection<Player> targets = resolveTargets(executor, args, index, permission);
        if (targets.isEmpty()) return false; // resolveTargets returns false if target not found

        for (Player target : targets) {
            T result = action.apply(target);
            if (result == null) return false;

            String thisMessage = message;
            String thisExecutorMessage = executorMessage;

            // Allow action to override messages via TargetException
            if (result instanceof TargetException te) {
                thisMessage = te.message();
                thisExecutorMessage = te.executorMessage();
            }

            // Build list of string values (could be single string or list)
            List<String> values = new ArrayList<>();
            if (result instanceof String s) {
                values.add(s);
            } else if (result instanceof List<?> list) {
                values.addAll(list.stream()
                        .filter(Objects::nonNull)
                        .filter(o -> o instanceof String || o instanceof Integer || o instanceof Boolean)
                        .map(String::valueOf).toList());
            }

            // for message replacement, operate on copies per-target
            if (thisMessage != null && !thisMessage.isEmpty()) {
                target.sendMessage(replaceStaffPlaceholder(executor, target, fillValues(thisMessage, values)));
            }

            if (thisExecutorMessage != null && !thisExecutorMessage.isEmpty() && !target.equals(executor)) {
                executor.sendMessage(replaceStaffPlaceholder(executor, target, fillValues(thisExecutorMessage, values))); // executor sees his own message (no staff component needed)
            }
        }

        return true;
    }

    /**
     * Consumer-based runTarget that also sends message/executorMessage (if provided).
     *
     * @param executor        The player executing the command
     * @param args            Command args
     * @param index           1-based index for target arg
     * @param permission      Permission required for using the target arg
     * @param action          Consumer action to run for each target
     * @param message         Message to send to each target after action (supports placeholders)
     * @param executorMessage Message to send to executor if target != executor (supports placeholders)
     */
    public static boolean runTarget(Player executor, String[] args, int index, String permission, Consumer<Player> action, String message, String executorMessage) {
        Collection<Player> targets = resolveTargets(executor, args, index, permission);
        if (targets.isEmpty()) return false;

        for (Player target : targets) {
            action.accept(target);

            if (message != null && !message.isEmpty()) {
                target.sendMessage(replaceStaffPlaceholder(executor, target, message));
            }

            if (!target.equals(executor) && executorMessage != null && !executorMessage.isEmpty()) {
                executor.sendMessage(replaceStaffPlaceholder(executor, executor, executorMessage));
            }
        }

        return true;
    }

    /**
     * Very simple Consumer-only runTarget (no messages).
     */
    public static boolean runTarget(Player executor, String[] args, int index, String permission, Consumer<Player> action) {
        return runTarget(executor, args, index, permission, action, null, null);
    }

    /**
     * Generic completion helper for any type of items.
     *
     * @param args      command args
     * @param length    the index to complete
     * @param arguments list to add completions to
     * @param items     items to suggest
     * @param mapper    how to convert item to String
     * @param <T>       type of items
     */
    private static <T> void addCompletion(String[] args, int length, List<String> arguments, T[] items, Function<T, String> mapper) {
        boolean first = length == 1 && args.length == 0;
        if (first || args.length == length) {
            arguments.addAll(Arrays.stream(items)
                    .map(mapper)
                    .filter(name -> first || name.toLowerCase().startsWith(args[length - 1].toLowerCase()))
                    .toList()
            );
        }
    }

    /**
     * Convenience overload for String arrays
     */
    public static void addCompletion(String[] args, int length, List<String> arguments, String... items) {
        addCompletion(args, length, arguments, items, s -> s);
    }

    /**
     * Convenience overload for Object arrays
     */
    public static void addCompletion(String[] args, int length, List<String> arguments, Object[] items) {
        addCompletion(args, length, arguments, items, Object::toString);
    }

    /**
     * Completion helper for online players with optional permission check.
     *
     * @param args       command args
     * @param length     index to complete
     * @param arguments  list to add completions to
     * @param source     command source stack
     * @param permission optional permission (nullable)
     */
    public static void addPlayerCompletion(String[] args, int length, List<String> arguments, CommandSourceStack source, String permission, boolean withSelf) {
        CommandSender sender = source.getSender();
        if (permission != null && !permission.isEmpty() && !sender.hasPermission(permission)) return;

        addCompletion(args, length, arguments, Bukkit.getOnlinePlayers().stream()
                .filter(player -> {
                    if (!withSelf) return player != sender;
                    return true;
                })
                .map(Player::getName).toArray()
        );
    }

    public static void addPlayerCompletion(String[] args, int length, List<String> arguments, CommandSourceStack source, String permission) {
        addPlayerCompletion(args, length, arguments, source, permission, false);
    }

    /**
     * Adds a lightning strike effect at the given location without fire or fall damage.
     *
     * @param location the strike location
     */
    public static void strikeLightningWithoutFire(Location location) {
        LightningStrike lightningStrike = location.getWorld().strikeLightningEffect(location);
        lightningStrike.setFireTicks(0);
        lightningStrike.setFallDistance(0);
    }
}
