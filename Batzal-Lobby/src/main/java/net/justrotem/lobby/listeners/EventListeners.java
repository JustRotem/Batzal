package net.justrotem.lobby.listeners;

import net.justrotem.data.cache.CooldownManager;
import net.justrotem.data.util.CooldownManager;
import net.justrotem.data.util.TextFormatter;
import net.justrotem.data.util.TextUtility;
import net.justrotem.lobby.Main;
import net.justrotem.lobby.commands.*;
import net.justrotem.lobby.hooks.LuckPermsManager;
import net.justrotem.lobby.hooks.PlayerManager;
import net.justrotem.lobby.menu.MenuManager;
import net.justrotem.lobby.menu.menus.PunchMessages;
import net.justrotem.lobby.menu.menus.Rewards;
import net.justrotem.lobby.nick.NickManager;
import net.justrotem.lobby.utils.LobbyManager;
import net.justrotem.lobby.utils.Visibility;
import net.justrotem.lobby.vanish.VanishManager;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.title.Title;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.*;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryMoveItemEvent;
import org.bukkit.event.player.*;
import org.bukkit.event.weather.ThunderChangeEvent;
import org.bukkit.event.weather.WeatherChangeEvent;
import org.bukkit.event.world.TimeSkipEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector;

import java.time.Duration;
import java.time.temporal.ChronoUnit;

public class EventListeners implements Listener {

    @EventHandler
    public void join(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        PlayerManager.registerPlayer(player);

        VanishManager.updateVanishedPlayers(player);
        Visibility.updatePlayers(player);

        if (NickManager.isNicked(player)) {
            String nickname = NickManager.getNickName(player);
            if (nickname != null) {
                if (NickManager.isNameRestricted(player, nickname, true)) {
                    NickManager.resetNick(player);
                    player.sendMessage(TextUtility.color("&cThere was a problem with your last nickname. &eChange your nickname using /nick"));
                } else {
                    NickManager.reuseNick(player);
                    player.sendMessage(TextUtility.color("&aYou are now nicked as %name%!".replace("%name%", nickname)));
                }
            }
        }

        Component joinMessage = null;

        if (!NickManager.isLobbyNicked(player)) {
            ConfigurationSection section = Main.getInstance().getConfig().getConfigurationSection("join-message");
            if (section != null) for (String rank : section.getKeys(false)) {
                    if (LuckPermsManager.getPrimaryGroup(player.getUniqueId()).equalsIgnoreCase(rank)) {
                        String message = section.getString(rank);
                        if (message == null) continue;

                        joinMessage = TextUtility.color(message.replace("%player%", player.getName()).replace("%displayname%", PlayerManager.getLegacyDisplayName(player)));
                        break;
                    }
                }
        }

        event.joinMessage(!VanishManager.isInvisible(player) ? joinMessage : null);
    }

    @EventHandler
    public void quit(PlayerQuitEvent event) {
        Player player = event.getPlayer();

        NickManager.resetInGameRank(player);

        PlayerManager.saveAndRemove(player.getUniqueId());
        NickManager.saveAndRemove(player.getUniqueId());

        event.quitMessage(null);
    }

