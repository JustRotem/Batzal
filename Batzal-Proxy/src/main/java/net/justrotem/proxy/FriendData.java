package net.justrotem.proxy;

import com.velocitypowered.api.proxy.Player;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class FriendData {

    public static FriendData create(UUID uuid) {
        return new FriendData(uuid);
    }

    public static FriendData create(UUID uuid, boolean notifications, List<UUID> friends, List<UUID> requests) {
        return new FriendData(uuid, notifications, friends, requests);
    }

    private final UUID uuid;
    private boolean dirty, notifications;
    private List<UUID> friends, requests;

    protected FriendData(UUID uuid) {
        this.uuid = uuid;
        this.dirty = true;
        this.notifications = true;
        this.friends = new ArrayList<>();
        this.requests = new ArrayList<>();
    }

    protected FriendData(UUID uuid, boolean notifications, List<UUID> friends, List<UUID> requests) {
        this.uuid = uuid;
        init(notifications, friends, requests);
    }

    private void init(boolean notifications, List<UUID> friends, List<UUID> requests) {
        this.dirty = true;
        this.notifications = notifications;
        this.friends = friends;
        this.requests = requests;
    }

    public UUID getUniqueId() {
        return this.uuid;
    }

    public boolean isDirty() {
        return this.dirty;
    }

    public boolean isEnabledNotifications() {
        return this.notifications;
    }

    public List<UUID> getFriends() {
        return this.friends;
    }

    public List<UUID> getRequests() {
        return this.requests;
    }

    public void setDirty(boolean dirty) {
        this.dirty = dirty;
    }

    public void setNotifications(boolean notifications) {
        this.notifications = notifications;
        this.dirty  = true;
    }

    public void setFriends(List<UUID> friends) {
        this.friends = friends;
        this.dirty = true;
    }

    public void setRequests(List<UUID> requests) {
        this.requests = requests;
        this.dirty = true;
    }

    public void clone(@NotNull FriendData friendData) {
        init(friendData.notifications, friendData.friends, friendData.requests);
    }

    public List<Player> getOnlineFriends() {
        return this.friends.stream()
                .filter(uuid -> Main.getInstance().getProxy().getPlayer(uuid).isPresent())
                .map(uuid -> Main.getInstance().getProxy().getPlayer(uuid).get())
                .toList();
    }
}
