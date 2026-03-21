package net.justrotem.lobby.commands;

import io.papermc.paper.command.brigadier.BasicCommand;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import net.justrotem.data.util.TextUtility;
import net.justrotem.lobby.utils.PlayerUtility;
import org.bukkit.entity.Player;

public class ZooCommand implements BasicCommand {
    @Override
    public void execute(CommandSourceStack source, String[] args) {
        if (PlayerUtility.isConsole(source)) return;
        Player player = (Player) source.getSender();

        player.sendMessage(TextUtility.color("&6This isn't the Zoo, it's the lobby!"));
    }
}
