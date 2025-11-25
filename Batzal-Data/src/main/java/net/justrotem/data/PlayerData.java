package net.justrotem.data;

import java.util.UUID;

public class PlayerData {

    public static PlayerData create(UUID uuid, String username) {
        return new PlayerData(uuid, username);
    }

    public static PlayerData create(UUID uuid, String username, boolean vanished, boolean togglePunch, boolean toggleChat, int totalExperience, RankColor.Color rankColor, RankColor.PrefixColor prefixColor) {
        return new PlayerData(uuid, username, vanished, toggleChat, togglePunch, totalExperience, rankColor, prefixColor);
    }

    private final UUID uuid;
    private final String username;
    private boolean vanished, toggleChat, togglePunch;
    private int totalExperience;
    private RankColor.Color rankColor;
    private RankColor.PrefixColor prefixColor;

    protected PlayerData(UUID uuid, String username) {
        this.uuid = uuid;
        this.username = username;
        this.vanished = false;
        this.toggleChat = false;
        this.totalExperience = 0;
        this.rankColor = RankColor.Color.Red;
        this.prefixColor = RankColor.PrefixColor.Gold;
    }

    protected PlayerData(UUID uuid, String username, boolean vanished, boolean togglePunch, boolean toggleChat, int totalExperience, RankColor.Color rankColor, RankColor.PrefixColor prefixColor) {
        this.uuid = uuid;
        this.username = username;
        this.vanished = vanished;
        this.toggleChat = toggleChat;
        this.togglePunch = togglePunch;
        this.totalExperience = totalExperience;
        this.rankColor = rankColor;
        this.prefixColor = prefixColor;
    }

    public UUID getUniqueId() {
        return this.uuid;
    }

    public String getUsername() {
        return this.username;
    }

    public boolean isVanished() {
        return this.vanished;
    }

    public boolean isToggleChat() {
        return this.toggleChat;
    }

    public boolean isTogglePunch() {
        return this.togglePunch;
    }

    public int getTotalExperience() {
        return this.totalExperience;
    }

    public RankColor.Color getRankColor() {
        return this.rankColor;
    }

    public RankColor.PrefixColor getPrefixColor() {
        return this.prefixColor;
    }

    public PlayerData setVanished(boolean vanished) {
        this.vanished = vanished;
        return this;
    }

    public PlayerData setToggleChat(boolean toggleChat) {
        this.toggleChat = toggleChat;
        return this;
    }

    public PlayerData setTogglePunch(boolean togglePunch) {
        this.togglePunch = togglePunch;
        return this;
    }

    public PlayerData setTotalExperience(int totalExperience) {
        this.totalExperience = totalExperience;
        return this;
    }

    public PlayerData setRankColor(RankColor.Color rankColor) {
        this.rankColor = rankColor;
        return this;
    }

    public PlayerData setPrefixColor(RankColor.PrefixColor prefixColor) {
        this.prefixColor = prefixColor;
        return this;
    }
}
