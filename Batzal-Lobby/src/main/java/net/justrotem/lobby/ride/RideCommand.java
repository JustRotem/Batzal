package net.justrotem.lobby.ride;

import io.papermc.paper.command.brigadier.BasicCommand;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import net.justrotem.data.PlayerManager;
import net.justrotem.lobby.ride.nms.PetEnderDragon;
import net.justrotem.lobby.utils.TextUtils;
import net.justrotem.lobby.utils.Utility;
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
        if (Utility.isConsole(source)) return;
        Player player = (Player) source.getSender();

        if (args.length == 1) {
            try {
                EntityType entityType = EntityType.valueOf(args[0]);
                Location location = player.getLocation();

                if (entityType == EntityType.ENDER_DRAGON) {
                    PetEnderDragon dragon = DragonFactory.create(player.getWorld(), player.getUniqueId());
                    dragon.spawn(player.getLocation().toVector());
                    DragonFactory.tryRide(player, dragon.getEntity());
                } else RideManager.spawnRidable(player, entityType, location);
                return;
            } catch (IllegalArgumentException ignored) {
            }

            Player target = Utility.getTargetNonNull(player, args[0]);
            if (target == null) return;

            target.addPassenger(player);
            target.sendMessage(TextUtils.color("%displayname% &ais riding on top of you!".replace("%displayname%", PlayerManager.getLegacyRealDisplayName(player))));
            player.sendMessage(TextUtils.color("&aYou are riding %displayname%".replace("%displayname%", PlayerManager.getLegacyRealDisplayName(target))));
            return;
        }

        player.sendMessage(TextUtils.color("&cUsage: /ride <player/entity>"));
    }

    @Override
    public @NotNull Collection<String> suggest(@NotNull CommandSourceStack source, String[] args) {
        List<String> arguments = new ArrayList<>();

        Utility.addPlayerCompletion(args, 1, arguments, source, "batzal.vanish.others");
        Utility.addCompletion(args, 1, arguments, EntityType.values());

        return arguments;
    }

    @Override
    public @Nullable String permission() {
        return "batzal.ride";
    }
}
