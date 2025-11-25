package net.justrotem.lobby.ride;

import net.justrotem.lobby.Main;
import net.justrotem.lobby.ride.nms.PetEnderDragon;
import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.OfflinePlayer;
import org.bukkit.World;
import org.bukkit.entity.EnderDragon;
import org.bukkit.entity.Entity;
import org.bukkit.entity.HumanEntity;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;

import javax.annotation.Nullable;
import java.lang.reflect.InvocationTargetException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

public class DragonFactory {
	
	private static final Main plugin = Main.getInstance();
	private static final NamespacedKey ownerKey = new NamespacedKey(plugin, PetEnderDragon.OWNER_ID);
	private static final NamespacedKey dragonIdKey = new NamespacedKey(plugin, PetEnderDragon.DRAGON_ID);
	private static boolean canDamage = false;

	public static PetEnderDragon create(World world, UUID owner) {
		try {
			PetEnderDragon dragon = new PetEnderDragon(world);

			if (!dragon.getEntity().getPersistentDataContainer().has(dragonIdKey, PersistentDataType.STRING)) {
				dragon.getEntity().getPersistentDataContainer().set(dragonIdKey, PersistentDataType.STRING, dragon.getEntity().getUniqueId().toString());
			}

			if (owner != null){
				dragon.getEntity().getPersistentDataContainer().set(ownerKey, PersistentDataType.STRING, owner.toString());
			}
			return dragon;
		} catch (Exception e){
			e.printStackTrace();
		}
		return null;
	}
	
	public static boolean isPetDragon(Entity ent) {
		if (!(ent instanceof EnderDragon)) return false;
		return ent.getScoreboardTags().contains(PetEnderDragon.DRAGON_ID);
	}
	
	public static boolean canDamage(PetEnderDragon dragon) {
		UUID owner = getOwner(dragon.getEntity());
        return owner == null || canDamage;
    }

	public static void setCanDamage(boolean canDamage) {
		DragonFactory.canDamage = canDamage;
	}
	
	public static boolean tryRide(HumanEntity p, EnderDragon dragon) {
		if (!isPetDragon(dragon)) return false;

		ItemStack handHeld = p.getInventory().getItemInMainHand();
		if (!handHeld.getType().isAir()) return false;

		dragon.addPassenger(p);
		return true;
	}

	/**
	 * Manually reset dragons spawned before 1.6 since their entity type is still wrong
	 * @param ent the dragon to check
	 */
	public static void handleOldDragon(Entity ent) {
		if (!isPetDragon(ent)) return;
		EnderDragon dragon = (EnderDragon) ent;
		try {
			if (dragon.getClass().getDeclaredMethod("getHandle").invoke(dragon) instanceof PetEnderDragon) return;
		} catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException ignore) {
		}
		resetDragon(dragon);
	}

	public static void resetDragon(EnderDragon dragon) {
		if (!isPetDragon(dragon)) return;

		List<Entity> passengers = dragon.getPassengers();
		dragon.remove();

		PetEnderDragon petDragon = create(dragon.getWorld(), null);
		petDragon.copyFrom(dragon);
		petDragon.spawn(dragon.getLocation().toVector());

		passengers.forEach(p -> petDragon.getEntity().addPassenger(p));
	}

	public static Set<EnderDragon> getDragons(OfflinePlayer player) {
		Set<EnderDragon> result = new HashSet<>();
		for (World world : Bukkit.getWorlds()){
			for (EnderDragon dragon: world.getEntitiesByClass(EnderDragon.class)){
				if (!isPetDragon(dragon)) continue;
				if (!player.getUniqueId().equals(getOwner(dragon))) continue;

				result.add(dragon);
			}
		}
		return result;
	}

	public static @Nullable UUID getOwner(EnderDragon dragon){
		if (!dragon.getPersistentDataContainer().has(ownerKey, PersistentDataType.STRING)) return null;
		String uuidText = dragon.getPersistentDataContainer().get(ownerKey, PersistentDataType.STRING);
		if (uuidText == null || uuidText.isEmpty()) return null;
		return UUID.fromString(uuidText);
	}
}
