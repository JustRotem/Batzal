package net.justrotem.lobby.commands;

import io.papermc.paper.command.brigadier.BasicCommand;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import net.justrotem.data.util.TextUtility;
import net.justrotem.lobby.utils.PlayerUtility;
import org.bukkit.Bukkit;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class SudoCommand implements BasicCommand {

    @Override
    public void execute(CommandSourceStack source, String[] args) {
        if (args.length < 2) {
            source.getSender().sendMessage(TextUtility.color("&cUsage: /sudo <player> <command> &8- &e(Make sure the command doesn't start with a /)"));
            return;
        }

        if (args[1].contains("/")) args[1] = args[1].replaceFirst("/", "");
        StringBuilder text = new StringBuilder();
        for (int i = 1; i < args.length; i++) {
            if (i < args.length + 1) text.append(args[i]).append(" ");
        }

        PlayerUtility.runTarget(source.getSender(), args, 1, permission() + ".others", target -> {
            Bukkit.dispatchCommand(target, text.toString());
            return text.toString();
        }, "", "&aYou successfully made %target% &aexecute the command: &3/%value%&a.");
    }

    @Override
    public Collection<String> suggest(CommandSourceStack source, String[] args) {
        List<String> arguments = new ArrayList<>();

        PlayerUtility.addPlayerCompletion(args, 1, arguments, source, permission() + ".others");
        PlayerUtility.addCompletion(args, 2, arguments, Bukkit.getServer().getCommandMap().getKnownCommands().values().toArray());

        return arguments;
    }

    @Override
    public @Nullable String permission() {
        return "batzal.sudo";
    }
}
