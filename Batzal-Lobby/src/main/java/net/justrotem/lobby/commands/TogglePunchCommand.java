package net.justrotem.lobby.commands;

import io.papermc.paper.command.brigadier.BasicCommand;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import net.justrotem.lobby.hooks.PlayerManager;
import net.justrotem.lobby.utils.PlayerUtility;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class TogglePunchCommand implements BasicCommand {

    @Override
    public void execute(CommandSourceStack source, String[] args) {
        if (PlayerUtility.isConsole(source)) return;
        Player player = (Player) source.getSender();

        PlayerUtility.runTarget(player, args, 1, "batzal.punch.toggle-others", target -> {
            boolean togglePunch = PlayerManager.get(target.getUniqueId()).isTogglePunch();
            PlayerManager.get(target.getUniqueId()).setTogglePunch(!togglePunch);
            return !togglePunch ? "now" : "no longer";
        }, "&aYou will %value% be punched%staff%!", "%target% &awill %value% be punched!");
    }

    @Override
    public @NotNull Collection<String> suggest(@NotNull CommandSourceStack source, String[] args) {
        List<String> arguments = new ArrayList<>();

        PlayerUtility.addPlayerCompletion(args, 1, arguments, source, "batzal.punch.toggle-others");

        return arguments;
    }
}