    @EventHandler(priority=EventPriority.LOWEST)
    public void punch(EntityDamageByEntityEvent event) {
        if (WarCommand.isWarMode()) return;

        event.setCancelled(true);
        if (!(event.getDamager() instanceof Player player && event.getEntity() instanceof Player target)) return;

        if (VanishManager.isInvisible(player) || VanishManager.isInvisible(target)) return;

        if (PlayerManager.get(target.getUniqueId()).isTogglePunch()) return;

        if (!LuckPermsManager.hasPermission(player, "batzal.punch.puncher")) return;
        if (!LuckPermsManager.hasPermission(target, "batzal.punch.punched")) return;

        if (!CooldownManager.isReady(player.getUniqueId(), Main.CooldownCategory.Punch)) {
            player.sendMessage(TextFormatter.color("&cYou have to wait %cooldown% more seconds!".replace("%cooldown%", String.valueOf(CooldownManager.getRemaining(player.getUniqueId(), Main.CooldownCategory.Punch)))));
            return;
        }

        CooldownManager.startCooldown(player.getUniqueId(), Main.CooldownCategory.Punch, Duration.of(1, ChronoUnit.MINUTES));

        if (target.isFlying()) target.setFlying(false);
        target.getLocation().getWorld().createExplosion(target.getLocation(), 5F, false, false);
        target.setVelocity(new Vector(0.0D, 10.0, 0.0D));

        for (Player p : Bukkit.getOnlinePlayers()) {
            p.sendMessage(TextUtility.color(PunchMessages.getPunchMessages(player.getUniqueId()).replace("%player%", PlayerManager.getLegacyDisplayName(player)).replace("%target%", PlayerManager.getLegacyDisplayName(target))));
        }
    }

    @EventHandler
    public void entityDamage(EntityDamageEvent event) {
        if (
                !(event.getEntity() instanceof Player player) ||
                        VanishManager.isInvisible(player) ||
                        GodCommand.isOn(player) ||
                        !WarCommand.isWarMode()
        ) {
            event.setCancelled(true);
            return;
        }

        if (event.getCause() == EntityDamageEvent.DamageCause.VOID) {
            if (WarCommand.isWarMode()) {
                player.setHealth(0);
                return;
            }

            StuckCommand.teleport(player);
        }
    }

    @EventHandler
    public void breaking(BlockBreakEvent event) {
        if (WarCommand.isWarMode() || BuildCommand.isBuilding(event.getPlayer())) return;

        event.setCancelled(true);
    }

    @EventHandler
    public void placing(BlockPlaceEvent event) {
        if (WarCommand.isWarMode() || BuildCommand.isBuilding(event.getPlayer())) return;

        event.setCancelled(true);
    }
    
    @EventHandler(priority=EventPriority.LOWEST)
    public void animation(PlayerAnimationEvent event) {
        Player player = event.getPlayer();
        ItemStack itemInHand = player.getInventory().getItemInMainHand();

        if (event.getAnimationType() == PlayerAnimationType.ARM_SWING && itemInHand.isSimilar(LightningStickCommand.LIGHTNING_STICK)) {
            Location location = player.getTargetBlock(null, 70).getLocation();
            if (location.getBlock().getType() == Material.AIR) return;

            player.getWorld().strikeLightning(location);
        }
    }

    @EventHandler(priority=EventPriority.LOWEST)
    public void drop(PlayerDropItemEvent event) {
        Player player = event.getPlayer();

        if (WarCommand.isWarMode() || BuildCommand.isBuilding(player)) return;
        event.setCancelled(true);
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void click(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player player)) return;

