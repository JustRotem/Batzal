package net.justrotem.lobby.commands;

import io.papermc.paper.command.brigadier.BasicCommand;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import net.justrotem.lobby.hooks.LuckPermsManager;
import net.justrotem.lobby.utils.PlayerUtility;
import net.justrotem.lobby.vanish.VanishManager;
import org.bukkit.entity.Player;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class FlyCommand implements BasicCommand {

    @Override
    public void execute(CommandSourceStack source, String[] args) {
        if (PlayerUtility.isConsole(source)) return;
        Player player = (Player) source.getSender();

        PlayerUtility.runTarget(player, args, 1,permission() + ".others", target -> {
            if (!canFly(target)) return new PlayerUtility.TargetException("&cYou're currently not allowed to fly!", "%target%&c's currently not allowed to fly!");

            boolean flying = target.getAllowFlight();
            target.setAllowFlight(!flying);
            target.setFlying(!flying);
            return !flying ? "on" : "off";
        }, "&aTurn %value% flight%staff%!", "&aTurn %value% flight for %target%&a!");
    }

    @Override
    public Collection<String> suggest(CommandSourceStack source, String[] args) {
        List<String> arguments = new ArrayList<>();

        PlayerUtility.addPlayerCompletion(args, 1, arguments, source, permission() + ".others");

        return arguments;
    }

    @Override
    public @Nullable String permission() {
        return "batzal.fly";
    }

    public static void flyByPermission(Player player) {
        boolean perm = canFly(player);
        player.setAllowFlight(perm);
        player.setFlying(perm);
    }

    public static boolean canFly(Player player) {
        return VanishManager.isInvisible(player) || LuckPermsManager.hasPermission(player, new FlyCommand().permission());
    }
}