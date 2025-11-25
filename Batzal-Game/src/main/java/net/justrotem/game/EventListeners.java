package net.justrotem.game;

import io.papermc.paper.event.player.AsyncChatEvent;
import net.justrotem.data.PlayerManager;
import net.justrotem.data.hooks.LuckPermsManager;
import net.justrotem.game.nick.NickManager;
import net.justrotem.game.utils.TextUtils;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

public class EventListeners implements Listener {

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        NickManager.registerPlayer(player);

        // Check moderator or admin if vanished

        if (NickManager.isNicked(player)) {
            String nickname = NickManager.getNickName(player);
            if (nickname != null) {
                if (NickManager.isNameRestricted(player, nickname, true)) {
                    NickManager.resetNick(player);
                    player.sendMessage(TextUtils.color("&cThere was a problem with your last nickname. &eChange your nickname using /nick"));
                } else {
                    NickManager.reuseNick(player);
                    player.sendMessage(TextUtils.color("&aYou are now nicked as %name%!".replace("%name%", nickname)));
                }
            }
        }

        event.joinMessage(null);
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        NickManager.savePlayer(player);

        event.quitMessage(null);
    }

    @EventHandler
    public void onChat(AsyncChatEvent event) {
        if (event.isCancelled()) return;

        event.setCancelled(true);

        Player player = event.getPlayer();

        // Prefix
        Component message;
        if (LuckPermsManager.isHooked()) {
            String rank;
            if (NickManager.isNicked(player)) rank = NickManager.getRank(player);
            else rank = LuckPermsManager.getPrimaryGroup(player.getUniqueId());

            message = TextUtils.color("&" + (rank.equalsIgnoreCase("default") ? '7' : 'f'))
                    .append(PlayerManager.getDisplayName(player))
                    .append(TextUtils.color(": "));
        } else message = Component.text("<", NamedTextColor.WHITE).append(player.displayName(), Component.text("> "));

        // Checks if mentioning a Player
        String plainMention = TextUtils.escapeTags(TextUtils.serialize(event.message(), TextUtils.Format.PLAIN));

        List<Player> mentionedPlayers = new ArrayList<>();
        for (Player p : Bukkit.getOnlinePlayers()) {
            if (p.equals(player)) continue;

            String name;
            if (NickManager.isNicked(p)) name = NickManager.getNickName(p);
            else name = p.getName();

            if (plainMention.toLowerCase().contains(name.toLowerCase())) {
                plainMention = plainMention.replaceAll("(?i)" + Pattern.quote(name), "<yellow>" + name + "</yellow>");
                mentionedPlayers.add(p);
            }
        }

        // Colored (legacy & codes), for mentioned players only
        Component mentionMessage = player.hasPermission("batzal.chat.colors")
                ? TextUtils.color(plainMention)
                : TextUtils.deserialize(plainMention, TextUtils.Format.MINIMESSAGE);

        // Plain, for non-mentioned players
        Component plainMessage = player.hasPermission("batzal.chat.colors")
                ? TextUtils.color(TextUtils.serialize(event.message(), TextUtils.Format.PLAIN))
                : event.message();

        for (Audience viewer : event.viewers()) {
            if (viewer instanceof Player p && mentionedPlayers.contains(p)) {
                event.message(mentionMessage);
                p.playSound(p.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1f, 1f);
            }
            else event.message(plainMessage);

            viewer.sendMessage(message.append(event.message()));
        }
    }
}
