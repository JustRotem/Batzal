package net.justrotem.data.model;

import net.justrotem.data.enums.*;

import java.util.UUID;

/**
 * Represents all persistent player data stored in the system.
 *
 * <p>This object is mutable and uses a "dirty" flag to track changes
 * that need to be persisted.</p>
 */
public class PlayerData {

    /**
     * Factory method for creating a PlayerData instance.
     */
    public static PlayerData create(UUID uuid, String name, String value, String signature, boolean vanished, boolean toggleChat, boolean togglePunch, int totalExperience, RankColor.Color rankColor, RankColor.PrefixColor prefixColor, PunchMessage punchMessage, MessageMode messageMode, Status status, Visibility.State visibilityState) {
        return new PlayerData(uuid, name, value, signature, vanished, toggleChat, togglePunch, totalExperience, rankColor, prefixColor, punchMessage, messageMode, status, visibilityState);
    }

    private final UUID uuid;
    private boolean dirty;
    private String name, value, signature;
    private boolean vanished, toggleChat, togglePunch;
    private int totalExperience;
    private RankColor.Color rankColor;
    private RankColor.PrefixColor prefixColor;
    private PunchMessage punchMessage;
    private MessageMode messageMode;
    private Status status;
    private Visibility.State visibilityState;

    protected PlayerData(UUID uuid, String name, String value, String signature, boolean vanished, boolean toggleChat, boolean togglePunch, int totalExperience, RankColor.Color rankColor, RankColor.PrefixColor prefixColor, PunchMessage punchMessage, MessageMode messageMode, Status status, Visibility.State visibilityState) {
        this.uuid = uuid;
        init(name, value, signature, vanished, toggleChat, togglePunch, totalExperience, rankColor, prefixColor, punchMessage, messageMode, status, visibilityState);
    }

    /**
     * Initializes or reinitializes all fields.
     *
     * <p>Marks the object as dirty.</p>
     */
    protected void init(String name, String value, String signature, boolean vanished, boolean toggleChat, boolean togglePunch, int totalExperience, RankColor.Color rankColor, RankColor.PrefixColor prefixColor, PunchMessage punchMessage, MessageMode messageMode, Status status, Visibility.State visibilityState) {
        this.dirty = true;
        this.name = name;
        this.signature = signature;
        this.value = value;
        this.vanished = vanished;
        this.toggleChat = toggleChat;
        this.togglePunch = togglePunch;
        this.totalExperience = totalExperience;
        this.rankColor = rankColor;
        this.prefixColor = prefixColor;
        this.punchMessage = punchMessage;
        this.messageMode = messageMode;
        this.status = status;
        this.visibilityState = visibilityState;
    }

    public UUID getUniqueId() {
        return this.uuid;
    }

    public boolean isDirty() {
        return this.dirty;
    }

    public String getName() {
        return this.name;
    }

    public String getValue() {
        return this.value;
    }

    public String getSignature() {
        return this.signature;
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

    public PunchMessage getPunchMessage() {
        return this.punchMessage;
    }

    public MessageMode getMessageMode() {
        return this.messageMode;
    }

    public Status getStatus() {
        return this.status;
    }

    public Visibility.State getVisibilityState() {
        return this.visibilityState;
    }

    public void setDirty(boolean dirty) {
        this.dirty = dirty;
    }

    public void setVanished(boolean vanished) {
        this.vanished = vanished;
        this.dirty = true;
    }

    public void setToggleChat(boolean toggleChat) {
        this.toggleChat = toggleChat;
        this.dirty = true;
    }

    public void setTogglePunch(boolean togglePunch) {
        this.togglePunch = togglePunch;
        this.dirty = true;
    }

    public void setTotalExperience(int totalExperience) {
        this.totalExperience = totalExperience;
        this.dirty = true;
    }

    public void setRankColor(RankColor.Color rankColor) {
        this.rankColor = rankColor;
        this.dirty = true;
    }

    public void setPrefixColor(RankColor.PrefixColor prefixColor) {
        this.prefixColor = prefixColor;
        this.dirty = true;
    }

    public void setPunchMessage(PunchMessage punchMessage) {
        this.punchMessage = punchMessage;
        this.dirty = true;
    }

    public void setMessageMode(MessageMode messageMode) {
        this.messageMode = messageMode;
        this.dirty = true;
    }

    public void setStatus(Status status) {
        this.status = status;
        this.dirty = true;
    }

    public void setVisibilityState(Visibility.State visibilityState) {
        this.visibilityState = visibilityState;
        this.dirty = true;
    }

    /**
     * Updates name and skin data if changed.
     *
     * <p>Marks as dirty only if a change occurs.</p>
     */
    public void updatePlayer(String name, String value, String signature) {
        boolean updated = false;

        if (name != null && !name.isEmpty() && !name.equals(this.name)) {
            this.name = name;
            updated = true;
        }

        if (value != null && !value.isEmpty() && !value.equals(this.value)) {
            this.value = value;
            updated = true;
        }

        if (signature != null && !signature.isEmpty() && !signature.equals(this.signature)) {
            this.signature = signature;
            updated = true;
        }

        if (updated) this.dirty = true;
    }

    /**
     * Copies all data from another PlayerData instance.
     *
     * <p>This resets the current state and marks it as dirty.</p>
     */
    public void copyFrom(PlayerData playerData) {
        init(playerData.name, playerData.value, playerData.signature, playerData.vanished, playerData.toggleChat, playerData.togglePunch, playerData.totalExperience, playerData.rankColor, playerData.prefixColor, playerData.punchMessage, playerData.messageMode, playerData.status, playerData.visibilityState);
    }
}
