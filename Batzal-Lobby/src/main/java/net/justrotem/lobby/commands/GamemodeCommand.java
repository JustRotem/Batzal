package net.justrotem.lobby.commands;

import io.papermc.paper.command.brigadier.BasicCommand;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import net.justrotem.data.utils.TextUtility;
import net.justrotem.lobby.utils.PlayerUtility;
import org.bukkit.GameMode;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class GamemodeCommand implements BasicCommand {

    private final GameMode gameMode;
    public GamemodeCommand(GameMode gameMode) {
        this.gameMode = gameMode;
    }

    @Override
    public void execute(CommandSourceStack source, String[] args) {
        if (PlayerUtility.isConsole(source)) return;
        Player player = (Player) source.getSender();

        GameMode gameMode;
        // Determine GameMode
        if (this.gameMode == null) {
            if (args.length == 0) {
                player.sendMessage(TextUtility.color("&cUsage: /gamemode <survival/creative/adventure/spectator> <player>"));
                return;
            }

            String modeInput = args[0];
            if (modeInput.matches("\\d+")) {
                switch (Integer.parseInt(modeInput)) {
                    case 0 -> gameMode = GameMode.SURVIVAL;
                    case 1 -> gameMode = GameMode.CREATIVE;
                    case 2 -> gameMode = GameMode.ADVENTURE;
                    case 3 -> gameMode = GameMode.SPECTATOR;
                    default -> {
                        player.sendMessage(TextUtility.color("&cThis is an invalid gamemode!"));
                        return;
                    }
                }
            } else {
                try {
                    gameMode = GameMode.valueOf(modeInput.toUpperCase());
                } catch (IllegalArgumentException e) {
                    player.sendMessage(TextUtility.color("&cThis is an invalid gamemode!"));
                    return;
                }
            }
        } else {
            gameMode = this.gameMode;
        }

        PlayerUtility.runTarget(player, args, (this.gameMode == null && args.length >= 2) ? 2 : (this.gameMode != null && args.length >= 1) ? 1 : 0, "", target -> {
            target.setGameMode(gameMode);
            FlyCommand.flyByPermission(target);
            return gameMode.name();
        }, "&aChanged your gamemode to %value%%staff%!", "&aChanged %target%&a's gamemode to %value%!");
    }

    @Override
    public @NotNull Collection<String> suggest(@NotNull CommandSourceStack source, String[] args) {
        List<String> arguments = new ArrayList<>();

        if (this.gameMode == null) PlayerUtility.addCompletion(args, 1, arguments, GameMode.values());

        PlayerUtility.addPlayerCompletion(args, this.gameMode == null ? 2 : 1, arguments, source, "");
        return arguments;
    }

    @Override
    public @Nullable String permission() {
        return "batzal.gamemode";
    }
}