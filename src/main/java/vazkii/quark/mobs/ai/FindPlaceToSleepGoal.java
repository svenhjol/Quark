/**
 * This class was created by <WireSegal>. It's distributed as
 * part of the Quark Mod. Get the Source Code in github:
 * https://github.com/Vazkii/Quark
 * <p>
 * Quark is Open Source and distributed under the
 * CC-BY-NC-SA 3.0 License: https://creativecommons.org/licenses/by-nc-sa/3.0/deed.en_GB
 * <p>
 * File Created @ [Jul 13, 2019, 12:17 AM (EST)]
 */
package vazkii.quark.mobs.ai;

import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.FurnaceBlockEntity;
import net.minecraft.entity.ai.goal.MoveToTargetPosGoal;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.WorldView;
import vazkii.quark.mobs.entity.FoxhoundEntity;

import javax.annotation.Nonnull;

public class FindPlaceToSleepGoal extends MoveToTargetPosGoal {
	private final FoxhoundEntity foxhound;

	private final boolean furnaceOnly;

	private boolean hadSlept = false;

	public FindPlaceToSleepGoal(FoxhoundEntity foxhound, double speed, boolean furnaceOnly) {
		super(foxhound, speed, 8);
		this.foxhound = foxhound;
		this.furnaceOnly = furnaceOnly;
	}

	@Override
	public boolean canStart() {
		return this.foxhound.isTamed() && !this.foxhound.isInSittingPose() && super.canStart();
	}

	@Override
	public boolean shouldContinue() {
		return (!hadSlept || this.foxhound.isSleeping()) && super.shouldContinue();
	}

	@Override
	public void start() {
		super.start();
		hadSlept = false;
		this.foxhound.setInSittingPose(false);
		this.foxhound.getSleepGoal().setSleeping(false);
		this.foxhound.setSleeping(false);
	}

	@Override
	public void stop() {
		super.stop();
		hadSlept = false;
		this.foxhound.setInSittingPose(false);
		this.foxhound.getSleepGoal().setSleeping(false);
		this.foxhound.setSleeping(false);
	}

	@Override
	public void tick() {
		super.tick();

		Vec3d motion = foxhound.getVelocity();

		if (!this.hasReached() || motion.x > 0 || motion.z > 0) {
			this.foxhound.setInSittingPose(false);
			this.foxhound.getSleepGoal().setSleeping(false);
			this.foxhound.setSleeping(false);
		} else if (!this.foxhound.isInSittingPose()) {
			this.foxhound.setInSittingPose(true);
			this.foxhound.getSleepGoal().setSleeping(true);
			this.foxhound.setSleeping(true);
			hadSlept = true;
		}
	}

	@Override
	protected boolean isTargetPos(@Nonnull WorldView world, @Nonnull BlockPos pos) {
		if (!world.isAir(pos.up())) {
			return false;
		} else {
			BlockState state = world.getBlockState(pos);
			BlockEntity tileentity = world.getBlockEntity(pos);

			if(furnaceOnly)
				return tileentity instanceof FurnaceBlockEntity;

			return state.getLightValue(world, pos) > 2;
		}
	}
}
