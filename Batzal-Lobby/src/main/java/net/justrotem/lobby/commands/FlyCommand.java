package net.justrotem.lobby.commands;

import io.papermc.paper.command.brigadier.BasicCommand;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import net.justrotem.data.PlayerManager;
import net.justrotem.lobby.utils.TextUtils;
import net.justrotem.lobby.utils.Utility;
import org.bukkit.entity.Player;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class FlyCommand implements BasicCommand {

    @Override
    public void execute(CommandSourceStack source, String[] args) {
        if (Utility.isConsole(source)) return;
        Player player = (Player) source.getSender();

        if (args.length == 1) {
            Player target = Utility.getTargetNonNull(player, args[0]);
            if (target == null) return;

            boolean flying = target.getAllowFlight();
            target.setAllowFlight(!flying);
            target.setFlying(!flying);

            player.sendMessage(TextUtils.color("&aTurn %mode% flight for %target%&a!".replace("%mode%", !flying ? "on" : "off").replace("%target%", PlayerManager.getLegacyDisplayName(target))));
            target.sendMessage(TextUtils.color("&aTurn %mode% flight by a Staff member!".replace("%mode%", !flying ? "on" : "off")));
            return;
        }

        boolean flying = player.getAllowFlight();
        player.setAllowFlight(!flying);
        player.setFlying(!flying);

        player.sendMessage(TextUtils.color("&aTurn %mode% flight!".replace("%mode%", !flying ? "on" : "off")));
    }

    @Override
    public Collection<String> suggest(CommandSourceStack source, String[] args) {
        List<String> arguments = new ArrayList<>();

        Utility.addPlayerCompletion(args, 1, arguments, source, "batzal.fly.others");

        return arguments;
    }

    @Override
    public @Nullable String permission() {
        return "batzal.fly";
    }
}