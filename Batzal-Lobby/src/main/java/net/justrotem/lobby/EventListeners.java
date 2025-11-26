package net.justrotem.lobby;

import io.papermc.paper.event.player.AsyncChatEvent;
import net.justrotem.data.PlayerManager;
import net.justrotem.data.utils.CooldownManager;
import net.justrotem.lobby.commands.*;
import net.justrotem.lobby.hooks.LuckPermsManager;
import net.justrotem.lobby.nick.NickManager;
import net.justrotem.lobby.utils.TextUtils;
import net.justrotem.lobby.utils.Utility;
import net.justrotem.lobby.vanish.VanishManager;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.attribute.Attribute;
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
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.Vector;

import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

public class EventListeners implements Listener {

    @EventHandler
    public void join(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        PlayerManager.registerPlayer(player);
        NickManager.registerPlayer(player);

        VanishManager.updateVanishedPlayers();

        if (NickManager.isNicked(player)) {
            String nickname = NickManager.getNickName(player);
            if (nickname != null) {
                if (NickManager.isNameRestricted(player, nickname, true)) {
                    NickManager.resetNick(player);
                    player.sendMessage(TextUtils.color("&cThere was a problem with your last nickname. &eChange your nickname using /nick"));
                } else {
                    NickManager.reuseNick(player);
                    player.sendMessage(TextUtils.color("&aYou are now nicked as %name%!".replace("%name%", nickname)));

                    if (!NickManager.isLobbyNicked(player)) NickManager.resetRankInGame(player);
                }
            }
        }

        Component joinMessage = null;

        if (!NickManager.isLobbyNicked(player)) {
            ConfigurationSection section = Main.getInstance().getConfig().getConfigurationSection("JoinMessages");
            if (section != null) {
                for (String rank : section.getKeys(false)) {
                    if (LuckPermsManager.getPrimaryGroup(player.getUniqueId()).equalsIgnoreCase(rank)) {
                        String message = section.getString(rank);
                        if (message == null) continue;

                        joinMessage = TextUtils.color(message.replace("%displayname%", PlayerManager.getLegacyDisplayName(player)));
                        break;
                    }
                }
            }
        }

        if (LuckPermsManager.hasPermission(player, "batzal.fly")) {
            player.setAllowFlight(true);
            player.setFlying(true);
        }

        player.setHealth(player.getAttribute(Attribute.MAX_HEALTH).getBaseValue());
        player.setFoodLevel(20);

        StuckCommand.teleport(player);

        event.joinMessage(!VanishManager.isInvisible(player) ? joinMessage : null);
    }

    @EventHandler
    public void quit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        PlayerManager.savePlayer(player);
        NickManager.savePlayer(player);

