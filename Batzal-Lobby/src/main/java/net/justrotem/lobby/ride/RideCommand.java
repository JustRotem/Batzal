package net.justrotem.lobby.ride;

import io.papermc.paper.command.brigadier.BasicCommand;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import net.justrotem.data.utils.TextUtility;
import net.justrotem.lobby.hooks.PlayerManager;
import net.justrotem.lobby.ride.nms.PetEnderDragon;
import net.justrotem.lobby.utils.PlayerUtility;
import org.bukkit.Location;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class RideCommand implements BasicCommand {

    @Override
    public void execute(CommandSourceStack source, String[] args) {
        if (PlayerUtility.isConsole(source)) return;
        Player player = (Player) source.getSender();

        if (args.length == 0) {
            player.sendMessage(TextUtility.color("&cUsage: /ride <player/entity> <player>"));
            return;
        }

        EntityType entityType = null;
        Player ridable = null;
        try {
            entityType = EntityType.valueOf(args[0]);
        } catch (IllegalArgumentException e) {
            ridable = PlayerUtility.getTarget(player, args[0]);
        }

        if (entityType == null && ridable == null) return;

        EntityType finalEntityType = entityType;
        Player finalRidable = ridable;
        PlayerUtility.runTarget(player, args, 2, permission() + ".others", target -> {
            if (finalEntityType != null) {
                Location location = target.getLocation();

                if (finalEntityType == EntityType.ENDER_DRAGON) {
                    PetEnderDragon dragon = DragonFactory.create(target.getWorld(), target.getUniqueId());
                    dragon.spawn(target.getLocation().toVector());
                    DragonFactory.tryRide(target, dragon.getEntity());
                } else RideManager.spawnRidable(target, finalEntityType, location);
                return finalEntityType.name().toUpperCase();
            } else {
                finalRidable.addPassenger(target);
                finalRidable.sendMessage(TextUtility.color("%target% &ais riding on top of you!".replace("%target%", PlayerManager.getLegacyRealDisplayName(target))));
            }
            return PlayerManager.getLegacyRealDisplayName(finalRidable);
        }, "&aYou are riding on top of %value%&a!%staff%", "%target% &ais riding on top of %value%&a!");
    }

    @Override
    public @NotNull Collection<String> suggest(@NotNull CommandSourceStack source, String[] args) {
        List<String> arguments = new ArrayList<>();

        PlayerUtility.addPlayerCompletion(args, 1, arguments, source, "batzal.ride");
        PlayerUtility.addCompletion(args, 1, arguments, EntityType.values());

        PlayerUtility.addPlayerCompletion(args, 2, arguments, source, permission() + ".others");

        return arguments;
    }

    @Override
    public @Nullable String permission() {
        return "batzal.ride";
    }
}
