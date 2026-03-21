package net.justrotem.lobby.commands;

import io.papermc.paper.command.brigadier.BasicCommand;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import net.justrotem.data.utils.ToggleManager;
import net.justrotem.lobby.Main;
import net.justrotem.lobby.hooks.LuckPermsManager;
import net.justrotem.lobby.utils.PlayerUtility;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class LeaveMeAloneCommand implements BasicCommand {

    public LeaveMeAloneCommand() {
        new BukkitRunnable() {
            @Override
            public void run() {
                ToggleManager.getAll(Main.ToggleCategory.LeaveMeAlone, true).forEach(uuid -> {
                    Player player = Bukkit.getPlayer(uuid);
                    if (player == null) return;

                    for (Player target : Bukkit.getOnlinePlayers()) {
                        if (uuid == target.getUniqueId() || !LuckPermsManager.hasPermission(player, "batzal.leavemealone") || LuckPermsManager.hasPermission(target, "batzal.leavemealone.bypass")) continue;

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
        if (PlayerUtility.isConsole(source)) return;
        Player player = (Player) source.getSender();

        PlayerUtility.runTarget(player, args, 1, permission() + ".others", target -> {
            ToggleManager.toggle(Main.ToggleCategory.LeaveMeAlone, target.getUniqueId());
            return ToggleManager.isOn(Main.ToggleCategory.LeaveMeAlone, player.getUniqueId()) ? "on" : "off";
        }, "&aTurn %value% Leave Me Alone%staff%!", "&aTurn %value% Leave Me Alone for %target%&a!");
    }

    @Override
    public @NotNull Collection<String> suggest(@NotNull CommandSourceStack source, String[] args) {
        List<String> arguments = new ArrayList<>();

        PlayerUtility.addPlayerCompletion(args, 1, arguments, source, permission() + ".others");

        return arguments;
    }

    @Override
    public @Nullable String permission() {
        return "batzal.leavemealone";
    }
}
