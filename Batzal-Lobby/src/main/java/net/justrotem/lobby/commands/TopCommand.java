package net.justrotem.lobby.commands;

import io.papermc.paper.command.brigadier.BasicCommand;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import net.justrotem.lobby.utils.PlayerUtility;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class TopCommand implements BasicCommand {

    @Override
    public void execute(CommandSourceStack source, String[] args) {
        if (PlayerUtility.isConsole(source)) return;
        Player player = (Player) source.getSender();

        PlayerUtility.runTarget(player, args, 1, permission() + ".other", target -> {
            Location location = player.getLocation();

            player.teleport(getHighestBlockAt(location));
        }, "&aYou have teleported to the highest block at your location!%staff%", "%target% &ahas been teleported to the highest block at your location!");
    }

    @Override
    public @NotNull Collection<String> suggest(@NotNull CommandSourceStack source, String[] args) {
        List<String> arguments = new ArrayList<>();

        PlayerUtility.addPlayerCompletion(args, 1, arguments, source, permission() + ".others");

        return arguments;
    }

    @Override
    public @Nullable String permission() {
        return "batzal.top";
    }

    public static Location getHighestBlockAt(Location loc) {
        World world = loc.getWorld();
        int x = loc.getBlockX();
        int z = loc.getBlockZ();
        float yaw = loc.getYaw();
        float pitch = loc.getPitch();

        int maxY = world.getMaxHeight();
        int minY = world.getMinHeight();

        // Step 1: scan from the TOP of the world downward until we hit a solid block
        for (int y = maxY; y >= minY; y--) {
            Block block = world.getBlockAt(x, y, z);

            if (block.getType().isSolid()) {
                // Step 2: return safe standing spot above solid block
                return new Location(world, x + 0.5, y + 1, z + 0.5, yaw, pitch);
            }
        }

        // Step 3: if the column is empty (void world), find closest safe Y
        // e.g., teleport to minY+5
        return new Location(world, x, minY + 5, z, yaw, pitch);
    }

}
