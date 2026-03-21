package net.justrotem.lobby.nick;

import java.util.UUID;

public class NickData {

    public static NickData create(UUID uuid) {
        return new NickData(uuid);
    }

    public static NickData create(UUID uuid, boolean nicked, String nickname, String skin, String rank) {
        return new NickData(uuid, nicked, nickname, skin, rank);
    }

    private final UUID uuid;
    private boolean dirty;
    private boolean nicked;
    private String nickname, skin, rank;

    protected NickData(UUID uuid) {
        this.uuid = uuid;
        this.dirty = true;
        this.nicked = false;
        this.nickname = null;
        this.skin = null;
        this.rank = null;
    }

    protected NickData(UUID uuid, boolean nicked, String nickname, String skin, String rank) {
        this.uuid = uuid;
        init(nicked, nickname, skin, rank);
    }

    private void init(boolean nicked, String nickname, String skin, String rank) {
        this.dirty = true;
        this.nicked = nicked;
        this.nickname = nickname;
        this.skin = skin;
        this.rank = rank;
    }

    public UUID getUniqueId() {
        return this.uuid;
    }

    public boolean isDirty() {
        return this.dirty;
    }

    public boolean isNicked() {
        return this.nicked;
    }

    public String getNickname() {
        return this.nickname;
    }

    public String getSkin() {
        return this.skin;
    }

    public String getRank() {
        return this.rank;
    }

    public void setDirty(boolean dirty) {
        this.dirty = dirty;
    }

    public void setNicked(boolean nicked) {
        this.nicked = nicked;
        this.dirty = true;
    }

    public void setNickname(String nickname) {
        this.nickname = nickname;
        this.dirty = true;
    }

    public void setSkin(String skin) {
        this.skin = skin;
        this.dirty = true;
    }

    public void setRank(String rank) {
        this.rank = rank;
        this.dirty = true;
    }

    public void clone(NickData nickData) {
        init(nickData.nicked, nickData.nickname, nickData.skin, nickData.rank);
    }
}
