package net.justrotem.game.utils;

import io.papermc.paper.command.brigadier.CommandSourceStack;
import net.justrotem.data.PlayerManager;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.jspecify.annotations.NonNull;

import java.util.List;
import java.util.UUID;

public class Utility {

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

    public static boolean isConsole(@NonNull CommandSourceStack source) {
        if (source.getSender() instanceof Player) return false;

        source.getSender().sendMessage(TextUtils.color("&cOnly in game players can use this command"));
        return true;
    }

    public static Player getTargetNonNull(@NonNull Player player, @NonNull String name) {
        if (!PlayerManager.isNameRegistered(name)) {
            player.sendMessage(TextUtils.playerNotFound(name));
            return null;
        }

        Player target = Bukkit.getPlayer(name);
        if (target == null) {
            player.sendMessage(TextUtils.playerNotOnline(name));
            return null;
        }
        return target;
    }

    public static void addArguments(List<String> arguments, String search, String... args) {
        for (String s : args) {
            if (s.toLowerCase().startsWith(search)) arguments.add(s);
        }
    }

    public static void addPlayersToTabComplete(List<String> arguments, String search, CommandSourceStack source, String permission) {
        if (permission != null && !permission.isEmpty() && !source.getSender().hasPermission(permission)) return;

        arguments.addAll(Bukkit.getOnlinePlayers().stream()
                .map(Player::getName)
                .filter(name -> !name.equalsIgnoreCase(source.getSender().getName()))
                .filter(name -> search == null || name.toLowerCase().startsWith(search))
                .toList());
    }

    public static void addPlayersCompletion(String[] args, int length, List<String> arguments, CommandSourceStack source, String permission) {
        if (args.length == length - 1) addPlayersToTabComplete(arguments, null, source, permission);

        if (args.length == length) {
            String search = args[length - 1].toLowerCase();
            Utility.addPlayersToTabComplete(arguments, search, source, permission);
        }
    }
}
