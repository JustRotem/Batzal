package net.justrotem.lobby.ride;

import net.justrotem.lobby.Main;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

public class RideManager {

    public static void spawnRidable(Player player, EntityType type, Location loc) {
        World world = loc.getWorld();
        if (world == null) return;

        Entity entity;

        // Special handling for EnderDragon
        entity = world.spawnEntity(loc, type);

        // Make entity invulnerable
        entity.setInvulnerable(true);

        // Mount the player
        entity.addPassenger(player);

        // Attach movement if entity is living
        if (entity instanceof LivingEntity living) attach(living, player);
    }

    public static void attach(LivingEntity entity, Player rider) {
        new BukkitRunnable() {
            @Override
            public void run() {
                // Stop movement if entity dead or rider offline or dismounted
                if (entity.isDead() || !rider.isOnline() || !entity.getPassengers().contains(rider)) {
                    entity.remove();
                    cancel();
                    return;
                }

                // Get player direction including vertical for flying
                Vector direction = rider.getLocation().getDirection();

                // Adjust speed (slightly slower than default)
                direction.multiply(0.3);

                // Apply velocity
                entity.setVelocity(direction);

                // Rotate entity to match player yaw/pitch
                entity.setRotation(rider.getLocation().getYaw(), rider.getLocation().getPitch());
            }
        }.runTaskTimer(Main.getInstance(), 0L, 1L);
    }
}
