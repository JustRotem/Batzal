package net.justrotem.lobby.commands;

import io.papermc.paper.command.brigadier.BasicCommand;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import net.justrotem.lobby.hooks.LuckPermsManager;
import net.justrotem.lobby.utils.TextUtils;
import net.justrotem.lobby.utils.Utility;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;

public class BuildCommand implements BasicCommand {
    @Override
    public void execute(CommandSourceStack source, String[] args) {
        if (Utility.isConsole(source)) return;
        Player player = (Player) source.getSender();

        boolean build = !isBuilding(player);
        updatePlayer(player, getData(player).setBuilding(build));

        player.sendMessage(TextUtils.color(build ? "&aYou can now build!" : "&cYou can no longer build!"));
    }

    private static final HashMap<Player, BuildData> recordedBuildData = new HashMap<>();

    public static BuildData getData(Player player) {
        if (recordedBuildData.containsKey(player)) return recordedBuildData.get(player);

        registerPlayer(player);
        return recordedBuildData.get(player);
    }

    public static void registerPlayer(Player player) {
        if (recordedBuildData.containsKey(player)) return;

        recordedBuildData.put(player, BuildData.create(player));
    }

    public static void updatePlayer(Player player, BuildData buildData){
        if (buildData.isBuilding()) {
            player.setGameMode(GameMode.CREATIVE);
            buildData = buildData.setGameMode(player.getPreviousGameMode());
        } else {
            GameMode gameMode = buildData.getGameMode();
            player.setGameMode(gameMode != null ? gameMode : Bukkit.getDefaultGameMode());
            if (LuckPermsManager.hasPermission(player, "batzal.fly")) {
                player.setAllowFlight(true);
                player.setFlying(true);
            }
        }

        ItemStack[] inventory = player.getInventory().getContents();
        player.getInventory().setContents(buildData.getInventory());
        buildData = buildData.setInventory(inventory);

        recordedBuildData.put(player, buildData);
    }

    public static boolean isBuilding(Player player) {
        return getData(player).isBuilding();
    }

    public static class BuildData {
        public static BuildData create(Player player) {
            return new BuildData(player);
        }

        private final Player player;
        private boolean building;
        private GameMode gameMode;
        private ItemStack[] inventory;

        protected BuildData(Player player) {
            this.player = player;
            this.building = false;
            this.gameMode = player.getGameMode();
            this.inventory = player.getInventory().getContents();
        }

        protected BuildData(Player player, boolean building, GameMode gameMode, ItemStack[] inventory) {
            this.player = player;
            this.building = building;
            this.gameMode = gameMode;
            this.inventory = inventory;
        }

        public Player getPlayer() {
            return this.player;
        }

        public boolean isBuilding() {
            return this.building;
        }

        public GameMode getGameMode() {
            return this.gameMode;
        }

        public ItemStack[] getInventory() {
            return this.inventory;
        }

        public BuildData setBuilding(boolean building) {
            this.building = building;
            return this;
        }

        public BuildData setGameMode(GameMode gameMode) {
            this.gameMode = gameMode;
            return this;
        }

        public BuildData setInventory(ItemStack[] inventory) {
            this.inventory = inventory;
            return this;
        }
    }
}