        event.quitMessage(null);
    }

    @EventHandler
    public void chat(AsyncChatEvent event) {
        if (event.isCancelled()) return;

        Player player = event.getPlayer();
        event.setCancelled(true);

        // afk update

        // Toggle chat
        if (PlayerManager.isChatToggled(player)) {
            player.sendMessage(TextUtils.color("&cYou have chat disabled! Enable it again with /togglechat"));
            return;
        }

        // Same message
        if (PlayerManager.isSameMessage(player, event.message())) {
            player.sendMessage(TextUtils.color("&6&m---------------------------------------------\n&r&cYou cannot say the same message twice!\n&6&m---------------------------------------------"));
            return;
        }

        // Advertising - should add a check for how many times and maybe adding mute/ban feature.
        if (PlayerManager.isAdvertising(player, event.message())) {
            player.sendMessage(TextUtils.color("&6&m---------------------------------------------\n&r&cAdvertising is against the rules. You will be permanently\n&c banned from the server if you attempt to advertise.\n&6&m---------------------------------------------"));
            return;
        }

        // Save the message as the @player last message
        PlayerManager.setLastMessage(player, event.originalMessage());

        // Prefix
        Component message;
        if (LuckPermsManager.isHooked()) {
            String rank;
            if (NickManager.isLobbyNicked(player)) rank = NickManager.getRank(player);
            else rank = LuckPermsManager.getPrimaryGroup(player.getUniqueId());

            message = TextUtils.color("&" + (rank.equalsIgnoreCase("default") ? '7' : 'f'))
                    .append(PlayerManager.getDisplayName(player))
                    .append(TextUtils.color(": "));
        } else message = Component.text("<", NamedTextColor.WHITE).append(player.displayName(), Component.text("> "));

        // Checks if mentioning a Player
        String plainMention = TextUtils.escapeTags(TextUtils.serialize(event.message(), TextUtils.Format.PLAIN));

        List<Player> mentionedPlayers = new ArrayList<>();
        for (Player p : Bukkit.getOnlinePlayers()) {
            if (p.equals(player)) continue;

            String name;
            if (NickManager.isLobbyNicked(p)) name = NickManager.getNickName(p);
            else name = p.getName();

            if (plainMention.toLowerCase().contains(name.toLowerCase())) {
                plainMention = plainMention.replaceAll("(?i)" + Pattern.quote(name), "<yellow>" + name + "</yellow>");
                mentionedPlayers.add(p);
            }
        }

        // Colored (legacy & codes), for mentioned players only
        Component mentionMessage = player.hasPermission("batzal.chat.colors")
                ? TextUtils.color(plainMention)
                : TextUtils.deserialize(plainMention, TextUtils.Format.MINIMESSAGE);

        // Plain, for non-mentioned players
        Component plainMessage = player.hasPermission("batzal.chat.colors")
                ? TextUtils.color(TextUtils.serialize(event.message(), TextUtils.Format.PLAIN))
                : event.message();

        for (Audience viewer : event.viewers()) {
            if (viewer instanceof Player p && mentionedPlayers.contains(p)) {
                event.message(mentionMessage);
                p.playSound(p.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1f, 1f);
            }
            else event.message(plainMessage);

            viewer.sendMessage(message.append(event.message()));
        }
    }

    @EventHandler(priority=EventPriority.LOWEST)
    public void punch(EntityDamageByEntityEvent event) {
        if (WarCommand.isWarMode()) return;

        event.setCancelled(true);
        if (!(event.getDamager() instanceof Player player && event.getEntity() instanceof Player target)) return;

        if (VanishManager.isInvisible(player) || VanishManager.isInvisible(target)) return;

        if (PlayerManager.getData(target).isTogglePunch()) return;

        if (!LuckPermsManager.hasPermission(player, "batzal.punch.puncher")) return;
        if (!LuckPermsManager.hasPermission(target, "batzal.punch.punched")) return;

        if (!CooldownManager.isReady(player, Main.CooldownCategory.Punch)) {
            player.sendMessage(TextUtils.color("&cYou have to wait %cooldown% more seconds!".replace("%cooldown%", String.valueOf(CooldownManager.getRemaining(player, Main.CooldownCategory.Punch)))));
            return;
        }

        CooldownManager.startCooldown(player, Main.CooldownCategory.Punch, Duration.of(1, ChronoUnit.MINUTES));

        if (target.isFlying()) target.setFlying(false);
        target.getLocation().getWorld().createExplosion(target.getLocation(), 5F, false, false);
        target.setVelocity(new Vector(0.0D, 10.0, 0.0D));

        for (Player p : Bukkit.getOnlinePlayers()) {
            //p.sendMessage(TextUtils.color(PunchMessages.getPunchMessages(player).replace("%displayname%", PlayerManager.getLegacyDisplayName(target))));
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

    @EventHandler
    public void move(InventoryMoveItemEvent event) {
        if (!(event.getSource().getHolder() instanceof Player player)) return;

        if (WarCommand.isWarMode() || BuildCommand.isBuilding(player)) return;

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

    @EventHandler
    public void moving(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player player)) return;

        if (WarCommand.isWarMode() || BuildCommand.isBuilding(player)) return;
        event.setCancelled(true);
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void death(PlayerDeathEvent event) {
        Player player = event.getEntity();

        if (GodCommand.isOn(player)) event.setCancelled(true);

        event.deathMessage(Component.text(""));
        Bukkit.getScheduler().runTaskLater(Main.getInstance(), () -> {
            if (player.isDead()) player.spigot().respawn();
        }, 1);
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void respawn(PlayerRespawnEvent event) {
        Player player = event.getPlayer();
        Location location = StuckCommand.getSpawn(player);
        if (location == null) return;

        event.setRespawnLocation(location);

        Bukkit.getScheduler().runTask(Main.getInstance(), () -> player.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, Integer.MAX_VALUE, 0, true, false)));
        if (WarCommand.isWarMode()) WarCommand.giveWarModeItems(player);
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
        if (event.getEntity() instanceof Player player) player.kick(Component.text("LOBBY_TRANSFER"));
    }
}
