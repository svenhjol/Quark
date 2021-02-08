/**
 * This class was created by <WireSegal>. It's distributed as
 * part of the Quark Mod. Get the Source Code in github:
 * https://github.com/Vazkii/Quark
 * <p>
 * Quark is Open Source and distributed under the
 * CC-BY-NC-SA 3.0 License: https://creativecommons.org/licenses/by-nc-sa/3.0/deed.en_GB
 * <p>
 * File Created @ [May 11, 2019, 17:44 AM (EST)]
 */
package vazkii.quark.content.mobs.ai;

import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;
import java.util.function.BooleanSupplier;

import net.minecraft.entity.Entity;
import net.minecraft.entity.ai.goal.WanderAroundFarGoal;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.Vec3d;
import vazkii.quark.base.util.MutableVectorHolder;
import vazkii.quark.content.mobs.entity.StonelingEntity;

public class ActWaryGoal extends WanderAroundFarGoal {

	private final StonelingEntity stoneling;

	private final BooleanSupplier scaredBySuddenMovement;

	private final double range;

	private boolean startled;

	private final Map<PlayerEntity, MutableVectorHolder> lastPositions = new WeakHashMap<>();
	private final Map<PlayerEntity, MutableVectorHolder> lastSpeeds = new WeakHashMap<>();

	public ActWaryGoal(StonelingEntity stoneling, double speed, double range, BooleanSupplier scaredBySuddenMovement) {
		super(stoneling, speed, 1F);
		this.stoneling = stoneling;
		this.range = range;
		this.scaredBySuddenMovement = scaredBySuddenMovement;
	}

	private static void updateMotion(MutableVectorHolder holder, double x, double y, double z) {
		holder.x = x;
		holder.y = y;
		holder.z = z;
	}

	private static void updatePos(MutableVectorHolder holder, Entity entity) {
		Vec3d pos = entity.getPos();
		holder.x = pos.x;
		holder.y = pos.y;
		holder.z = pos.z;
	}

	private static MutableVectorHolder initPos(PlayerEntity p) {
		MutableVectorHolder holder = new MutableVectorHolder();
		updatePos(holder, p);
		return holder;
	}

	public void startle() {
		startled = true;
	}

	public boolean isStartled() {
		return startled;
	}

	protected boolean shouldApplyPath() {
		return super.canStart();
	}

	@Override
	public void tick() {
		if (stoneling.getNavigation().isIdle() && shouldApplyPath())
			start();
	}

	@Override
	public boolean shouldContinue() {
		return canStart();
	}

	@Override
	public void stop() {
		stoneling.getNavigation().stop();
	}

	@Override
	public boolean canStart() {
		if (startled || stoneling.isPlayerMade())
			return false;

		List<PlayerEntity> playersAround = stoneling.world.getEntitiesByClass(PlayerEntity.class, stoneling.getBoundingBox().expand(range),
				(player) -> player != null && !player.abilities.creativeMode && player.squaredDistanceTo(stoneling) < range * range);

		if (playersAround.isEmpty())
			return false;

		for (PlayerEntity player : playersAround) {
			if (player.isSneaky()) {
				if (scaredBySuddenMovement.getAsBoolean()) {
					MutableVectorHolder lastSpeed = lastSpeeds.computeIfAbsent(player, p -> new MutableVectorHolder());
					MutableVectorHolder lastPos = lastPositions.computeIfAbsent(player, ActWaryGoal::initPos);
					Vec3d pos = player.getPos();

					double dX = pos.x - lastPos.x;
					double dY = pos.y - lastPos.y;
					double dZ = pos.z - lastPos.z;

					double xDisplacement = dX - lastSpeed.x;
					double yDisplacement = dY - lastSpeed.y;
					double zDisplacement = dZ - lastSpeed.z;

					updateMotion(lastSpeed, dX, dY, dZ);
					updatePos(lastPos, player);

					double displacementSq = xDisplacement * xDisplacement +
							yDisplacement * yDisplacement +
							zDisplacement * zDisplacement;

					if (displacementSq < 0.01)
						return true;

					startled = true;
					return false;
				}
			} else {
				startled = true;
				return false;
			}
		}

		return true;
	}
}
