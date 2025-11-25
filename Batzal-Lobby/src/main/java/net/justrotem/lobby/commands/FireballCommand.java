package net.justrotem.lobby.commands;

import com.google.common.collect.ImmutableMap;
import io.papermc.paper.command.brigadier.BasicCommand;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import net.justrotem.lobby.utils.Utility;
import org.bukkit.entity.*;
import org.bukkit.util.Vector;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

public class FireballCommand implements BasicCommand {

    @Override
    public void execute(CommandSourceStack source, String[] args) {
        if (Utility.isConsole(source)) return;
        Player player = (Player) source.getSender();

        final String type = args.length > 0 && types.containsKey(args[0]) ? args[0] : "fireball";
        double speed = 2;

        if (args.length > 1) {
            try {
                speed = Double.parseDouble(args[1]);
                speed = Double.max(0, Double.min(speed, 8));
            } catch (final Exception ignored) {
            }
        }

        final Vector direction = player.getEyeLocation().getDirection().multiply(speed);
        final Projectile projectile = player.getWorld().spawn(player.getEyeLocation().add(direction.getX(), direction.getY(), direction.getZ()), types.get(type));
        projectile.setShooter(player);
        projectile.setVelocity(direction);
    }

    @Override
    public Collection<String> suggest(CommandSourceStack source, String[] args) {
        List<String> arguments = new ArrayList<>();

        Utility.addCompletion(args, 1, arguments, types.keySet().toArray());

        return arguments;
    }

    @Override
    public @Nullable String permission() {
        return "batzal.fireball";
    }

    private static final Map<String, Class<? extends Projectile>> types;
    static {
        final ImmutableMap.Builder<String, Class<? extends Projectile>> builder = ImmutableMap.<String, Class<? extends Projectile>>builder()
                .put("fireball", Fireball.class)
                .put("small", SmallFireball.class)
                .put("large", LargeFireball.class)
                .put("arrow", Arrow.class)
                .put("skull", WitherSkull.class)
                .put("egg", Egg.class)
                .put("snowball", Snowball.class)
                .put("expbottle", ThrownExpBottle.class);

        types = builder.build();
    }
}