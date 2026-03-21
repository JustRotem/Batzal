package net.justrotem.lobby.commands;

import io.papermc.paper.command.brigadier.BasicCommand;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import net.justrotem.data.util.CooldownManager;
import net.justrotem.data.util.TextUtility;
import net.justrotem.lobby.Main;
import net.justrotem.lobby.hooks.PlayerManager;
import net.justrotem.lobby.utils.PlayerUtility;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.title.Title;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;
import org.jspecify.annotations.Nullable;

import java.time.Duration;
import java.time.temporal.ChronoUnit;

public class WarCommand implements BasicCommand {

    private static boolean warMode = false;

    public static boolean isWarMode() {
        return warMode;
    }

    @Override
    public void execute(CommandSourceStack source, String[] args) {
        if (PlayerUtility.isConsole(source)) return;
        Player player = (Player) source.getSender();

        if (isWarMode()) {
            warMode = false;
            Bukkit.getOnlinePlayers().forEach(p -> p.showTitle(Title.title(TextUtility.color("&c&lWAR MODE"), TextUtility.color("&6DEACTIVATED BY " + PlayerManager.getLegacyRealDisplayName(p)), Title.Times.times(Duration.of(1, ChronoUnit.SECONDS), Duration.of(4, ChronoUnit.SECONDS), Duration.of(1, ChronoUnit.SECONDS)))));

            CooldownManager.startCooldown(player.getUniqueId(), Main.CooldownCategory.WarMode, Duration.of(20, ChronoUnit.SECONDS));

            new BukkitRunnable() {
                @Override
                public void run() {
                    long second = CooldownManager.getRemaining(player.getUniqueId(), Main.CooldownCategory.WarMode);
                    for (Player p : Bukkit.getOnlinePlayers()) {
                        if (second == 0 || CooldownManager.isReady(player.getUniqueId(), Main.CooldownCategory.WarMode)) {
                            p.showTitle(Title.title(TextUtility.color("&eServer is restarting!"), Component.empty(), Title.Times.times(Duration.of(1, ChronoUnit.SECONDS), Duration.of(4, ChronoUnit.SECONDS), Duration.of(1, ChronoUnit.SECONDS))));
                            cancel();
                            Bukkit.shutdown();
                        }

                        if (second == 10 || second <= 5) {
                            p.showTitle(Title.title(TextUtility.color("&c" + second), TextUtility.color("&eto Server restart"), Title.Times.times(Duration.of(1, ChronoUnit.SECONDS), Duration.of(1, ChronoUnit.SECONDS), Duration.of(1, ChronoUnit.SECONDS))));
                            p.sendMessage(TextUtility.color("&eServer restart in &c%second% &eseconds!".replace("%second%", String.valueOf(second))));
                            return;
                        }
                    }
                }
            }.runTaskTimer(Main.getInstance(), 200, 20);
            return;
        }

        warMode = true;
        for (Player p : Bukkit.getOnlinePlayers()) {
            p.showTitle(Title.title(TextUtility.color("&c&lWAR MODE"), TextUtility.color("&6ACTIVATED BY " + PlayerManager.getLegacyRealDisplayName(p)), Title.Times.times(Duration.of(1, ChronoUnit.SECONDS), Duration.of(4, ChronoUnit.SECONDS), Duration.of(1, ChronoUnit.SECONDS))));

            giveWarModeItems(p);
        }
    }

    @Override
    public @Nullable String permission() {
        return "batzal.warmode";
    }

    public static void giveWarModeItems(Player player) {
        player.setGameMode(GameMode.SURVIVAL);
        player.setAllowFlight(false);
        player.getInventory().clear();

        ItemStack bow = new ItemStack(Material.BOW);
        bow.addEnchantment(Enchantment.POWER, 1);

        player.getInventory().addItem(new ItemStack(Material.WOODEN_SWORD), bow, new ItemStack(Material.IRON_PICKAXE), new ItemStack(Material.COBBLESTONE, 16), new ItemStack(Material.BREAD, 4), new ItemStack(Material.ARROW, 32));
        player.getInventory().setHelmet(new ItemStack(Material.AIR));
        player.getInventory().setChestplate(new ItemStack(Material.CHAINMAIL_CHESTPLATE));
        player.getInventory().setLeggings(new ItemStack(Material.IRON_LEGGINGS));
        player.getInventory().setBoots(new ItemStack(Material.LEATHER_BOOTS));
    }


}
