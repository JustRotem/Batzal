package net.justrotem.lobby.commands;

import io.papermc.paper.command.brigadier.BasicCommand;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import net.justrotem.data.utils.TextUtility;
import net.justrotem.lobby.hooks.PlayerManager;
import net.justrotem.lobby.utils.PlayerUtility;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class TeleportCommand implements BasicCommand {

    @Override
    public void execute(CommandSourceStack source, String[] args) {
        if (PlayerUtility.isConsole(source)) return;
        Player player = (Player) source.getSender();

        if (args.length == 0) {
            player.sendMessage(TextUtility.color("&cUsage: /tp <to-player/location> <player>"));
            return;
        }

        Location location = null;
        Player toPlayer = null;
        try {
            location = new Location(player.getWorld(),
                    args[0].equals("~") ? player.getX() : Double.parseDouble(args[0]),
                    args[1].equals("~") ? player.getY() : Double.parseDouble(args[1]),
                    args[2].equals("~") ? player.getZ() : Double.parseDouble(args[2])
            );
        } catch (NumberFormatException e) {
            toPlayer = PlayerUtility.getTarget(player, args[0]);
            if (toPlayer == null) return;
        }

        Location finalLocation = location;
        Player finalToPlayer = toPlayer;
        PlayerUtility.runTarget(player, args, location == null ? 2 : 4, permission() + ".other", target -> {
            if (finalLocation != null) {
                target.teleport(finalLocation);
                return "&e" + getLocation(finalLocation, false);
            }
            else {
                target.teleport(finalToPlayer);
                return PlayerManager.getLegacyRealDisplayName(finalToPlayer);
            }
        }, "&aYou have teleported to %value%&a!%staff%", "%target% &ahas been teleported to %value%&a!");
    }

    @Override
    public @NotNull Collection<String> suggest(@NotNull CommandSourceStack source, String[] args) {
        List<String> arguments = new ArrayList<>();

        PlayerUtility.addPlayerCompletion(args, 1, arguments, source, permission());

        if (args.length > 1) {
            if (PlayerUtility.getTarget(args[0]) != null) PlayerUtility.addPlayerCompletion(args, 2, arguments, source, permission() + ".others");
            else PlayerUtility.addPlayerCompletion(args, 4, arguments, source, permission() + ".others");
        }

        return arguments;
    }

    @Override
    public @Nullable String permission() {
        return "batzal.teleport";
    }

    public static String getLocation(Location location, boolean world) {
        return location.getX() + ", " + location.getY() + ", " + location.getZ() + (world ? " (%world%)".replace("%world%", location.getWorld().getName()) : "");
    }
}