package net.justrotem.lobby.commands;

import io.papermc.paper.command.brigadier.BasicCommand;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import net.justrotem.data.utils.CooldownManager;
import net.justrotem.lobby.Main;
import net.justrotem.lobby.utils.TextUtils;
import net.justrotem.lobby.utils.Utility;
import org.bukkit.Color;
import org.bukkit.FireworkEffect;
import org.bukkit.Location;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Firework;
import org.bukkit.entity.Player;
import org.bukkit.inventory.meta.FireworkMeta;

import java.time.Duration;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;

public class FireworkCommand implements BasicCommand {
    @Override
    public void execute(CommandSourceStack source, String[] args) {
        if (Utility.isConsole(source)) return;
        Player player = (Player) source.getSender();

        if (!CooldownManager.isReady(player, Main.CooldownCategory.FireWork)) {
            long left = CooldownManager.getRemaining(player, Main.CooldownCategory.FireWork);
            player.sendMessage(TextUtils.color("&cYou have to wait %left% seconds between sending fireworks!".replace("%left%", String.valueOf(left))));
            return;
        }

        CooldownManager.startCooldown(player, Main.CooldownCategory.FireWork, Duration.ofSeconds(15));
        spawnRandomFirework(player.getLocation());
        player.sendMessage(TextUtils.color("&eLaunched a firework!"));
    }

    private static void spawnRandomFirework(Location loc) {
        Random random = ThreadLocalRandom.current();

        // Pre-defined Bukkit colors
        List<Color> colors = List.of(
                Color.AQUA, Color.BLACK, Color.BLUE, Color.FUCHSIA, Color.GRAY,
                Color.GREEN, Color.LIME, Color.MAROON, Color.NAVY, Color.OLIVE,
                Color.ORANGE, Color.PURPLE, Color.RED, Color.SILVER, Color.TEAL,
                Color.WHITE, Color.YELLOW
        );

        // Spawn firework
        Firework firework = (Firework) loc.getWorld().spawnEntity(loc, EntityType.FIREWORK_ROCKET);
        FireworkMeta meta = firework.getFireworkMeta();

        // Build effect
        FireworkEffect effect = FireworkEffect.builder()
                .flicker(random.nextBoolean())
                .trail(random.nextBoolean())
                .with(FireworkEffect.Type.values()[random.nextInt(FireworkEffect.Type.values().length)])
                .withColor(colors.get(random.nextInt(colors.size())))
                .withFade(colors.get(random.nextInt(colors.size())))
                .build();

        meta.addEffect(effect);
        meta.setPower(random.nextInt(2) + 1); // 1 or 2 power
        firework.setFireworkMeta(meta);
    }

}
