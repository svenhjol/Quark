/**
 * This class was created by <WireSegal>. It's distributed as
 * part of the Quark Mod. Get the Source Code in github:
 * https://github.com/Vazkii/Quark
 * <p>
 * Quark is Open Source and distributed under the
 * CC-BY-NC-SA 3.0 License: https://creativecommons.org/licenses/by-nc-sa/3.0/deed.en_GB
 * <p>
 * File Created @ [May 27, 2019, 13:45 AM (EST)]
 */
package vazkii.quark.content.mobs.ai;

import java.util.EnumSet;
import java.util.function.Predicate;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.entity.CreatureEntity;
import net.minecraft.entity.ai.attributes.Attributes;
import net.minecraft.entity.ai.goal.Goal;
import net.minecraft.tags.Tag;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.vector.Vector3d;

public class FavorBlockGoal extends Goal {

	private final CreatureEntity creature;
	private final double movementSpeed;
	private final Predicate<BlockState> targetBlock;

	protected int runDelay;
	private int timeoutCounter;
	private int maxStayTicks;

	protected BlockPos destinationBlock = BlockPos.ZERO;

	public FavorBlockGoal(CreatureEntity creature, double speed, Predicate<BlockState> predicate) {
		this.creature = creature;
		this.movementSpeed = speed;
		this.targetBlock = predicate;
		setMutexFlags(EnumSet.of(Flag.MOVE, Flag.JUMP));
	}

	public FavorBlockGoal(CreatureEntity creature, double speed, Tag<Block> tag) {
		this(creature, speed, (state) -> tag.contains(state.getBlock()));
	}

	public FavorBlockGoal(CreatureEntity creature, double speed, Block block) {
		this(creature, speed, (state) -> state.getBlock() == block);
	}

	@Override
	public boolean shouldExecute() {
		if (runDelay > 0) {
			--runDelay;
			return false;
		} else {
			runDelay = 200 + creature.getRNG().nextInt(200);
			return searchForDestination();
		}
	}

	@Override
	public boolean shouldContinueExecuting() {
		return timeoutCounter >= -maxStayTicks && timeoutCounter <= 1200 && targetBlock.test(creature.world.getBlockState(destinationBlock));
	}

	@Override
	public void startExecuting() {
		creature.getNavigator().tryMoveToXYZ(destinationBlock.getX() + 0.5, destinationBlock.getY() + 1, destinationBlock.getZ() + 0.5, movementSpeed);
		timeoutCounter = 0;
		maxStayTicks = creature.getRNG().nextInt(creature.getRNG().nextInt(1200) + 1200) + 1200;
	}


	@Override
	public void tick() {
		if (creature.getDistanceSq(new Vector3d(destinationBlock.getX(), destinationBlock.getY(), destinationBlock.getZ()).add(0.5, 1.5, 0.5)) > 1.0D) {
			++timeoutCounter;

			if (timeoutCounter % 40 == 0)
				creature.getNavigator().tryMoveToXYZ(destinationBlock.getX() + 0.5D, destinationBlock.getY() + 1, destinationBlock.getZ() + 0.5D, movementSpeed);
		} else {
			--timeoutCounter;
		}
	}

	private boolean searchForDestination() {
		double followRange = creature.getAttribute(Attributes.FOLLOW_RANGE).getValue();
		Vector3d cpos = creature.getPositionVec();
		double xBase = cpos.x;
		double yBase = cpos.y;
		double zBase = cpos.z;

		BlockPos.Mutable pos = new BlockPos.Mutable();

		for (int yShift = 0;
			 yShift <= 1;
			 yShift = yShift > 0 ? -yShift : 1 - yShift) {

			for (int seekDist = 0; seekDist < followRange; ++seekDist) {
				for (int xShift = 0;
					 xShift <= seekDist;
					 xShift = xShift > 0 ? -xShift : 1 - xShift) {

					for (int zShift = xShift < seekDist && xShift > -seekDist ? seekDist : 0;
						 zShift <= seekDist;
						 zShift = zShift > 0 ? -zShift : 1 - zShift) {

						pos.setPos(xBase + xShift, yBase + yShift - 1, zBase + zShift);

						if (creature.isWithinHomeDistanceFromPosition(pos) &&
								targetBlock.test(creature.world.getBlockState(pos))) {
							destinationBlock = pos;
							return true;
						}
					}
				}
			}
		}

		return false;
	}
}
