package net.justrotem.lobby.commands;

import io.papermc.paper.command.brigadier.BasicCommand;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import net.justrotem.lobby.utils.PlayerUtility;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;

public class BuildCommand implements BasicCommand {

    @Override
    public void execute(CommandSourceStack source, String[] args) {
        if (PlayerUtility.isConsole(source)) return;
        Player player = (Player) source.getSender();

        PlayerUtility.runTarget(player, args, 1, permission() + ".others", target -> {
            boolean build = !isBuilding(target);
            updatePlayer(target, getData(target).setBuilding(build));
            return build ? "now" : "no longer";
        }, "&aYou can %value% build%staff%!", "%target% &acan %value% build!");
    }

    @Override
    public @NotNull Collection<String> suggest(@NotNull CommandSourceStack source, String[] args) {
        List<String> arguments = new ArrayList<>();

        PlayerUtility.addPlayerCompletion(args, 1, arguments, source, permission() + ".others");

        return arguments;
    }

    @Override
    public @Nullable String permission() {
        return "batzal.build";
    }

    //<editor-fold desc="Data methods">
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
            FlyCommand.flyByPermission(player);
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
    //</editor-fold>
}
