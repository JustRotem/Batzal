package net.justrotem.lobby.commands;

import io.papermc.paper.command.brigadier.BasicCommand;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import net.justrotem.data.PlayerManager;
import net.justrotem.lobby.utils.TextUtils;
import net.justrotem.lobby.utils.Utility;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class SudoCommand implements BasicCommand {

    @Override
    public void execute(CommandSourceStack source, String[] args) {
        if (Utility.isConsole(source)) return;
        Player player = (Player) source.getSender();

        if (args.length < 2) {
            player.sendMessage(TextUtils.color("&cUsage: /sudo <player> <command> &8- &e(Make sure the command doesn't start with a /)"));
            return;
        }

        Player target = Utility.getTargetNonNull(player, args[0]);
        if (target == null) return;

        if (args[1].contains("/")) args[1] = args[1].replaceFirst("/", "");
        StringBuilder text = new StringBuilder();
        for (int i = 1; i < args.length; i++) {
            if (i < args.length + 1) text.append(args[i]).append(" ");
        }

        Bukkit.dispatchCommand(target, text.toString());
        player.sendMessage(TextUtils.color("&aYou successfully made %target% &aexecute the command: &3/%command%&a."
                .replace("%target%", PlayerManager.getLegacyDisplayName(target))
                .replace("%command%", text.toString())
        ));
    }

    @Override
    public Collection<String> suggest(CommandSourceStack source, String[] args) {
        List<String> arguments = new ArrayList<>();

        Utility.addPlayerCompletion(args, 1, arguments, source, "batzal.ping.others");
        Utility.addCompletion(args, 2, arguments, Bukkit.getServer().getCommandMap().getKnownCommands().values().toArray());

        return arguments;
    }

    @Override
    public @Nullable String permission() {
        return "batzal.sudo";
    }
}
