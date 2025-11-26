package net.justrotem.lobby.ride.listener;

import net.justrotem.lobby.ride.DragonFactory;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.entity.AreaEffectCloud;
import org.bukkit.entity.EnderDragon;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.event.entity.EntityDismountEvent;
import org.bukkit.event.player.PlayerKickEvent;

import java.util.Arrays;

public class DragonListener implements Listener {

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
	public void onSwoop(DragonSwoopEvent event){
		if (!(event.getTarget() instanceof Player target)) return;

        if (shouldCancelAttack(event.getEntity(), target)) event.setCancelled(true);
	}
	
	@EventHandler(priority=EventPriority.HIGHEST, ignoreCancelled = true)
	public void entityDamage(EntityDamageByEntityEvent e){
		Entity damager = e.getDamager();
		if (damager instanceof AreaEffectCloud cloud){
            if (cloud.getSource() instanceof Entity) {
				damager = (Entity) cloud.getSource();
			}
		}

		if (!DragonFactory.isPetDragon(damager)) return;
		if (!(e.getEntity() instanceof Player player)) return;

        if (shouldCancelAttack((EnderDragon) damager, player)) e.setCancelled(true);
	}

	private boolean shouldCancelAttack(EnderDragon dragon, Player player){
		return player.getUniqueId().equals(DragonFactory.getOwner(dragon)) ||
				dragon.getPassengers().contains(player);
	}
	
	//stop kick for flying
	@EventHandler(priority=EventPriority.LOWEST)
	public void kick(PlayerKickEvent e) {
		if (!PlainTextComponentSerializer.plainText().serialize(e.reason()).toLowerCase().contains("flying")) return;
		if (e.getPlayer().getNoDamageTicks() > 10) e.setCancelled(true);
		if (DragonFactory.isPetDragon(e.getPlayer().getVehicle())) e.setCancelled(true);
	}
	
	@EventHandler
	public void dragonDismount(EntityDismountEvent e){
		if (!DragonFactory.isPetDragon(e.getDismounted())) return;
		if (!(e.getEntity() instanceof Player player)) return;
        //prevent fall damage
		player.setNoDamageTicks(150);

		e.getDismounted().remove();
	}
	
	@EventHandler
	public void riderDamage(EntityDamageEvent e){
		if (!DragonFactory.isPetDragon(e.getEntity().getVehicle())) return;
		if (Arrays.asList(DamageCause.FLY_INTO_WALL, DamageCause.SUFFOCATION, DamageCause.ENTITY_EXPLOSION, DamageCause.ENTITY_ATTACK, DamageCause.FALL)
				.contains(e.getCause())) e.setCancelled(true);
	}
}
