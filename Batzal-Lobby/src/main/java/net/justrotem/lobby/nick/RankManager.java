package net.justrotem.lobby.nick;

import net.justrotem.lobby.Main;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.Random;

public class RankManager {

    private static final List<String> RANKS = Main.getInstance().getConfig().getStringList("nick.ranks");

    private static final Random RANDOM = new Random();

    public static List<String> getRanks() {
        return RANKS;
    }

    public static String getRandomRank() {
        return RANKS.get(RANDOM.nextInt(RANKS.size()));
    }

    public static String getRank(Player player) {
        return NickManager.getRank(player);
    }
}
