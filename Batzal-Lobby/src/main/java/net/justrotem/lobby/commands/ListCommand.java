package net.justrotem.lobby.commands;

import io.papermc.paper.command.brigadier.BasicCommand;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import net.justrotem.data.PlayerManager;
import net.justrotem.lobby.nick.NickManager;
import net.justrotem.lobby.utils.TextUtils;
import net.justrotem.lobby.utils.Utility;
import net.justrotem.lobby.vanish.VanishManager;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.Collection;
import java.util.List;
import java.util.UUID;
import java.util.stream.Stream;

public class ListCommand implements BasicCommand {

    @Override
    public void execute(CommandSourceStack source, String[] args) {
        if (Utility.isConsole(source)) return;
        Player player = (Player) source.getSender();

        List<Component> online = Bukkit.getOnlinePlayers().stream()
                .filter(p -> VanishManager.canSee(player, p))
                .map(p -> {
                    if (VanishManager.isInvisible(p)) return TextUtils.color("&7(V) " + PlayerManager.getLegacyRealDisplayName(p));
                    if (NickManager.isLobbyNicked(p) && NickManager.canSee(player, p)) return TextUtils.color(PlayerManager.getLegacyRealDisplayName(p) + " &8- " + PlayerManager.getLegacyDisplayName(p));
                    return PlayerManager.getDisplayName(p);
                }).toList();

        Component message = TextUtils.color("&eList of all &aOnline &e(&b%size%&e): ".replace("%size%", String.valueOf(online.size())));

        for (int i = 0; i < online.size(); i++) {
            Component component = online.get(i);

            message = message.append(component);

            if (i < online.size() - 1) message = message.append(TextUtils.color("&8, "));
        }

        player.sendMessage(message);
    }
}