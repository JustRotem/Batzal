package net.justrotem.lobby.commands;

import io.papermc.paper.command.brigadier.BasicCommand;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import net.justrotem.data.PlayerManager;
import net.justrotem.lobby.utils.TextUtils;
import net.justrotem.lobby.utils.Utility;
import org.bukkit.entity.Player;

public class TogglePunchCommand implements BasicCommand {
    @Override
    public void execute(CommandSourceStack source, String[] args) {
        if (Utility.isConsole(source)) return;
        Player player = (Player) source.getSender();

        boolean togglePunch = PlayerManager.getData(player).isTogglePunch();
        PlayerManager.updatePlayer(player, PlayerManager.getData(player).setTogglePunch(!togglePunch));

        player.sendMessage(TextUtils.color(togglePunch ? "&aYou will now be punched!" : "&cYou will no longer be punched!"));
    }
}
