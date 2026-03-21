package net.justrotem.lobby.nick;

import io.papermc.paper.command.brigadier.BasicCommand;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import net.justrotem.data.util.TextUtility;
import net.justrotem.lobby.Main;
import net.justrotem.lobby.hooks.LuckPermsManager;
import net.justrotem.lobby.nick.gui.BookManager;
import net.justrotem.lobby.skins.SkinManager;
import net.justrotem.lobby.utils.PlayerUtility;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class NickCommand implements BasicCommand {

    @Override
    public void execute(CommandSourceStack source, String[] args) {
        if (PlayerUtility.isConsole(source)) return;
        Player player = (Player) source.getSender();

        if (args.length > 0) {
            if (args[0].equalsIgnoreCase("skin")) {
                if (args.length == 1) {
                    player.sendMessage(TextUtility.color("&cUsage: /nick skin <random/normal/name>"));
                    return;
                }

                player.sendMessage(TextUtility.color("&eProcessing request. Please wait..."));
                if (args[1].equalsIgnoreCase("random")) {
                    SkinManager.setRandomSkin(player, skinData -> "&aYour skin has been set to %skin%!".replace("%skin%", skinData.getName().toUpperCase()));
                    return;
                }

                if (args[1].equalsIgnoreCase("normal")) {
                    SkinManager.resetSkin(player, skinData -> "&aYou will now have your Minecraft character's skin!");
                    return;
                }

                String skin = args[1];
                if (!SkinManager.isSkinAllowed(player, skin)) return;

                if (skin.equalsIgnoreCase(player.getName())) {
                    SkinManager.resetSkin(player, skinData -> "&aYou will now have your Minecraft character's skin!\n&eNext time use /nick skin normal");
                    return;
                }

                SkinManager.setSkin(player, skin, skinData -> {
                    if (skinData == null) return "&cCould not find a skin by the name '%name%'!".replace("%name%", skin);

                    return "&aYour skin has been set to " + skin.toUpperCase() + "!";
                });

                return;
            }

            if (args[0].equalsIgnoreCase("random")) {
                NickManager.randomNick(player);

                Bukkit.getScheduler().runTaskLater(Main.getInstance(), () -> {
                    player.sendMessage(TextUtility.color("&aSet your nick rank to ")
                            .append(LuckPermsManager.getGroupDisplayName(NickManager.getRank(player)), TextUtility.color("&a!")));
                    player.sendMessage(TextUtility.color("&aYour skin has been set to %skin%!".replace("%skin%", NickManager.getSkin(player).toUpperCase())));
                    player.sendMessage(TextUtility.color("&eGenerating a unique random name. Please wait..."));
                    player.sendMessage(TextUtility.color("&aYou are now nicked as %name%!".replace("%name%", NickManager.getNickName(player))));
                }, 3L);
                return;
            }

            if (args[0].equalsIgnoreCase("reset")) {
                if (!NickManager.isNicked(player)) {
                    player.sendMessage(TextUtility.color("&cYou are not nicked!"));
                    return;
                }

                NickManager.resetNick(player);

                Bukkit.getScheduler().runTaskLater(Main.getInstance(), () -> player.sendMessage(TextUtility.color("&aYour nickname has been reset!")), 3L);
                return;
            }

            if (args[0].equalsIgnoreCase("reuse")) {
                if (NickManager.isNicked(player)) {
                    player.sendMessage(TextUtility.color("&cYou are already nicked!"));
                    return;
                }

                String nickname = NickManager.getNickName(player);
                if (nickname == null || nickname.isEmpty()) {
                    player.sendMessage(TextUtility.color("&cYou have never been nicked! &eYou can use /nick for more information."));
                    return;
                }

                if (NickManager.isNameRestricted(player, nickname, true)) {
                    NickManager.resetNick(player);
                    player.sendMessage(TextUtility.color("&cThere was a problem with your last nickname. &eChange your nickname using /nick"));
                    return;
                }

                NickManager.reuseNick(player);

                Bukkit.getScheduler().runTaskLater(Main.getInstance(), () -> {
                    player.sendMessage(TextUtility.color("&aSet your nick rank to ")
                            .append(LuckPermsManager.getGroupDisplayName(NickManager.getRank(player)), TextUtility.color("&a!")));
                    String skin = NickManager.getSkin(player);
                    if (skin != null && !skin.isEmpty()) player.sendMessage(TextUtility.color("&aYour skin has been set to %skin%!".replace("%skin%", skin.toUpperCase())));
                    player.sendMessage(TextUtility.color("&eProcessing request. Please wait..."));
                    player.sendMessage(TextUtility.color("&aYou are now nicked as %name%!".replace("%name%", nickname)));
                }, 3L);
                return;
            }

            if (args.length == 1 && player.hasPermission("batzal.nick.customname")) {
                String name = args[0];
                if (NickManager.isNameRestricted(player, name, false)) return;

                NickManager.nick(player, name, null, null);

                Bukkit.getScheduler().runTaskLater(Main.getInstance(), () -> {
                    player.sendMessage(TextUtility.color("&aSet your nick rank to ")
                            .append(LuckPermsManager.getGroupDisplayName(NickManager.getRank(player)), TextUtility.color("&a!")));
                    String skin = NickManager.getSkin(player);
                    if (skin != null && !skin.isEmpty()) player.sendMessage(TextUtility.color("&aYour skin has been set to %skin%!".replace("%skin%", skin.toUpperCase())));
                    player.sendMessage(TextUtility.color("&eProcessing request. Please wait..."));
                    player.sendMessage(TextUtility.color("&aYou are now nicked as %name%!".replace("%name%", name)));
                }, 3L);
                return;
            }
        }

        BookManager.openBook(player, "nick");
    }

    @Override
    public Collection<String> suggest(CommandSourceStack source, String[] args) {
        List<String> arguments = new ArrayList<>();

        PlayerUtility.addCompletion(args, 1, arguments, "reset", "random", "reuse", "skin");
        PlayerUtility.addCompletion(args, 2, arguments, "normal", "random", "steve", "alex");

        return arguments;
    }

    @Override
    public @Nullable String permission() {
        return "batzal.nick.use";
    }

    public static class UnNickCommand implements BasicCommand {

        @Override
        public void execute(CommandSourceStack source, String[] args) {
            if (PlayerUtility.isConsole(source)) return;
            Player player = (Player) source.getSender();

            if (!NickManager.isNicked(player)) {
                player.sendMessage(TextUtility.color("&cYou are not nicked!"));
                return;
            }

            NickManager.resetNick(player);

            Bukkit.getScheduler().runTaskLater(Main.getInstance(), () -> player.sendMessage(TextUtility.color("&aYour nickname has been reset!")), 3L);
        }

        @Override
        public @Nullable String permission() {
            return new NickCommand().permission();
        }
    }
}
