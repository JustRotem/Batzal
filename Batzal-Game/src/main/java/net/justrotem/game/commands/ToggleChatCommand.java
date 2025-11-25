package net.justrotem.game.commands;

import io.papermc.paper.command.brigadier.BasicCommand;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import net.justrotem.data.PlayerManager;
import net.justrotem.game.utils.TextUtils;
import net.justrotem.game.utils.Utility;
import org.bukkit.entity.Player;

public class ToggleChatCommand implements BasicCommand {
    @Override
    public void execute(CommandSourceStack source, String[] args) {
        if (Utility.isConsole(source)) return;
        Player player = (Player) source.getSender();

        boolean chatToggled = PlayerManager.isChatToggled(player);
        PlayerManager.updatePlayer(player, PlayerManager.getData(player).setToggleChat(!chatToggled));

        player.sendMessage(TextUtils.color(chatToggled ? "&aYou will now see chat!" : "&cYou will no longer see chat!"));
    }
}