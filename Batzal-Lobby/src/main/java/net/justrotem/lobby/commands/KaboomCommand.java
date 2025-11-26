package net.justrotem.lobby.commands;

import io.papermc.paper.command.brigadier.BasicCommand;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import net.justrotem.data.PlayerManager;
import net.justrotem.lobby.utils.TextUtils;
import net.justrotem.lobby.utils.Utility;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class KaboomCommand implements BasicCommand {

    @Override
    public void execute(CommandSourceStack source, String[] args) {
        if (Utility.isConsole(source)) return;
        Player player = (Player) source.getSender();

        List<Player> players = new ArrayList<>();
        if (args.length == 0 || args[0].equalsIgnoreCase("*") || args[0].equalsIgnoreCase("all"))  players.addAll(Bukkit.getOnlinePlayers());
        else {
            Player target = Utility.getTargetNonNull(player, args[0]);
            if (target == null) return;

            players.add(target);
        }

        for (Player target : players) {
            if (target.isFlying()) target.setFlying(false);

            Utility.strikeLightningWithoutFire(target.getLocation());
            target.setVelocity(new Vector(0.0D, 5.0, 0.0D));
            target.setFallDistance(0);

            if (target != player) player.sendMessage(TextUtils.color("&aLaunched %target%&a!".replace("%target%", PlayerManager.getLegacyDisplayName(target))));
        }
    }

    @Override
    public Collection<String> suggest(CommandSourceStack source, String[] args) {
        List<String> arguments = new ArrayList<>();

        Utility.addCompletion(args, 1, arguments, "*", "all");
        Utility.addPlayerCompletion(args, 1, arguments, source, "batzal.kaboom");

        return arguments;
    }

    @Override
    public @Nullable String permission() {
        return "batzal.kaboom";
    }
}
