package net.justrotem.lobby.menu.menus;

import net.justrotem.data.player.PlayerData;
import net.justrotem.data.util.TextUtility;
import net.justrotem.lobby.hooks.LuckPermsManager;
import net.justrotem.lobby.hooks.PlayerManager;
import net.justrotem.lobby.menu.Menu;
import net.justrotem.lobby.menu.PlayerMenuUtility;
import net.justrotem.lobby.utils.ExperienceManager;
import net.justrotem.lobby.utils.ItemUtility;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

import java.util.List;
import java.util.Objects;

public class RankColor extends Menu implements net.justrotem.data.bukkit.RankColor {

    private final PlayerData playerData;

    public RankColor(PlayerMenuUtility playerMenuUtility) {
        super(playerMenuUtility);
        this.playerData = PlayerManager.get(player.getUniqueId());
    }

    @Override
    public String getMenuName() {
        return "Rank Color";
    }

    @Override
    public int getSlots() {
        return 45;
    }

    @Override
    public boolean cancelAllClicks() {
        return false;
    }

    @Override
    public void handleMenu(InventoryClickEvent event) {
        if (event.getSlot() == 40) {
            if (playerMenuUtility.peekMenu() != null) this.back();
            else this.close();
            return;
        }

        for (Color color : Color.values()) {
            if (event.getCurrentItem().equals(getColorItem(color))) {
                selectColor(color);
                close();
                break;
            }
        }

        if (event.getCurrentItem().equals(getToggleItem())) {
            toggleColor();
            close();
        }
    }

    @Override
    public void setMenuItems() {
        inventory.setItem(10, getColorItem(Color.Red));
        inventory.setItem(11, getColorItem(Color.Gold));
        inventory.setItem(12, getColorItem(Color.Green));
        inventory.setItem(13, getColorItem(Color.Yellow));
        inventory.setItem(14, getColorItem(Color.Light_Purple));
        inventory.setItem(15, getColorItem(Color.White));
        inventory.setItem(16, getColorItem(Color.Blue));
        inventory.setItem(19, getColorItem(Color.Dark_Green));
        inventory.setItem(20, getColorItem(Color.Dark_Red));
        inventory.setItem(21, getColorItem(Color.Dark_Aqua));
        inventory.setItem(22, getColorItem(Color.Dark_Purple));
        inventory.setItem(23, getColorItem(Color.Dark_Gray));
        inventory.setItem(24, getColorItem(Color.Black));
        inventory.setItem(25, getColorItem(Color.Dark_Blue));

        inventory.setItem(40, playerMenuUtility.peekMenu() == null ? ItemUtility.createItem(Material.BARRIER, "&cClose", null, false) : ItemUtility.createItem(Material.ARROW, "&aGo Back", List.of("&7To " + playerMenuUtility.peekMenu().getMenuName()), false));

        if (player.hasPermission("batzal.rankcolor.toggleprefixcolor"))
            inventory.setItem(44, getToggleItem());
    }

    private ItemStack getColorItem(Color color) {
        return ItemUtility.createItem(ColorItem.valueOf(color.name()).getItem(), getItemName(color), getLore(color), isSelected(color));
    }

    private String getItemName(Color color) {
        return (hasUnlocked(color) ? "&a" : "&c") + getName(color) + " Rank Color";
    }

    private String getName(Color color) {
        return color.name().replace("_", " ");
    }

    private List<String> getLore(Color color) {
        List<String> notUnlocked;
        if (isSelected(color)) notUnlocked = List.of("&aCurrently selected!");
        else if (hasUnlocked(color)) notUnlocked = List.of("&eClick to select!");
        else {
            if (Objects.requireNonNull(color) == Color.Dark_Blue) {
                notUnlocked = List.of("&6Unlocking by claiming 100 Ranks Gifted Reward!");
            } else {
                notUnlocked = List.of("&3Unlocked at Network Level " + color.getLevel());
            }
        }

        if (color.equals(Color.Red))
            return notUnlocked.size() == 2 ? List.of("&7The default color for &bMVP&c+&7.", "", notUnlocked.get(0), notUnlocked.get(1)) : List.of("&7The default color for &bMVP&c+&7.", "", notUnlocked.get(0));

        return notUnlocked.size() == 2 ?
                List.of(
                        "&7Changes the color of the plus in &bMVP&c+",
                        "&7to " + getName(color) + ", turning it into &bMVP" + color.getColorCode() + "+",
                        "",
                        "&7Shown in tab list also when chatting",
                        "&7and joining lobbies.",
                        "",
                        notUnlocked.get(0),
                        notUnlocked.get(1)) :
                List.of(
                        "&7Changes the color of the plus in &bMVP&c+",
                        "&7to " + getName(color) + ", turning it into &bMVP" + color.getColorCode() + "+",
                        "",
                        "&7Shown in tab list also when chatting",
                        "&7and joining lobbies.",
                        "",
                        notUnlocked.get(0));
    }

