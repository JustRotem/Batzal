package net.justrotem.lobby.commands;

import io.papermc.paper.command.brigadier.BasicCommand;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import net.justrotem.lobby.Main;
import net.justrotem.lobby.utils.Utility;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.configuration.Configuration;
import org.bukkit.entity.Player;

public class StuckCommand implements BasicCommand {
    @Override
    public void execute(CommandSourceStack source, String[] args) {
        if (Utility.isConsole(source)) return;
        Player player = (Player) source.getSender();

        teleport(player);
    }

    public static Location getSpawn(Player player) {
        try {
            Configuration config = Main.getInstance().getConfig();

            World world = Bukkit.getWorld(config.getString("Spawn.world"));
            if (world == null) throw new Exception("Spawn->world is not configured correctly, in config.yml");

            Location location = new Location(world, config.getDouble("Spawn.x"), config.getDouble("Spawn.y"), config.getDouble("Spawn.z"), config.getInt("Spawn.yaw"), config.getInt("Spawn.pitch"));

            location.setY(location.getY() - 4);
            if (location.getBlock().getType() == Material.AIR) location.setY(world.getHighestBlockAt(location).getY() + 1);

            location.setY(location.getY() + 4);
            if (!(player.hasPermission("batzal.fly") && location.getBlock().getType() == Material.AIR)) location.setY(world.getHighestBlockAt(location).getY() + 1);

            return location;
        } catch (Exception ignored) {
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
