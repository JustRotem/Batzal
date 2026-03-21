package net.justrotem.lobby.nick;

import net.justrotem.data.util.TextUtility;
import net.justrotem.lobby.Main;
import net.justrotem.lobby.commands.FlyCommand;
import net.justrotem.lobby.hooks.LuckPermsManager;
import net.justrotem.lobby.hooks.PlayerManager;
import net.justrotem.lobby.skins.SkinManager;
import net.justrotem.lobby.sql.MySQL;
import net.justrotem.lobby.sql.NickDataManager;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.*;
import java.util.concurrent.CancellationException;

public class NickManager {

    //<editor-fold desc="Data methods">

    private static final HashMap<UUID, NickData> CACHE = new HashMap<>();
    private static final NickDataManager sql = MySQL.getNickData();

    public static void startAutoSave(JavaPlugin plugin) {
        Bukkit.getScheduler().runTaskTimerAsynchronously(plugin, () -> {
            for (UUID uuid : CACHE.keySet()) {
                NickData nickData = CACHE.get(uuid);
                if (!nickData.isDirty()) continue;

                sql.update(nickData);
                nickData.setDirty(false);
            }
        }, 20 * 30, 20 * 30); // every 30 seconds
    }

    public static void register(UUID uuid) {
        NickData nickData = get(uuid);
        if (nickData == null) nickData = NickData.create(uuid);

        update(nickData);
    }

    private static void update(NickData nickData) {
        if (CACHE.containsKey(nickData.getUniqueId())) CACHE.get(nickData.getUniqueId()).clone(nickData);
        CACHE.put(nickData.getUniqueId(), nickData);
    }

    public static NickData get(UUID uuid) {
        if (uuid == null) return null;

        if (CACHE.containsKey(uuid)) return CACHE.get(uuid);

        try {
            return sql.getData(uuid).join();
        } catch (CancellationException e) {
            return null;
        }
    }

    public static List<NickData> getAll() {
        return CACHE.values().stream().toList();
    }

    public static void saveAndRemove(UUID uuid) {
        NickData nickData = get(uuid);
        if (nickData == null) return;

        sql.update(nickData);
        CACHE.remove(nickData.getUniqueId());
    }

    public static void saveAll() {
        CACHE.values().forEach(sql::update);
    }

    public static boolean isRegistered(UUID uuid) {
        if (uuid == null) return false;

        if (CACHE.containsKey(uuid)) return true;

        try {
            return sql.getData(uuid).join() != null;
        } catch (CancellationException e) {
            return false;
        }
    }

    //</editor-fold>

    //<editor-fold desc="Config">
    private static List<String> names;

    public static void initialize(List<String> namesList) {
        if (namesList.isEmpty()) {
            Main.getInstance().getLogger().warning("No available names in names.yml!");
            return;
        }

        names = namesList;
    }
    //</editor-fold>

    //<editor-fold desc="Bukkit methods">
    public static boolean canChange(Player player) {
        return player.hasPermission("batzal.nick.inlobby");
    }

    /**
     * Sets a nickname, skin and rank for Player.
     *
     * @param nickname the nickname
     * @param skin the skin
     * @param rank the rank
     */
    public static void nick(Player player, String nickname, String skin, String rank) {
        if (nickname == null || nickname.isEmpty()) {
            Main.getInstance().getLogger().warning("Check names.yml! Seems to be empty..");
            return;
        }

        if (canChange(player)) {
            player.displayName(TextUtility.color(nickname));
            player.playerListName(TextUtility.color(nickname));
            player.customName(TextUtility.color(nickname));
        }

        if ((skin == null || skin.isEmpty()) && isNicked(player)) skin = getSkin(player);
        if (!(skin == null || skin.isEmpty()) && canChange(player)) SkinManager.setSkin(player, skin);

        if (rank == null || rank.isEmpty()) {
            if (isNicked(player)) rank = getRank(player);
            else rank = "default";
        }

        if (!(rank == null || rank.isEmpty()) && canChange(player)) LuckPermsManager.setPrefix(player.getUniqueId(), LuckPermsManager.getLegacyGroupPrefix(rank), 100);

        update(NickData.create(player.getUniqueId(), true, nickname, skin, rank));

        FlyCommand.flyByPermission(player);
    }

