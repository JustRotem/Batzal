package net.justrotem.lobby.utils;

import net.justrotem.data.player.PlayerManager;
import net.justrotem.data.player.PlayerData;
import net.justrotem.data.bukkit.SkinData;
import net.justrotem.data.utils.TextUtility;
import net.justrotem.lobby.skins.SkinManager;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Color;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.LeatherArmorMeta;
import org.bukkit.inventory.meta.SkullMeta;

import java.util.List;

public class ItemUtility {

    /**
     * Creates an {@link ItemStack} with customizable options.
     *
     * @param item          the item
     * @param name              display name
     * @param lore              lore lines
     * @param unbreakable       set item unbreakable
     * @param hideUnbreakable   hide the unbreakable flag
     * @param hideEnchants      hide item enchantments
     * @param hideAttributes    hide item attributes
     * @param glow              apply glow effect (Unbreaking 0)
     * @return configured {@link ItemStack}
     */
    public static ItemStack createItem(ItemStack item, String name, List<String> lore, boolean glow, boolean unbreakable, boolean hideUnbreakable, boolean hideEnchants, boolean hideAttributes) {
        ItemMeta meta = item.getItemMeta();

        meta.customName(TextUtility.color(name).decoration(TextDecoration.ITALIC, false));
        if (lore != null) meta.lore(lore.stream().map(line -> TextUtility.color(line).decoration(TextDecoration.ITALIC, false)).toList());

        if (unbreakable) meta.setUnbreakable(true);
        if (hideUnbreakable) meta.addItemFlags(ItemFlag.HIDE_UNBREAKABLE);
        if (hideEnchants) meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
        if (hideAttributes) meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
        if (glow) meta.addEnchant(Enchantment.UNBREAKING,  0, true);

        item.setItemMeta(meta);
        return item;
    }

    /**
     * Convenience overload for {@link #createItem(ItemStack, String, List, boolean, boolean, boolean, boolean, boolean)}.
     *
     * @param item the item
     * @param name     display name
     * @param lore     lore lines
     * @return configured {@link ItemStack} with default flags
     */
    public static ItemStack createItem(ItemStack item, String name, List<String> lore) {
        return createItem(item, name, lore, false, false, false, false, false);
    }

    /**
     * Convenience overload for {@link #createItem(ItemStack, String, List, boolean, boolean, boolean, boolean, boolean)}.
     *
     * @param item the item
     * @param name     display name
     * @param lore     lore lines
     * @return configured {@link ItemStack} with default flags
     */
    public static ItemStack createItem(ItemStack item, String name, String... lore) {
        return createItem(item, name, List.of(lore), false, false, false, false, false);
    }

    /**
     * Convenience overload for {@link #createItem(ItemStack, String, List, boolean, boolean, boolean, boolean, boolean)} with glow.
     *
     * @param item the item
     * @param name     display name
     * @param lore     lore lines
     * @param glow     whether to apply glow effect
     * @return configured {@link ItemStack}
     */
    public static ItemStack createItem(ItemStack item, String name, List<String> lore, boolean glow) {
        return createItem(item, name, lore, glow, false, false, glow, glow);
    }

    /**
     * Creates an {@link ItemStack} with customizable options.
     *
     * @param material          the item material
     * @param name              display name
     * @param lore              lore lines
     * @param unbreakable       set item unbreakable
     * @param hideUnbreakable   hide the unbreakable flag
     * @param hideEnchants      hide item enchantments
     * @param hideAttributes    hide item attributes
     * @param glow              apply glow effect (Unbreaking 0)
     * @return configured {@link ItemStack}
     */
    public static ItemStack createItem(Material material, String name, List<String> lore, boolean glow, boolean unbreakable, boolean hideUnbreakable, boolean hideEnchants, boolean hideAttributes) {
        return createItem(new ItemStack(material), name, lore, glow, unbreakable, hideUnbreakable, hideEnchants, hideAttributes);
    }

    /**
     * Convenience overload for {@link #createItem(Material, String, List, boolean, boolean, boolean, boolean, boolean)}.
     *
     * @param material the item material
     * @param name     display name
     * @param lore     lore lines
     * @return configured {@link ItemStack} with default flags
     */
    public static ItemStack createItem(Material material, String name, List<String> lore) {
        return createItem(material, name, lore, false, false, false, false, false);
    }

    /**
     * Convenience overload for {@link #createItem(Material, String, List, boolean, boolean, boolean, boolean, boolean)}.
     *
     * @param material the item material
     * @param name     display name
     * @param lore     lore lines
     * @return configured {@link ItemStack} with default flags
     */
    public static ItemStack createItem(Material material, String name, String... lore) {
        return createItem(material, name, List.of(lore), false, false, false, false, false);
    }

    /**
     * Convenience overload for {@link #createItem(Material, String, List, boolean, boolean, boolean, boolean, boolean)} with glow.
     *
     * @param material the item material
     * @param name     display name
     * @param lore     lore lines
     * @param glow     whether to apply glow effect
     * @return configured {@link ItemStack}
     */
    public static ItemStack createItem(Material material, String name, List<String> lore, boolean glow) {
        return createItem(material, name, lore, glow, false, false, true, true);
    }

    public static ItemStack createLeatherArmor(Material material, Color color) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();

        LeatherArmorMeta leatherArmorMeta = (LeatherArmorMeta) meta;
        leatherArmorMeta.setColor(color);
        meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);

        item.setItemMeta(leatherArmorMeta);
        return item;
    }

    private static ItemStack createHead(SkinData skinData, String name, List<String> lore) {
        // Create default Steve head
        ItemStack head = createItem(Material.PLAYER_HEAD, name, lore);

        // Load skin from cache
        if (skinData != null) {
            SkullMeta meta = (SkullMeta) head.getItemMeta();
            meta.setPlayerProfile(skinData.getProfile());
            head.setItemMeta(meta);
        }

        return head;
    }

    public static ItemStack createCustomHead(String skin, String name, List<String> lore) {
        // Load skin async (Cache)
        SkinData skinData = SkinManager.getHead(skin);
        return createHead(skinData, name, lore);
    }

    public static ItemStack createPlayerHead(Player player, String name, List<String> lore) {
        // Load skin from cache
        SkinData skinData = null;
        PlayerData playerData = PlayerManager.get(player.getUniqueId());
        if (playerData != null) {
            skinData = SkinData.create(playerData.getName(), playerData.getValue(), playerData.getSignature(), false);
            SkinManager.load(skinData);
        }

        return createHead(skinData, name, lore);
    }
}
