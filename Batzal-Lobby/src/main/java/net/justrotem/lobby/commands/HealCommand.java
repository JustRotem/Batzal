package net.justrotem.lobby.commands;

import io.papermc.paper.command.brigadier.BasicCommand;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import net.justrotem.data.PlayerManager;
import net.justrotem.lobby.utils.TextUtils;
import net.justrotem.lobby.utils.Utility;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.Player;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class HealCommand implements BasicCommand {

    @Override
    public void execute(CommandSourceStack source, String[] args) {
        if (Utility.isConsole(source)) return;
        Player player = (Player) source.getSender();

        if (args.length >= 1) {
            Player target = Utility.getTargetNonNull(player, args[0]);
            if (target == null) return;

            target.setHealth(target.getAttribute(Attribute.MAX_HEALTH).getBaseValue());
            target.setFoodLevel(20);
            player.sendMessage(TextUtils.color("&aYou've healed %target%&a!".replace("%target%", PlayerManager.getLegacyDisplayName(target))));
            target.sendMessage(TextUtils.color("&aYou've been healed by a Staff member!"));
            return;
        }

        player.setHealth(player.getAttribute(Attribute.MAX_HEALTH).getBaseValue());
        player.setFoodLevel(20);
        player.sendMessage(TextUtils.color("&aYou've been healed!"));
    }

    @Override
    public Collection<String> suggest(CommandSourceStack source, String[] args) {
        List<String> arguments = new ArrayList<>();

        Utility.addPlayerCompletion(args, 1, arguments, source, "batzal.heal.others");

        return arguments;
    }

    @Override
    public @Nullable String permission() {
        return "batzal.heal";
    }
}
