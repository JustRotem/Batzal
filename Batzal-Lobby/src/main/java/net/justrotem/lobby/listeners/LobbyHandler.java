package net.justrotem.lobby.listeners;

import net.justrotem.data.utils.CooldownManager;
import net.justrotem.data.utils.TextUtility;
import net.justrotem.lobby.Main;
import net.justrotem.lobby.menu.MenuManager;
import net.justrotem.lobby.menu.menus.GameMenu;
import net.justrotem.lobby.menu.menus.LobbySelector;
import net.justrotem.lobby.menu.menus.Profile;
import net.justrotem.lobby.utils.ItemUtility;
import net.justrotem.lobby.utils.Visibility;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;

import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.List;

public class LobbyHandler implements Listener {

    public static ItemStack GAME_MENU = ItemUtility.createItem(Material.COMPASS, "&aGame Menu &7(Right Click)", "&7Right Click to bring up the Game Menu!");

    public static ItemStack LOBBY_SELECTOR = ItemUtility.createItem(Material.NETHER_STAR, "&aLobby Selector &7(Right Click)", List.of("&7Right-click to switch between different lobbies!", "&7Use this to stay with your friends."));

    public static ItemStack createProfile(Player player) {
        return ItemUtility.createPlayerHead(player, "&aMy Profile &7(Right Click)", List.of("&7Right-click to browse quests, view achievements,", "&7activate Network Boosters and more!"));
    }

    public static ItemStack VISIBLE_PLAYERS = ItemUtility.createItem(Material.LIME_DYE, "&fPlayers: &aVisible &7(Right Click)", "&7Right-click to toggle player visibility!");
    public static ItemStack HIDDEN_PLAYERS = ItemUtility.createItem(Material.GRAY_DYE, "&fPlayers: &cHidden &7(Right Click)", "&7Right-click to toggle player visibility!");

    public static void openMenu(Player player, ItemStack item, boolean rightClick) {
        if (item.isSimilar(GAME_MENU)) {
            MenuManager.openMenu(GameMenu.class, player, null);
            return;
        }

        if (item.isSimilar(LOBBY_SELECTOR)) {
            MenuManager.openMenu(LobbySelector.class, player, null);
            return;
        }

        if (TextUtility.getText(item.getItemMeta().customName()).equals(TextUtility.getText(createProfile(player).getItemMeta().customName()))) {
            Profile.removeTarget(player.getUniqueId());
            MenuManager.openMenu(Profile.class, player, null);
            return;
        }

        if (rightClick && (item.isSimilar(VISIBLE_PLAYERS) || item.isSimilar(HIDDEN_PLAYERS))) {
            if (!CooldownManager.isReady(player.getUniqueId(), Main.CooldownCategory.Visibility)) {
                player.sendMessage(TextUtility.color("&cYou must wait &e3s &cbetween uses!"));
                return;
            }

            if (item.isSimilar(VISIBLE_PLAYERS)) {
                Visibility.hide(player);
                player.sendMessage(TextUtility.color("&cPlayer visibility disabled!"));
            } else {
                Visibility.show(player);
                player.sendMessage(TextUtility.color("&aPlayer visibility enabled!"));
            }

            CooldownManager.startCooldown(player.getUniqueId(), Main.CooldownCategory.Visibility, Duration.of(3, ChronoUnit.SECONDS));
            player.getInventory().setItem(7, item.isSimilar(VISIBLE_PLAYERS) ? HIDDEN_PLAYERS : VISIBLE_PLAYERS);
        }
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        ItemStack item = event.getItem();

        if (event.getHand() != EquipmentSlot.HAND || item == null || item.getType() == Material.AIR || event.getAction() == Action.PHYSICAL) return;

        openMenu(player, item, event.getAction().isRightClick());
    }

    @EventHandler(priority = EventPriority.LOW)
    public void click(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player player)) return;

        ItemStack item = event.getCurrentItem();
        if (item == null || item.getType() == Material.AIR) return;

        openMenu(player, item, event.isRightClick());
        event.setCancelled(true);
    }
}
