package net.justrotem.game.vanish;

import io.papermc.paper.command.brigadier.BasicCommand;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import net.justrotem.data.PlayerManager;
import net.justrotem.game.utils.TextUtils;
import net.justrotem.game.utils.Utility;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class VanishCommand implements BasicCommand {
    @Override
    public void execute(CommandSourceStack source, String[] args) {
        if (Utility.isConsole(source)) return;
        Player player = (Player) source.getSender();

        if (args.length == 1 && player.hasPermission("batzal.vanish.others")) {
            Player target = Utility.getTargetNonNull(player, args[0]);
            if (target == null) return;

            boolean vanished = VanishManager.isVanished(target);

            if (vanished) VanishManager.showPlayer(target);
            else VanishManager.hidePlayer(target);

            target.sendMessage(TextUtils.color("&aYou have " + (vanished ? "reappeared" : "vanished") + " by a Staff Member!"));
            player.sendMessage(PlayerManager.getRealDisplayName(target).append(TextUtils.color(" &ahas been " + (vanished ? "reappeared" : "vanished") + "!")));
            return;
        }

        boolean vanished = VanishManager.isVanished(player);

        if (vanished) VanishManager.showPlayer(player);
        else VanishManager.hidePlayer(player);

        player.sendMessage(TextUtils.color("&aYou have " + (vanished ? "reappeared" : "vanished") + "!"));
    }

    @Override
    public Collection<String> suggest(CommandSourceStack source, String[] args) {
        List<String> arguments = new ArrayList<>();

        Utility.addPlayersCompletion(args, 1, arguments, source, "batzal.vanish.others");

        return arguments;
    }

    @Override
    public @Nullable String permission() {
        return "batzal.vanish.ingame";
    }
}
