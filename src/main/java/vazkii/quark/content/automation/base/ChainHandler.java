/**
 * This class was created by <WireSegal>. It's distributed as
 * part of the Quark Mod. Get the Source Code in github:
 * https://github.com/Vazkii/Quark
 * <p>
 * Quark is Open Source and distributed under the
 * CC-BY-NC-SA 3.0 License: https://creativecommons.org/licenses/by-nc-sa/3.0/deed.en_GB
 * <p>
 * File Created @ [Jul 17, 2019, 20:09 AM (EST)]
 */
package vazkii.quark.content.automation.base;

import java.util.UUID;

import net.minecraft.entity.Entity;
import net.minecraft.entity.item.BoatEntity;
import net.minecraft.entity.item.minecart.AbstractMinecartEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.GameRules;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.fml.network.NetworkDirection;
import vazkii.quark.base.network.QuarkNetwork;
import vazkii.quark.base.network.message.SyncChainMessage;
import vazkii.quark.content.automation.module.ChainLinkageModule;

public class ChainHandler {
	public static final String LINKED_TO = "Quark:VehicleLink";
	public static final double DRAG = 0.95;
	public static final float CHAIN_SLACK = 2F;
	public static final float MAX_DISTANCE = 8F;
	private static final float STIFFNESS = 0.4F;
//	private static final float DAMPING = 0F;
//	private static final float MIN_FORCE = 0F;
	private static final float MAX_FORCE = 6F;

	private static <T extends Entity> void adjustVelocity(T master, Entity follower) {
		if (master == follower || master.world.isRemote)
			return;

		double dist = master.getDistance(follower);

		Vector3d masterPosition = master.getPositionVec();
		Vector3d followerPosition = follower.getPositionVec();

		Vector3d masterMotion = master.getMotion();
		Vector3d followerMotion = follower.getMotion();

		Vector3d direction = followerPosition.subtract(masterPosition);
		direction = direction.subtract(0, direction.y, 0).normalize();

		double base = masterMotion.length() + followerMotion.length();
		if (base != 0) {
			double masterRatio = 1 + masterMotion.length() / base;
			double followerRatio = 1 + followerMotion.length() / base;

			double stretch = dist - CHAIN_SLACK;

			double springX = STIFFNESS * stretch * direction.x;
			double springZ = STIFFNESS * stretch * direction.z;

			springX = MathHelper.clamp(springX, -MAX_FORCE, MAX_FORCE);
			springZ = MathHelper.clamp(springZ, -MAX_FORCE, MAX_FORCE);

//			double totalSpringSq = springX * springX + springZ * springZ;
			masterMotion = masterMotion.add(springX * followerRatio, 0, springZ * followerRatio);
			followerMotion = followerMotion.subtract(springX * masterRatio, 0, springZ * masterRatio);


//			if (totalSpringSq > MIN_FORCE * MIN_FORCE) {
//				Vector3d newMasterVelocity = new Vector3d(masterMotion.x, 0, masterMotion.z);
//				Vector3d newFollowerVelocity = new Vector3d(followerMotion.x, 0, followerMotion.z);
//
//				double deviation = newFollowerVelocity.subtract(newMasterVelocity).dotProduct(direction);
//
//				double dampX = DAMPING * deviation * direction.x;
//				double dampZ = DAMPING * deviation * direction.z;
//
//				dampX = MathHelper.clamp(dampX, -MAX_FORCE, MAX_FORCE);
//				dampZ = MathHelper.clamp(dampZ, -MAX_FORCE, MAX_FORCE);
//
//				masterMotion = masterMotion.add(dampX, 0, dampZ);
//				followerMotion = followerMotion.subtract(dampX, 0, dampZ);
//

			master.setMotion(masterMotion);
			follower.setMotion(followerMotion);
//			}
		}
	}

	public static UUID getLink(Entity vehicle) {
		if (!canBeLinked(vehicle))
			return null;
		if (!vehicle.getPersistentData().hasUniqueId(LINKED_TO))
			return null;

		return vehicle.getPersistentData().getUniqueId(LINKED_TO);
	}

	public static boolean canBeLinkedTo(Entity entity) {
		return entity instanceof BoatEntity || entity instanceof AbstractMinecartEntity || entity instanceof PlayerEntity;
	}

	public static boolean canBeLinked(Entity entity) {
		return entity instanceof BoatEntity || entity instanceof AbstractMinecartEntity;
	}

	public static <T extends Entity> Entity getLinked(T vehicle) {
		UUID uuid = getLink(vehicle);
		if (uuid == null || uuid.equals(SyncChainMessage.NULL_UUID))
			return null;

		for (Entity entity : vehicle.world.getEntitiesWithinAABB(Entity.class,
				vehicle.getBoundingBox().grow(MAX_DISTANCE), ChainHandler::canBeLinkedTo)) {

			if (entity.getUniqueID().equals(uuid)) {
				return entity;
			}
		}

		return null;
	}

	public static <T extends Entity> void adjustVehicle(T cart) {
		Entity other = getLinked(cart);

		if (other == null) {
			if (getLink(cart) != null)
				breakChain(cart);
			return;
		}

		if(!(other instanceof PlayerEntity))
			adjustVelocity(other, cart);

		cart.setMotion(cart.getMotion().mul(DRAG, 1, DRAG));
	}

	private static <T extends Entity> void breakChain(T cart) {
		setLink(cart, null, true);

		if (!cart.world.isRemote && cart.world.getGameRules().getBoolean(GameRules.DO_ENTITY_DROPS))
			cart.entityDropItem(new ItemStack(Items.CHAIN), 0f);
	}

	public static void setLink(Entity entity, UUID uuid, boolean sync) {
		if (canBeLinked(entity)) {
			if (entity.getUniqueID().equals(uuid))
				return;

			if (uuid != null && !uuid.equals(SyncChainMessage.NULL_UUID))
				entity.getPersistentData().putUniqueId(LINKED_TO, uuid);
			else {
				entity.getPersistentData().remove(LINKED_TO);
			}

			if (sync) {
				if (entity.world instanceof ServerWorld) {
					ServerWorld world = (ServerWorld) entity.world;
					world.getChunkProvider().sendToAllTracking(entity,
							QuarkNetwork.toVanillaPacket(new SyncChainMessage(entity.getEntityId(), uuid),
									NetworkDirection.PLAY_TO_CLIENT));
				}
			}
		}
	}
}
