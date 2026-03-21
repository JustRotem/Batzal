package net.justrotem.lobby.menu.menus;

import net.justrotem.data.utils.TextUtility;
import net.justrotem.lobby.hooks.PlayerManager;
import net.justrotem.lobby.menu.Menu;
import net.justrotem.lobby.menu.PlayerMenuUtility;
import net.justrotem.lobby.utils.ItemUtility;
import net.justrotem.lobby.utils.LobbyManager;
import org.bukkit.Material;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class LobbySelector extends Menu {

    public LobbySelector(PlayerMenuUtility playerMenuUtility) {
        super(playerMenuUtility);
    }

    @Override
    public String getMenuName() {
        return LobbyManager.getServerType() + " Lobby Selector";
    }

    @Override
    public int getSlots() {
        return 18;
    }

    @Override
    public boolean cancelAllClicks() {
        return false;
    }

    @Override
    public void handleMenu(InventoryClickEvent event) {
        for (LobbyManager.Lobby lobby : LobbyManager.getLobbies()) {
            if (event.getSlot() == getSlot(lobby)) {
                connect(lobby);
                break;
            }
        }
    }

    @Override
    public void setMenuItems() {
        for (LobbyManager.Lobby lobby : LobbyManager.getLobbies()) {
            inventory.setItem(getSlot(lobby), getLobbyItem(lobby));
        }
    }

    private int getSlot(LobbyManager.Lobby lobby) {
        return getLobbyNumber(lobby) - 1;
    }

    private int getLobbyNumber(LobbyManager.Lobby lobby) {
        String[] parts = lobby.getServerName().split("-");
        try {
            return Integer.parseInt(parts[parts.length - 1]);  // last part
        } catch (NumberFormatException ignored) {
            return 1; // invalid name
        }
    }

    private ItemStack getLobbyItem(LobbyManager.Lobby lobby) {
        Material material;
        boolean joinable = true;

        if (lobby.isSameServer() || lobby.isFull()) {
            material = Material.RED_TERRACOTTA;
            joinable = false;
        }
        else if (!lobby.isOnline()) {
            material = Material.BLACK_TERRACOTTA;
            joinable = false;
        }
        else if (!getOnlineFriends(lobby).isEmpty()) material = Material.LIGHT_BLUE_TERRACOTTA;
        else material = Material.QUARTZ_BLOCK;

        return ItemUtility.createItem(material,
                (joinable ? "&a" : "&c") + LobbyManager.getServerType()  + " Lobby #" + getLobbyNumber(lobby),
                getLore(lobby)
        );
    }

    private List<String> getLore(LobbyManager.Lobby lobby) {
        String online = getOnlineFriends(lobby);
        String lastLine = lobby.isSameServer() ? "&cAlready connected!" : !lobby.isOnline() ? "&cServer is offline!" : lobby.isFull() ? "&cServer is full!" : "&eClick to connect!";

        String players = "&7Players: %players%/%max_players%".replace("%players%", String.valueOf(lobby.getOnlinePlayers())).replace("%max_players%", String.valueOf(lobby.getMaxPlayers()));
        if (!online.isEmpty() && lobby.isOnline() && !lobby.isSameServer()) return List.of(
                players,
                "",
                "&7Online Friends:",
                online,
                "",
                lastLine);
        else return List.of(
                players,
                "",
                lastLine);
    }

    private String getOnlineFriends(LobbyManager.Lobby lobby) {
        List<UUID> online = /*FriendManager.getData(player).getOnlineFriends(lobby.getServerName())*/new ArrayList<>();

        if (online.isEmpty()) return "";

        return String.join("&f, ", online.stream().map(PlayerManager::getLegacyRealDisplayName).toList());
    }

    private void connect(LobbyManager.Lobby lobby) {
        String type = LobbyManager.getServerType();
        if (lobby.isSameServer()) {
            player.sendMessage(TextUtility.color("&cYou are already in " + type + " Lobby #" + getLobbyNumber(lobby) + "!"));
            return;
        }

        if (!lobby.isOnline()) {
            player.sendMessage(TextUtility.color("&c" + type + " Lobby #" + getLobbyNumber(lobby) + " is offline."));
            return;
        }

        LobbyManager.connect(player, lobby.getServerName());
        player.sendMessage(TextUtility.color("&eTransferring you to " + LobbyManager.getServerType() + " Lobby #" + getLobbyNumber(lobby) + "!"));
    }
}