        if (WarCommand.isWarMode() || BuildCommand.isBuilding(player)) return;
        event.setCancelled(true);
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void move(InventoryMoveItemEvent event) {
        if (!(event.getSource().getHolder() instanceof Player player)) return;

        if (WarCommand.isWarMode() || BuildCommand.isBuilding(player)) return;
        event.setCancelled(true);
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void death(PlayerDeathEvent event) {
        Player player = event.getEntity();

        if (GodCommand.isOn(player)) event.setCancelled(true);

        event.deathMessage(Component.empty());
        Bukkit.getScheduler().runTaskLater(Main.getInstance(), () -> {
            if (player.isDead()) player.spigot().respawn();
        }, 1);
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void respawn(PlayerRespawnEvent event) {
        Player player = event.getPlayer();

        PlayerManager.respawn(player);
    }

    @EventHandler(priority=EventPriority.LOWEST)
    public void teleport(PlayerTeleportEvent event) {
        Player player = event.getPlayer();
        if (event.getCause() != PlayerTeleportEvent.TeleportCause.ENDER_PEARL) return;

        event.setCancelled(true);
        player.setNoDamageTicks(1);
        player.setFallDistance(0);
        player.teleport(event.getTo());
    }

    @EventHandler(priority=EventPriority.LOWEST)
    public void day(TimeSkipEvent event) {
        if (WarCommand.isWarMode()) return;

        if (event.getSkipReason() == TimeSkipEvent.SkipReason.NIGHT_SKIP) event.setCancelled(true);
    }

    @EventHandler(priority=EventPriority.LOWEST)
    public void weather(WeatherChangeEvent event) {
        if (WarCommand.isWarMode()) return;

        if (event.toWeatherState()) event.setCancelled(true);
    }

    @EventHandler(priority=EventPriority.LOWEST)
    public void thunder(ThunderChangeEvent event) {
        if (WarCommand.isWarMode()) return;

        if (event.toThunderState()) event.setCancelled(true);
    }

    @EventHandler
    public void onFoodLevelChangeEvent(FoodLevelChangeEvent event) {
        if (WarCommand.isWarMode()) return;

        event.setFoodLevel(20);
    }

    @EventHandler
    public void entityExplode(EntityExplodeEvent event) {
        if (WarCommand.isWarMode()) return;

        event.blockList().clear();
    }

    @EventHandler
    public void creatureSpawn(CreatureSpawnEvent event) {
        if (WarCommand.isWarMode()) return;

        if (event.getSpawnReason().equals(CreatureSpawnEvent.SpawnReason.NATURAL)) event.setCancelled(true);
    }

    @EventHandler
    public void targetEntity(EntityTargetEvent event) {
        if (
            !(event.getTarget() instanceof Player player) ||
            VanishManager.isInvisible(player) ||
            GodCommand.isOn(player) ||
            !WarCommand.isWarMode()
        )
            event.setCancelled(true);
    }

    @EventHandler(priority = EventPriority.LOW)
    public void portal(EntityPortalEnterEvent event) {
        if (WarCommand.isWarMode()) return;

        if (event.getEntity() instanceof Player player) {
            LobbyManager.Lobby lobby = LobbyManager.getRandomNonFullLobby();
            if (lobby == null) return;
            LobbyManager.connect(player, lobby.getServerName());
        }
    }

    @EventHandler
    public void onPlayerLevelChangeEvent(PlayerLevelChangeEvent event) {
        if (WarCommand.isWarMode()) return;

        Player player = event.getPlayer();
        if (event.getNewLevel() < event.getOldLevel()) return;

        player.sendMessage(TextUtility.color("▬".repeat(64)));
        player.sendMessage(TextUtility.color(" ".repeat(21) + "&a&ka &r&6LEVEL UP! &a&ka"));
        player.sendMessage(TextUtility.color(""));
        player.sendMessage(TextUtility.color(" ".repeat(11) + "&7You are now &3Network Level &a" + event.getNewLevel() + "&7!"));
        player.sendMessage(TextUtility.color(""));
        if (event.getNewLevel() <= 250) player.sendMessage(MenuManager.clickable(" ".repeat(10) + "&eClick here to claim your reward!", player.getUniqueId(), "&eClick to open rewards!", p -> MenuManager.openMenu(Rewards.class, player, null)));
        player.sendMessage(TextUtility.color("▬".repeat(64)));

        player.playSound(player.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1f, 1f);
        player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, 1f, 1f);

        int flashes = 4, intervalTicks = 15;
        for (int i = 0; i < flashes; i++) {
            boolean white = i % 2 == 0; // alternate color each iteration

            Title title = Title.title(
                    TextUtility.color(white ? "&f&lLEVEL UP" : "&6&lLEVEL UP"),
                    TextUtility.color(""),
                    5, 10, 5
            );

            int delay = i * intervalTicks;

            Bukkit.getScheduler().runTaskLater(Main.getInstance(), () -> player.showTitle(title), delay);
        }
    }
}
