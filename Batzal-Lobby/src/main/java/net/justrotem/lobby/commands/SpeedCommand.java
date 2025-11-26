package net.justrotem.lobby.commands;

import io.papermc.paper.command.brigadier.BasicCommand;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import net.justrotem.lobby.utils.TextUtils;
import net.justrotem.lobby.utils.Utility;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class SpeedCommand implements BasicCommand {

    @Override
    public void execute(CommandSourceStack source, String[] args) {
        if (Utility.isConsole(source)) return;
        Player player = (Player) source.getSender();

        String mode;
        float speed = 0;

        if (args.length >= 1) {
            if (args[0].equalsIgnoreCase("walk") || args[0].equalsIgnoreCase("fly")) {
                mode = args[0].toLowerCase();

                if (args.length >= 2) {
                    speed = getMoveSpeed(player, Float.parseFloat(args[1]), mode);
                }
            } else {
                if (player.isFlying()) mode = "fly";
                else mode = "walk";

                speed = getMoveSpeed(player, Float.parseFloat(args[0]), mode);
            }

            if (mode.equals("fly")) player.setFlySpeed(speed);
            if (mode.equals("walk")) player.setWalkSpeed(speed);

            player.sendMessage(TextUtils.color("&aSet %mode%ing speed to &e%speed%&a!"
                    .replace("%mode%", mode)
                    .replace("%speed%", args.length > 1 ? args[1] : args[0])
            ));
            return;
        }

        player.sendMessage(TextUtils.color("&cUsage: /speed <walk/fly> <speed>"));
    }

    @Override
    public @NotNull Collection<String> suggest(@NotNull CommandSourceStack source, String[] args) {
        List<String> arguments = new ArrayList<>();

        Utility.addCompletion(args, 1, arguments, "walk", "fly");

        return arguments;
    }

    @Override
    public @Nullable String permission() {
        return "batzal.speed";
    }

    private float getMoveSpeed(Player player, float speed, String mode) {
        try {
            final float defaultSpeed = mode.equals("fly") ? 0.1f : 0.2f;
            float maxSpeed = 1f;

            if (speed < 1f) {
                return defaultSpeed * speed;
            } else {
                final float ratio = ((speed - 1) / 9) * (maxSpeed - defaultSpeed);
                return ratio + defaultSpeed;
            }
        } catch (IllegalArgumentException e) {
            player.sendMessage(TextUtils.color("&cThis is not a valid number!"));
            return 0;
        }
    }
}