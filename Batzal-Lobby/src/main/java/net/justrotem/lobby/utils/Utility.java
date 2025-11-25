package net.justrotem.lobby.utils;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.ProtocolManager;
import com.comphenix.protocol.events.PacketContainer;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import net.justrotem.data.PlayerManager;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.LightningStrike;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jspecify.annotations.NonNull;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

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
    public static <T> void addCompletion(String[] args, int length, List<String> arguments, T[] items, Function<T, String> mapper) {
        String search = (args.length == length) ? args[length - 1].toLowerCase() : null;
        if (args.length == length || args.length == length - 1) {
            arguments.addAll(Arrays.stream(items)
                    .map(mapper)
                    .filter(Objects::nonNull)
                    .filter(s -> search == null || s.toLowerCase().startsWith(search))
                    .sorted()
                    .distinct()
                    .toList());
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
    public static void addPlayerCompletion(String[] args, int length, List<String> arguments, CommandSourceStack source, String permission) {
        CommandSender sender = source.getSender();
        String search = (args.length == length) ? args[length - 1].toLowerCase() : null;
        if (args.length != length && args.length != length - 1) return;
        if (permission != null && !permission.isEmpty() && !sender.hasPermission(permission)) return;

        arguments.addAll(Bukkit.getOnlinePlayers().stream()
                .map(Player::getName)
                .filter(name -> !name.equalsIgnoreCase(sender.getName()))
                .filter(name -> search == null || name.toLowerCase().startsWith(search))
                .sorted()
                .distinct()
                .toList());
    }

    public static void playExplosion(Location location, int power, Collection<Player> players) {
        ProtocolManager pm = ProtocolLibrary.getProtocolManager();
        PacketContainer packet = pm.createPacket(PacketType.Play.Server.EXPLOSION);
        packet.getModifier().writeDefaults();
        packet.getDoubles().write(0, location.getX()).write(1, location.getY()).write(2, location.getZ());
        packet.getFloat().write(0, (float) power);
        players.forEach(online -> {
            pm.sendServerPacket(online, packet);
        });
    }

    public static void playExplosion(Location location, int power) {
        playExplosion(location, power, location.getWorld().getPlayers());
    }

    public static void strikeLightningWithoutFire(Location location) {
        LightningStrike lightningStrike = location.getWorld().strikeLightningEffect(location);
        lightningStrike.setFireTicks(0);
        lightningStrike.setFallDistance(0);
    }

    public static ItemStack createItem(Material material, String name, List<String> lore, boolean unbreakable, boolean hideUnbreakable, boolean hideEnchants, boolean hideAttributes, boolean glow) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();

        meta.customName(TextUtils.color(name));
        if (lore != null) meta.lore(lore.stream().map(TextUtils::color).toList());

        if (unbreakable) meta.setUnbreakable(true);
        if (hideUnbreakable) meta.addItemFlags(ItemFlag.HIDE_UNBREAKABLE);
        if (hideEnchants) meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
        if (hideAttributes) meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
        if (glow) meta.addEnchant(Enchantment.UNBREAKING,  0, true);

        item.setItemMeta(meta);
        return item;
    }

    public static ItemStack createItem(Material material, String name, List<String> lore) {
        return createItem(material, name, lore, false, false, false, false, false);
    }

    public static ItemStack createItem(Material material, String name, List<String> lore, boolean glow) {
        return createItem(material, name, lore, false, false, true, true, glow);
    }
}
