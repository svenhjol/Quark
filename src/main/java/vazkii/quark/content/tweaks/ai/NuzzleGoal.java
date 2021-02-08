/**
 * This class was created by <WireSegal>. It's distributed as
 * part of the Quark Mod. Get the Source Code in github:
 * https://github.com/Vazkii/Quark
 * <p>
 * Quark is Open Source and distributed under the
 * CC-BY-NC-SA 3.0 License: https://creativecommons.org/licenses/by-nc-sa/3.0/deed.en_GB
 * <p>
 * File Created @ [May 30, 2019, 20:50 AM (EST)]
 */
package vazkii.quark.content.tweaks.ai;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.goal.Goal;
import net.minecraft.entity.passive.TameableEntity;
import net.minecraft.pathfinding.FlyingPathNavigator;
import net.minecraft.pathfinding.GroundPathNavigator;
import net.minecraft.pathfinding.PathNavigator;
import net.minecraft.pathfinding.PathNodeType;
import net.minecraft.util.SoundEvent;

import java.util.EnumSet;

public class NuzzleGoal extends Goal {

	private final TameableEntity creature;
	private LivingEntity owner;
	private final double followSpeed;
	private final PathNavigator petPathfinder;
	private int timeUntilRebuildPath;
	private final float maxDist;
	private final float whineDist;
	private int whineCooldown;
	private float oldWaterCost;
	private final SoundEvent whine;

	public NuzzleGoal(TameableEntity creature, double followSpeed, float maxDist, float whineDist, SoundEvent whine) {
		this.creature = creature;
		this.followSpeed = followSpeed;
		this.petPathfinder = creature.getNavigator();
		this.maxDist = maxDist;
		this.whineDist = whineDist;
		this.whine = whine;
		this.setMutexFlags(EnumSet.of(Flag.MOVE, Flag.LOOK, Flag.TARGET));

		if (!(creature.getNavigator() instanceof GroundPathNavigator) && !(creature.getNavigator() instanceof FlyingPathNavigator))
			throw new IllegalArgumentException("Unsupported mob type for FollowOwnerGoal");
	}

	@Override
	public boolean shouldExecute() {
		if (!WantLoveGoal.needsPets(creature))
			return false;

		LivingEntity living = this.creature.getOwner();

		if (living == null || living.isSpectator() ||
				this.creature.isSitting())
			return false;
		else {
			this.owner = living;
			return true;
		}
	}

	@Override
	public boolean shouldContinueExecuting() {
		if (!WantLoveGoal.needsPets(creature))
			return false;
		return !this.petPathfinder.noPath() && this.creature.getDistanceSq(this.owner) > (this.maxDist * this.maxDist) && !this.creature.isSitting();
	}

	@Override
	public void startExecuting() {
		this.timeUntilRebuildPath = 0;
		this.whineCooldown = 10;
		this.oldWaterCost = this.creature.getPathPriority(PathNodeType.WATER);
		this.creature.setPathPriority(PathNodeType.WATER, 0.0F);
	}

	@Override
	public void resetTask() {
		this.owner = null;
		this.petPathfinder.clearPath();
		this.creature.setPathPriority(PathNodeType.WATER, this.oldWaterCost);
	}

	@Override
	public void tick() {
		this.creature.getLookController().setLookPositionWithEntity(this.owner, 10.0F, this.creature.getVerticalFaceSpeed());

		if (!this.creature.isSitting()) {
			if (--this.timeUntilRebuildPath <= 0) {
				this.timeUntilRebuildPath = 10;

				this.petPathfinder.tryMoveToEntityLiving(this.owner, this.followSpeed);
			}
		}

		if (creature.getDistanceSq(owner) < whineDist) {
			if (--this.whineCooldown <= 0) {
				this.whineCooldown = 80 + creature.getRNG().nextInt(40);
				creature.playSound(whine, 1F, 0.5F + (float) Math.random() * 0.5F);
			}
		}
	}
}
