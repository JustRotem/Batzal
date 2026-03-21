package net.justrotem.lobby.commands;

import io.papermc.paper.command.brigadier.BasicCommand;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import net.justrotem.lobby.Main;
import net.justrotem.lobby.utils.PlayerUtility;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.configuration.Configuration;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;

public class StuckCommand implements BasicCommand {
    @Override
    public void execute(CommandSourceStack source, String[] args) {
        if (PlayerUtility.isConsole(source)) return;
        Player player = (Player) source.getSender();

        PlayerUtility.runTarget(player, args, 1, "batzal.stuck.others", StuckCommand::teleport);
    }

    @Override
    public @NotNull Collection<String> suggest(@NotNull CommandSourceStack source, String[] args) {
        List<String> arguments = new ArrayList<>();

        PlayerUtility.addPlayerCompletion(args, 1, arguments, source, "batzal.stuck.others");

        return arguments;
    }

    public static Location getSpawn(Player player) {
        try {
            Configuration config = Main.getInstance().getConfig();

            World world = Bukkit.getWorld(Objects.requireNonNull(config.getString("Spawn.world")));

            Location location = new Location(world, config.getDouble("Spawn.x"), config.getDouble("Spawn.y"), config.getDouble("Spawn.z"), config.getInt("Spawn.yaw"), config.getInt("Spawn.pitch"));

            location.setY(TopCommand.getHighestBlockAt(location).getBlockY());

            if (FlyCommand.canFly(player)) location.add(0, 2, 0);

            return location;
        } catch (NullPointerException ignored) {
        }

        return null;
    }

    public static void teleport(Player player) {
        Location location = getSpawn(player);
        if (location == null) {
            player.teleport(player.getWorld().getSpawnLocation());
            return;
        }

        player.teleport(location);
    }
}
