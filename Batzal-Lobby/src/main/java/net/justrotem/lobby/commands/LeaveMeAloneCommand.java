package net.justrotem.lobby.commands;

import io.papermc.paper.command.brigadier.BasicCommand;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import net.justrotem.data.utils.ToggleManager;
import net.justrotem.lobby.Main;
import net.justrotem.lobby.utils.TextUtils;
import net.justrotem.lobby.utils.Utility;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;
import org.jspecify.annotations.Nullable;

public class LeaveMeAloneCommand implements BasicCommand {

    public LeaveMeAloneCommand() {
        new BukkitRunnable() {
            @Override
            public void run() {
                ToggleManager.getAll(Main.ToggleCategory.LeaveMeAlone, true).forEach(player -> {
                    for (Player target : Bukkit.getOnlinePlayers()) {
                        if (player == target || !player.hasPermission("batzal.leavemealone") || target.hasPermission("batzal.leavemealone.bypass")) continue;

                        if (player.getLocation().distance(target.getLocation()) <= 5) {
                            Vector entV = target.getLocation().toVector();
                            Vector plV = player.getLocation().toVector();

                            Vector v = entV.clone().subtract(plV).multiply(0.5 / entV.distance(plV));
                            v.setY(0.5);

                            target.setVelocity(v);
                        }
                    }
                });
            }
        }.runTaskTimer(Main.getInstance(), 0L, 10L); // Runs every 20 ticks = 1 second
    }

    @Override
    public void execute(CommandSourceStack source, String[] args) {
        if (Utility.isConsole(source)) return;
        Player player = (Player) source.getSender();

        ToggleManager.toggle(Main.ToggleCategory.LeaveMeAlone, player);
        player.sendMessage(TextUtils.color("&aTurn %mode% Leave me alone!".replace("%mode%", ToggleManager.isOn(Main.ToggleCategory.LeaveMeAlone, player) ? "on" : "off")));
    }

    @Override
    public @Nullable String permission() {
        return "batzal.leavemealone";
    }
}