    private boolean isSelected(Color color) {
        return playerData.getRankColor().equals(color);
    }

    private boolean hasUnlocked(Color color) {
        return player.hasPermission("batzal.rankcolor.bypass") || ((ExperienceManager.getLevel(player.getUniqueId()) >= color.getLevel() && color.getLevel() != -1) || (color.getLevel() == -1 && player.hasPermission("batzal.rankcolor." + color.name().toLowerCase())));
    }

    private void selectColor(Color color) {
        if (!hasUnlocked(color)) {
            player.sendMessage(TextUtility.color("&cYou haven't unlocked (or claimed) that yet!"));
            player.playSound(player.getLocation(), Sound.ENTITY_ENDERMAN_TELEPORT, 5, 0);
            return;
        }

        if (playerData.getRankColor().equals(color)) {
            player.sendMessage(TextUtility.color("&cYou already have that selected!"));
            player.playSound(player.getLocation(), Sound.ENTITY_ENDERMAN_TELEPORT, 5, 0);
            return;
        }

        playerData.setRankColor(color);
        updatePrefix(playerData.getRankColor());

        player.sendMessage(TextUtility.color("&aSelected!"));
        player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, 5, -5);
    }

    private void toggleColor() {
        PrefixColor prefixColor = playerData.getPrefixColor();

        playerData.setPrefixColor(prefixColor.equals(PrefixColor.Gold) ? PrefixColor.Aqua : PrefixColor.Gold);
        updatePrefix(playerData.getRankColor());

        player.sendMessage(TextUtility.color("&aChanged your prefix color to %color%&a!".replace("%color%", (prefixColor.equals(PrefixColor.Gold) ? "&bAqua" : "&6Gold"))));
    }

    private ItemStack getToggleItem() {
        PrefixColor prefixColor = playerData.getPrefixColor();
        return ItemUtility.createItem(Material.NETHER_STAR, "&aToggle Prefix Color", List.of(
                "&7Selected: " + (prefixColor.equals(PrefixColor.Gold) ? "&6Gold" : "&bAqua"),
                "&7",
                "&7Click to change the color to " + (prefixColor.equals(PrefixColor.Gold) ? "&bAqua" : "&6Gold")
        ), false);
    }

    private void updatePrefix(Color color) {
        int mvpPlusWeight = LuckPermsManager.getGroupWeight("mvp+");
        int mvpPlusPlusWeight = LuckPermsManager.getGroupWeight("mvp++");

        if (LuckPermsManager.getPrimaryGroup(player).equals("mvp++")) {
            if (color == RankColor.Color.Red && playerData.getPrefixColor() == PrefixColor.Gold) LuckPermsManager.removePrefixes(player.getUniqueId(), mvpPlusPlusWeight);
            else {
                String PREFIX_COLOR = playerData.getPrefixColor().getColorCode();
                LuckPermsManager.setPrefix(player.getUniqueId(), PREFIX_COLOR + "[MVP" + playerData.getRankColor().getColorCode() + "++" + PREFIX_COLOR + "] " + PREFIX_COLOR, mvpPlusPlusWeight);
            }
        } else if (LuckPermsManager.getPrimaryGroup(player).equals("mvp+")) {
            if (color == RankColor.Color.Red) LuckPermsManager.removePrefixes(player.getUniqueId(), mvpPlusWeight);
            else LuckPermsManager.setPrefix(player.getUniqueId(), "&b[MVP" + color.getColorCode() + "+&b] &b", mvpPlusWeight);
        } else {
            LuckPermsManager.removePrefixes(player.getUniqueId(), mvpPlusWeight);
            LuckPermsManager.removePrefixes(player.getUniqueId(), mvpPlusPlusWeight);
        }
    }
}
