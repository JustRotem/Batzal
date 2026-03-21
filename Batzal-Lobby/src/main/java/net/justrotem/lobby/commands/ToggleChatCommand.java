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

public class ToggleChatCommand implements BasicCommand {

    @Override
    public void execute(CommandSourceStack source, String[] args) {
        if (PlayerUtility.isConsole(source)) return;
        Player player = (Player) source.getSender();

        PlayerUtility.runTarget(player, args, 1, "batzal.chat.toggle-others", target -> {
            boolean chatToggled = PlayerManager.isChatToggled(target.getUniqueId());
            PlayerManager.get(target.getUniqueId()).setToggleChat(!chatToggled);
            return !chatToggled ? "now" : "no longer";
        }, "&aYou will %value% see chat%staff%!", "%target% &awill %value% see chat!");
    }

    @Override
    public @NotNull Collection<String> suggest(@NotNull CommandSourceStack source, String[] args) {
        List<String> arguments = new ArrayList<>();

        PlayerUtility.addPlayerCompletion(args, 1, arguments, source, "batzal.chat.toggle-others");

        return arguments;
    }
}