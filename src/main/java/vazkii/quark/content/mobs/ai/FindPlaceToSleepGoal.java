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
import net.minecraft.entity.ai.goal.MoveToBlockGoal;
import net.minecraft.tileentity.FurnaceTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.IWorldReader;
import vazkii.quark.content.mobs.entity.FoxhoundEntity;

import javax.annotation.Nonnull;

public class FindPlaceToSleepGoal extends MoveToBlockGoal {
	private final FoxhoundEntity foxhound;

	private final boolean furnaceOnly;

	private boolean hadSlept = false;

	public FindPlaceToSleepGoal(FoxhoundEntity foxhound, double speed, boolean furnaceOnly) {
		super(foxhound, speed, 8);
		this.foxhound = foxhound;
		this.furnaceOnly = furnaceOnly;
	}

	@Override
	public boolean shouldExecute() {
		return this.foxhound.isTamed() && !this.foxhound.isSitting() && super.shouldExecute();
	}

	@Override
	public boolean shouldContinueExecuting() {
		return (!hadSlept || this.foxhound.isSleeping()) && super.shouldContinueExecuting();
	}

	@Override
	public void startExecuting() {
		super.startExecuting();
		hadSlept = false;
		this.foxhound.func_233687_w_(false); // setSitting
		this.foxhound.getSleepGoal().setSleeping(false);
		this.foxhound.setSleeping(false);
	}

	@Override
	public void resetTask() {
		super.resetTask();
		hadSlept = false;
		this.foxhound.func_233687_w_(false); // setSitting
		this.foxhound.getSleepGoal().setSleeping(false);
		this.foxhound.setSleeping(false);
	}

	@Override
	public void tick() {
		super.tick();

		Vector3d motion = foxhound.getMotion();

		if (!this.getIsAboveDestination() || motion.x > 0 || motion.z > 0) {
			this.foxhound.func_233687_w_(false); // setSitting
			this.foxhound.getSleepGoal().setSleeping(false);
			this.foxhound.setSleeping(false);
		} else if (!this.foxhound.isSitting()) {
			this.foxhound.func_233687_w_(true); // setSitting
			this.foxhound.getSleepGoal().setSleeping(true);
			this.foxhound.setSleeping(true);
			hadSlept = true;
		}
	}

	@Override
	protected boolean shouldMoveTo(@Nonnull IWorldReader world, @Nonnull BlockPos pos) {
		if (!world.isAirBlock(pos.up())) {
			return false;
		} else {
			BlockState state = world.getBlockState(pos);
			TileEntity tileentity = world.getTileEntity(pos);

			if(furnaceOnly)
				return tileentity instanceof FurnaceTileEntity;

			return state.getLightValue(world, pos) > 2;
		}
	}
}
