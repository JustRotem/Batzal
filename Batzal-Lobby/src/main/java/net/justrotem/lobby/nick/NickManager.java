package net.justrotem.lobby.nick;

import net.justrotem.data.PlayerManager;
import net.justrotem.lobby.Main;
import net.justrotem.lobby.hooks.LuckPermsManager;
import net.justrotem.lobby.skins.SkinManager;
import net.justrotem.lobby.sql.AsyncNickDataManager;
import net.justrotem.lobby.sql.MySQL;
import net.justrotem.lobby.utils.TextUtils;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;

import java.util.HashMap;
import java.util.List;
import java.util.Random;
import java.util.UUID;

public class NickManager implements Listener {

    private static List<String> names;

    public static void initialize(List<String> namesList) {
        if (namesList.isEmpty()) {
            Main.getInstance().getLogger().warning("No available names in names.yml!");
            return;
        }

        names = namesList;
    }

    //<editor-fold desc="Data methods">
    private static final AsyncNickDataManager nickData = MySQL.getNickData();
    private static final HashMap<UUID, NickData> recordedNicks = new HashMap<>();

    public static List<NickData> getPlayers() {
        return recordedNicks.values().stream().toList();
    }

    public static NickData getData(Player player) {
        if (recordedNicks.containsKey(player.getUniqueId())) return recordedNicks.get(player.getUniqueId());

        registerPlayer(player);
        return recordedNicks.get(player.getUniqueId());
    }

    public static NickData getData(String name) {
        return getPlayers().stream().filter(nickData -> PlayerManager.getName(nickData.getUniqueId()).equalsIgnoreCase(name)).findFirst().orElse(null);
    }

    public static NickData getData(UUID uuid) {
        return getPlayers().stream().filter(nickData -> nickData.getUniqueId().equals(uuid)).findFirst().orElse(null);
    }

    public static void registerPlayer(Player player) {
        if (recordedNicks.containsKey(player.getUniqueId())) return;

        recordedNicks.put(player.getUniqueId(), nickData.getNickData(player.getUniqueId()).thenApply(nickData -> {
            if (nickData == null) return NickManager.nickData.registerPlayer(player.getUniqueId());
            return nickData;
        }).join());
    }

    public static void registerAllPlayers() {
        Bukkit.getOnlinePlayers().forEach(NickManager::registerPlayer);
    }

    public static void updatePlayer(Player player, NickData nickData) {
        recordedNicks.put(player.getUniqueId(), nickData);
    }

    public static void savePlayer(Player player) {
        if (recordedNicks.containsKey(player.getUniqueId())) nickData.updatePlayer(recordedNicks.get(player.getUniqueId()));
    }

    public static void saveAllPlayers() {
        recordedNicks.values().forEach(nickData::updatePlayer);
    }
    //</editor-fold>

    //<editor-fold desc="Bukkit methods">
    public static boolean canChange(Player player) {
        return player.hasPermission("batzal.nick.inlobby");
    }

    /**
     * Sets a nickname for a player.
     *
     * @param player the player
     * @param nickname the nickname
     * @param skin the skin
     * @param rank the rank
     */
    public static void setNick(Player player, String nickname, String skin, String rank) {
        if (nickname == null || nickname.isEmpty()) {
            Main.getInstance().getLogger().warning("Check names.yml! Seems to be empty..");
            return;
        }

        if (canChange(player)) {
            player.displayName(TextUtils.color(nickname));
            player.playerListName(TextUtils.color(nickname));
            player.customName(TextUtils.color(nickname));
        }

        if ((skin == null || skin.isEmpty()) && isNicked(player)) skin = getSkin(player);
        if (!(skin == null || skin.isEmpty())) SkinManager.setSkin(player, skin);

        if (rank == null || rank.isEmpty()) {
            if (isNicked(player)) rank = getRank(player);
            else rank = "default";
        }

        if (!(rank == null || rank.isEmpty()) && canChange(player)) LuckPermsManager.setPrefix(player, LuckPermsManager.getLegacyGroupPrefix(rank), 100);

        updatePlayer(player, NickData.create(player.getUniqueId(), true, nickname, skin, rank));
    }

    /**
     * Sets a random nickname for a player.
     *
     * @param player the player
     */
    public static void setRandomNick(Player player) {
        String nickname = getRandomNick(player);
        String skin = SkinManager.getRandomSkin(player).name();
        String rank = RankManager.getRandomRank();

        setNick(player, nickname, skin, rank);
    }

    /**
     * Removes nickname and resets original display.
     */
    public static void resetNick(Player player) {
        updatePlayer(player, getData(player.getUniqueId()).setNicked(false));

        player.displayName(player.name());
        player.playerListName(player.name());
        player.customName(player.name());

        SkinManager.resetSkin(player, true);
        LuckPermsManager.removePrefixes(player, 100);
    }

    /**
     * nick the player as the last used nickname & skin & rank
     * @param player the player
     */
    public static void reuseNick(Player player) {
        NickData data = getData(player.getUniqueId());

        String nickname = data.getNickname();
        String skin = data.getSkin();
        String rank = data.getRank();

        setNick(player, nickname, skin, rank);
    }

    public static void saveSkin(Player player, String name) {
        updatePlayer(player, getData(player.getUniqueId()).setSkin(name));
    }

    public static void resetRankInGame(Player player) {
        LuckPermsManager.removePrefixes(player, 100);
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
        NickData nick = getData(player.getUniqueId());
        if (nick != null) return nick.getNickname();
        return null;
    }

    /**
     * @return current skin, or null if none.
     */
    public static String getSkin(Player player) {
        NickData nick = getData(player.getUniqueId());
        if (nick != null) return nick.getSkin();
        return null;
    }

    /**
     * @return current rank, or null if none.
     */
    public static String getRank(Player player) {
        NickData nick = getData(player.getUniqueId());
        if (nick != null) return nick.getRank();
        return "default";
    }

    public static String getNameAllowedMessage(Player player, String name) {
        String message = "";

        if (name.length() < 3) message = "Your nickname cannot be less than 3 letters";
        else if (name.length() > 16) message = "Your nickname cannot be more than 16 letters";
        else if (TextUtils.containsSpecialChars(name)) message = "Your nickname can contain only 0-9, a-z and A-Z";
        else if (player.getName().equalsIgnoreCase(name)) message = "You can't nick as yourself";
        else if (PlayerManager.isNameRegistered(name) || Bukkit.getPlayer(name) != null || isNicknameUsed(player.getUniqueId(), name)) message = "This name belongs to a known player";

        return message;
    }

    public static boolean isNameRestricted(Player player, String name, boolean clean) {
        String message = getNameAllowedMessage(player, name);

        if (!clean && !message.isEmpty()) {
            player.sendMessage(TextUtils.color("&c" + message + "&c!"));
            return true;
        }

        return false;
    }

    public static boolean isNicked(Player player) {
        try {
            return getData(player.getUniqueId()).isNicked();
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
        return TextUtils.color(getLegacyDisplayName(nickname, rank));
    }

    public static String getLegacyDisplayName(String nickname, String rank) {
        return String.join("", LuckPermsManager.getLegacyGroupPrefix(rank), nickname, LuckPermsManager.getLegacyGroupSuffix(rank));
    }

    public static List<NickData> getNickedPlayers() {
        return recordedNicks.values().stream().filter(NickData::isNicked).toList();
    }
    //</editor-fold>
}
