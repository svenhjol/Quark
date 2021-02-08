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
package vazkii.quark.content.mobs.ai;

import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.FurnaceBlockEntity;
import net.minecraft.entity.ai.goal.MoveToTargetPosGoal;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.WorldView;
import vazkii.quark.content.mobs.entity.FoxhoundEntity;

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
		return this.foxhound.isTamed() && !this.foxhound.isSitting() && super.canStart();
	}

	@Override
	public boolean shouldContinue() {
		return (!hadSlept || this.foxhound.isSleeping()) && super.shouldContinue();
	}

	@Override
	public void start() {
		super.start();
		hadSlept = false;
		this.foxhound.setSitting(false); // setSitting
		this.foxhound.getSleepGoal().setSleeping(false);
		this.foxhound.setInSittingPose(false);
	}

	@Override
	public void stop() {
		super.stop();
		hadSlept = false;
		this.foxhound.setSitting(false); // setSitting
		this.foxhound.getSleepGoal().setSleeping(false);
		this.foxhound.setInSittingPose(false);
	}

	@Override
	public void tick() {
		super.tick();

		Vec3d motion = foxhound.getVelocity();

		if (!this.hasReached() || motion.x > 0 || motion.z > 0) {
			this.foxhound.setSitting(false); // setSitting
			this.foxhound.getSleepGoal().setSleeping(false);
			this.foxhound.setInSittingPose(false);
		} else if (!this.foxhound.isSitting()) {
			this.foxhound.setSitting(true); // setSitting
			this.foxhound.getSleepGoal().setSleeping(true);
			this.foxhound.setInSittingPose(true);
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
