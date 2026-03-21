package net.justrotem.lobby.vanish;

import io.papermc.paper.command.brigadier.BasicCommand;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import net.justrotem.lobby.commands.FlyCommand;
import net.justrotem.lobby.utils.PlayerUtility;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class VanishCommand implements BasicCommand {

    @Override
    public void execute(CommandSourceStack source, String[] args) {
        if (PlayerUtility.isConsole(source)) return;
        Player player = (Player) source.getSender();

        PlayerUtility.runTarget(player, args, 1, permission() + ".others", target -> {
            boolean vanished = VanishManager.isInvisible(target);
            if (vanished) VanishManager.showPlayer(target);
            else VanishManager.hidePlayer(target);
            FlyCommand.flyByPermission(player);

            return (vanished ? "reappeared" : "vanished");
        },"&aYou have %value%%staff%!", "%target% &ahas been %value%!");
    }

    @Override
    public @NotNull Collection<String> suggest(@NotNull CommandSourceStack source, String[] args) {
        List<String> arguments = new ArrayList<>();

        PlayerUtility.addPlayerCompletion(args, 1, arguments, source, permission() + ".others");

        return arguments;
    }

    @Override
    public @Nullable String permission() {
        return "batzal.vanish.use";
    }
}
