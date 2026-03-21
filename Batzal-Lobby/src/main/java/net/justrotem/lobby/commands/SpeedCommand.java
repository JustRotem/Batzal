package net.justrotem.lobby.commands;

import io.papermc.paper.command.brigadier.BasicCommand;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import net.justrotem.data.utils.TextUtility;
import net.justrotem.lobby.utils.PlayerUtility;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class SpeedCommand implements BasicCommand {

    @Override
    public void execute(CommandSourceStack source, String[] args) {
        if (PlayerUtility.isConsole(source)) return;
        Player player = (Player) source.getSender();

        if (args.length >= 1) {
            float speed;
            boolean flyOrWalk = args[0].equalsIgnoreCase("walk") || args[0].equalsIgnoreCase("fly");
            String mode = flyOrWalk ? args[0].toLowerCase() : player.isFlying() ? "fly" : "walk";
            int showedSpeed = Integer.parseInt(args.length >= 2 ? args[1] : args[0]);

            try {
                speed = getMoveSpeed(Float.parseFloat(String.valueOf(showedSpeed)), mode);
                if (showedSpeed < 1 || showedSpeed > 10) throw new NumberFormatException();
            } catch (NumberFormatException e) {
                player.sendMessage(TextUtility.color("&cThis is an invalid number! (1-10)"));
                return;
            }

            float finalSpeed = speed;
            PlayerUtility.runTarget(player, args, (flyOrWalk && args.length >= 3) ? 3 : (!flyOrWalk && args.length >= 2) ? 2 : 0, permission() + ".others", target -> {
                if (mode.equals("fly")) target.setFlySpeed(finalSpeed);
                if (mode.equals("walk")) target.setWalkSpeed(finalSpeed);

                return List.of(mode, showedSpeed);
            }, "&aSet %value-1%ing speed to &e%value-2%&a!%staff%", "&aSet %target%&a's %value-1%ing speed to &e%value-2%&a!");
            return;
        }

        player.sendMessage(TextUtility.color("&cUsage: /speed <walk/fly> <speed> <player>"));
    }

    @Override
    public @NotNull Collection<String> suggest(@NotNull CommandSourceStack source, String[] args) {
        List<String> arguments = new ArrayList<>();

        PlayerUtility.addCompletion(args, 1, arguments, "walk", "fly");
        if (args.length >= 1) PlayerUtility.addPlayerCompletion(args, args[0].equalsIgnoreCase("walk") || args[0].equalsIgnoreCase("fly") ? 3 : 2, arguments, source, permission() + ".others");

        return arguments;
    }

    @Override
    public @Nullable String permission() {
        return "batzal.speed";
    }

    private float getMoveSpeed(float speed, String mode) {
        final float defaultSpeed = mode.equals("fly") ? 0.1f : 0.2f;
        float maxSpeed = 1f;

        if (speed < 1f) {
            return defaultSpeed * speed;
        } else {
            final float ratio = ((speed - 1) / 9) * (maxSpeed - defaultSpeed);
            return ratio + defaultSpeed;
        }
    }
}