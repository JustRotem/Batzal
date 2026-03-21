package net.justrotem.lobby.ride.listener;

import com.destroystokyo.paper.event.entity.EntityAddToWorldEvent;
import net.justrotem.lobby.ride.DragonFactory;
import org.bukkit.entity.EnderDragon;
import org.bukkit.entity.Entity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.world.ChunkLoadEvent;
import org.bukkit.event.world.EntitiesLoadEvent;
import org.bukkit.event.world.WorldLoadEvent;
import org.bukkit.plugin.java.JavaPlugin;

public class EntitiesLoadListener {

    public static void initialize(JavaPlugin plugin) {
        plugin.getServer().getPluginManager().registerEvents(new DragonListener(), plugin);

        try {
            Class.forName("com.destroystokyo.paper.event.entity.EntityAddToWorldEvent");
            plugin.getServer().getPluginManager().registerEvents(new Listener() {
                @EventHandler
                private void onEntitiesLoad(EntityAddToWorldEvent event) {
                    DragonFactory.handleOldDragon(event.getEntity());
                }
            }, plugin);
        } catch (ClassNotFoundException e1) {
            try {
                Class.forName("org.bukkit.event.world.EntitiesLoadEvent");
                plugin.getServer().getPluginManager().registerEvents(new Listener() {
                    @EventHandler
                    private void onEntitiesLoad(EntitiesLoadEvent event) {
                        event.getEntities().forEach(DragonFactory::handleOldDragon);
                    }
                }, plugin);
            } catch (ClassNotFoundException e2) {
                plugin.getServer().getPluginManager().registerEvents(new Listener() {
                    @EventHandler
                    private void onChunkLoad(ChunkLoadEvent event) {
                        for (Entity ent: event.getChunk().getEntities()){
                            DragonFactory.handleOldDragon(ent);
                        }
                    }
                }, plugin);
            } finally {
                plugin.getServer().getPluginManager().registerEvents(new Listener() {
                    @EventHandler
                    private void onChunkLoad(WorldLoadEvent event) {
                        for (Entity ent: event.getWorld().getEntitiesByClass(EnderDragon.class)){
                            DragonFactory.handleOldDragon(ent);
                        }
                    }
                }, plugin);
            }

        }
    }
}
