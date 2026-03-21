package net.justrotem.lobby.commands;

import io.papermc.paper.command.brigadier.BasicCommand;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import net.justrotem.data.utils.ToggleManager;
import net.justrotem.lobby.Main;
import net.justrotem.lobby.utils.PlayerUtility;
import org.bukkit.entity.Player;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class GodCommand implements BasicCommand {

    @Override
    public void execute(CommandSourceStack source, String[] args) {
        if (PlayerUtility.isConsole(source)) return;
        Player player = (Player) source.getSender();

        PlayerUtility.runTarget(player, args, 1, permission() + ".others", target -> {
            ToggleManager.toggle(Main.ToggleCategory.God, target.getUniqueId());
            return isOn(target) ? "on" : "off";
        }, "&aTurned God mode %value%!%staff%", "&aTurned God mode %value% for %target%&a!");
    }

    @Override
    public Collection<String> suggest(CommandSourceStack source, String[] args) {
        List<String> arguments = new ArrayList<>();

        PlayerUtility.addPlayerCompletion(args, 1, arguments, source, permission() + ".others");

        return arguments;
    }

    @Override
    public @Nullable String permission() {
        return "batzal.god";
    }

    public static boolean isOn(Player player) {
        return ToggleManager.isOn(Main.ToggleCategory.God, player.getUniqueId());
    }
}