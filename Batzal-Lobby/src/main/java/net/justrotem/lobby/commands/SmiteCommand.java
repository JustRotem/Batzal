package net.justrotem.lobby.commands;

import io.papermc.paper.command.brigadier.BasicCommand;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import net.justrotem.lobby.utils.Utility;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class SmiteCommand implements BasicCommand {

    @Override
    public void execute(CommandSourceStack source, String[] args) {
        if (Utility.isConsole(source)) return;
        Player player = (Player) source.getSender();

        if (args.length == 1) {
            Collection<Player> list = new ArrayList<>();
            if (args[0].equalsIgnoreCase("*") || args[0].equalsIgnoreCase("all")) list.addAll(Bukkit.getOnlinePlayers());
            else {
                Player target = Utility.getTargetNonNull(player, args[0]);
                if (target == null) return;

                list.add(target);
            }

            for (Player p : list) p.getWorld().strikeLightning(p.getLocation());
            return;
        }

        Location location = player.getTargetBlockExact(50).getLocation();
        player.getWorld().strikeLightning(location);
    }

    @Override
    public @NotNull Collection<String> suggest(@NotNull CommandSourceStack source, String[] args) {
        List<String> arguments = new ArrayList<>();

        Utility.addPlayerCompletion(args, 1, arguments, source, "batzal.ping.others");

        return arguments;
    }

    @Override
    public @Nullable String permission() {
        return "batzal.smite";
    }
}