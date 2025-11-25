package net.justrotem.game.nick;

import java.util.UUID;

public class NickData {

    public static NickData create(UUID uuid) {
        return new NickData(uuid);
    }

    public static NickData create(UUID uuid, boolean nicked, String nickname, String skin, String rank) {
        return new NickData(uuid, nicked, nickname, skin, rank);
    }

    private final UUID uuid;
    private boolean nicked;
    private String nickname, skin, rank;

    private NickData(UUID uuid) {
        this.uuid = uuid;
        this.nicked = false;
        this.nickname = null;
        this.skin = null;
        this.rank = null;
    }

    private NickData(UUID uuid, boolean nicked, String nickname, String skin, String rank) {
        this.uuid = uuid;
        this.nicked = nicked;
        this.nickname = nickname;
        this.skin = skin;
        this.rank = rank;
    }

    public UUID getUniqueId() {
        return uuid;
    }

    public boolean isNicked() {
        return nicked;
    }

    public String getNickname() {
        return nickname;
    }

    public String getSkin() {
        return skin;
    }

    public String getRank() {
        return rank;
    }

    public NickData setNicked(boolean nicked) {
        this.nicked = nicked;
        return this;
    }

    public NickData setNickname(String nickname) {
        this.nickname = nickname;
        return this;
    }

    public NickData setSkin(String skin) {
        this.skin = skin;
        return this;
    }

    public NickData setRank(String rank) {
        this.rank = rank;
        return this;
    }
}