    /**
     * Sets a random nickname for a player.
     */
    public static void randomNick(Player player) {
        String nickname = getRandomNick(player);
        String skin;
        try {
            skin = Objects.requireNonNull(SkinManager.getRandomSkin(player)).getName();
        } catch (NullPointerException e) {
            skin = null;
        }
        String rank = RankManager.getRandomRank();

        nick(player, nickname, skin, rank);
    }

    /**
     * Removes nickname and resets original display.
     */
    public static void resetNick(Player player) {
        get(player.getUniqueId()).setNicked(false);

        player.displayName(player.name());
        player.playerListName(player.name());
        player.customName(player.name());

        SkinManager.resetSkin(player);
        LuckPermsManager.removePrefixes(player.getUniqueId(), 100);
    }

    /**
     * nick the player as the last used nickname & skin & rank
     * @param player the player
     */
    public static void reuseNick(Player player) {
        NickData data = get(player.getUniqueId());

        String nickname = data.getNickname();
        String skin = data.getSkin();
        String rank = data.getRank();

        nick(player, nickname, skin, rank);
    }

    public static void resetInGameRank(Player player) {
        LuckPermsManager.removePrefixes(player.getUniqueId(), 100);
    }

    /**
     * @return A random nickname from names.yml
     */
    public static String getRandomNick(Player player) {
        String nickname = names.get(new Random().nextInt(names.size()));

        if (nickname.isEmpty()) return "";

        if (isNicked(player) && getNickName(player) != null)
            if (getNickName(player).equalsIgnoreCase(nickname)) return getRandomNick(player);

        return nickname;
    }

    /**
     * @return current nickname, or null if none.
     */
    public static String getNickName(Player player) {
        NickData nick = get(player.getUniqueId());
        if (nick != null) return nick.getNickname();
        return null;
    }

    /**
     * @return current skin, or null if none.
     */
    public static String getSkin(Player player) {
        NickData nick = get(player.getUniqueId());
        if (nick != null) return nick.getSkin();
        return null;
    }

    /**
     * @return current rank, or null if none.
     */
    public static String getRank(Player player) {
        NickData nick = get(player.getUniqueId());
        if (nick != null) return nick.getRank();
        return "default";
    }

    public static String getNameAllowedMessage(Player player, String name) {
        String message = "";

        if (name.length() < 3) message = "Your nickname cannot be less than 3 letters";
        else if (name.length() > 16) message = "Your nickname cannot be more than 16 letters";
        else if (TextUtility.containsSpecialChars(name)) message = "Your nickname can contain only 0-9, a-z and A-Z";
        else if (player.getName().equalsIgnoreCase(name)) message = "You can't nick as yourself";
        else if (PlayerManager.isRegisteredOffline(name) || Bukkit.getPlayer(name) != null || isNicknameUsed(player.getUniqueId(), name)) message = "This name belongs to a known player";

        return message;
    }

    public static boolean isNameRestricted(Player player, String name, boolean clean) {
        String message = getNameAllowedMessage(player, name);

        if (!clean && !message.isEmpty()) {
            player.sendMessage(TextUtility.color("&c" + message + "&c!"));
            return true;
        }

        return false;
    }

    public static boolean isNicked(Player player) {
        try {
            return get(player.getUniqueId()).isNicked();
        } catch (NullPointerException e) {
            return false;
        }
    }

    public static boolean isLobbyNicked(Player player) {
        return isNicked(player) && canChange(player) && hasDifferentName(player);
    }

    public static boolean canSee(CommandSender sender, Player target) {
        if (sender.hasPermission("batzal.nick.see")) return true;
        if (target != null) return !isLobbyNicked(target);

        return false;
    }

    public static boolean canSee(CommandSender sender) {
        return canSee(sender, null);
    }

    public static boolean hasDifferentName(Player player) {
        return !player.displayName().equals(player.name());
    }

    public static boolean isNicknameUsed(UUID uuid, String name) {
        return getNickedPlayers().stream().filter(data -> !data.getUniqueId().equals(uuid)).map(NickData::getNickname).anyMatch(nickname -> nickname.equalsIgnoreCase(name));
    }

    public static Component getDisplayName(String nickname, String rank) {
        return TextUtility.color(getLegacyDisplayName(nickname, rank));
    }

    public static String getLegacyDisplayName(String nickname, String rank) {
        return String.join("", LuckPermsManager.getLegacyGroupPrefix(rank), nickname, LuckPermsManager.getLegacyGroupSuffix(rank));
    }

    public static List<NickData> getNickedPlayers() {
        return getAll().stream().filter(NickData::isNicked).toList();
    }
    //</editor-fold>
}
