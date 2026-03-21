package net.justrotem.lobby.commands;

import io.papermc.paper.command.brigadier.BasicCommand;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import net.justrotem.data.util.TextUtility;
import net.justrotem.lobby.hooks.PlayerManager;
import net.justrotem.lobby.nick.NickManager;
import net.justrotem.lobby.utils.PlayerUtility;
import net.justrotem.lobby.vanish.VanishManager;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.List;

public class ListCommand implements BasicCommand {

    @Override
    public void execute(CommandSourceStack source, String[] args) {
        if (PlayerUtility.isConsole(source)) return;
        Player player = (Player) source.getSender();

        List<Component> online = Bukkit.getOnlinePlayers().stream()
                .filter(p -> VanishManager.canSee(player, p))
                .map(p -> {
                    if (VanishManager.isInvisible(p)) return TextUtility.color("&7(V) " + PlayerManager.getLegacyRealDisplayName(p));
                    if (NickManager.isLobbyNicked(p) && NickManager.canSee(player, p)) return TextUtility.color(PlayerManager.getLegacyRealDisplayName(p) + " &8- " + PlayerManager.getLegacyDisplayName(p));
                    return PlayerManager.getDisplayName(p);
                }).toList();

        Component message = TextUtility.color("&eList of all &aOnline &e(&b%size%&e): ".replace("%size%", String.valueOf(online.size())));

        for (int i = 0; i < online.size(); i++) {
            Component component = online.get(i);

            message = message.append(component);

            if (i < online.size() - 1) message = message.append(TextUtility.color("&8, "));
        }

        player.sendMessage(message);
    }
}