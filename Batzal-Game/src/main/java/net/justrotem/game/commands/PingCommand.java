package net.justrotem.game.commands;

import io.papermc.paper.command.brigadier.BasicCommand;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import net.justrotem.data.PlayerManager;
import net.justrotem.game.utils.TextUtils;
import net.justrotem.game.utils.Utility;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class PingCommand implements BasicCommand {
    @Override
    public void execute(CommandSourceStack source, String[] args) {
        if (Utility.isConsole(source)) return;
        Player player = (Player) source.getSender();

        if (args.length == 1 && player.hasPermission("batzal.ping.others")) {
            Player target = Utility.getTargetNonNull(player, args[0]);
            if (target == null) return;

            player.sendMessage(PlayerManager.getRealDisplayName(target).append(TextUtils.color("&a's ping is %ping%!".replace("%ping%", String.valueOf(target.getPing())))));
            return;
        }

        player.sendMessage(TextUtils.color("&aYour ping is %ping%!".replace("%ping%", String.valueOf(player.getPing()))));
    }

    @Override
    public @NotNull Collection<String> suggest(@NotNull CommandSourceStack source, String[] args) {
        List<String> arguments = new ArrayList<>();

        Utility.addPlayersCompletion(args, 1, arguments, source, "batzal.ping.others");

        return arguments;
    }
}
