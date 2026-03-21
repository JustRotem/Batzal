package net.justrotem.lobby.menu;

import io.papermc.paper.command.brigadier.BasicCommand;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import net.justrotem.data.player.PlayerData;
import net.justrotem.data.util.TextUtility;
import net.justrotem.lobby.menu.menus.Profile;
import net.justrotem.lobby.menu.menus.PunchMessages;
import net.justrotem.lobby.menu.menus.RankColor;
import net.justrotem.lobby.menu.menus.Rewards;
import net.justrotem.lobby.utils.PlayerUtility;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class MenuCommands implements BasicCommand {

    private final String command;

    public MenuCommands(String command) {
        this.command = command;
    }

    @Override
    public void execute(CommandSourceStack source, String[] args) {
        if (PlayerUtility.isConsole(source)) return;
        Player player = (Player) source.getSender();

        switch (command) {
            case "profile" -> {
                if (args.length >= 1) {
                    PlayerData target = PlayerUtility.getOfflineTarget(player, args[0]);
                    if (target == null) return;

                    Profile.setTarget(player.getUniqueId(), target);
                }

                MenuManager.openMenu(Profile.class, player, null);
            }
            case "openpunchmessagemenu" -> MenuManager.openMenu(PunchMessages.class, player, null);
            case "rewards" -> MenuManager.openMenu(Rewards.class, player, null);
            case "rankcolor" -> {
                if (!player.hasPermission("batzal.rankcolor")) {
                    player.sendMessage(TextUtility.color("&cYou must be &bMVP&c+ to use this!"));
                    return;
                }

                MenuManager.openMenu(RankColor.class, player, null);
            }
        }

    }


    @Override
    public @NotNull Collection<String> suggest(@NotNull CommandSourceStack source, String[] args) {
        List<String> arguments = new ArrayList<>();

        if (command.equals("profile")) PlayerUtility.addPlayerCompletion(args, 1, arguments, source, null);

        return arguments;
    }
}
