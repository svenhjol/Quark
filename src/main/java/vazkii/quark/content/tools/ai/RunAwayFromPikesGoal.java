package vazkii.quark.content.tools.ai;

import java.util.EnumSet;
import java.util.List;

import javax.annotation.Nullable;

import net.minecraft.entity.CreatureEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.RandomPositionGenerator;
import net.minecraft.entity.ai.goal.Goal;
import net.minecraft.pathfinding.Path;
import net.minecraft.pathfinding.PathNavigator;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.World;
import vazkii.quark.content.tools.entity.SkullPikeEntity;

// Mostly a copy of AvoidEntityGoal cleaned up to work with pikes
public class RunAwayFromPikesGoal extends Goal {

	protected final CreatureEntity entity;
	private final double farSpeed;
	private final double nearSpeed;
	protected SkullPikeEntity avoidTarget;
	protected final float avoidDistance;
	protected Path path;
	protected final PathNavigator navigation;

	public RunAwayFromPikesGoal(CreatureEntity entityIn, float distance, double nearSpeedIn, double farSpeedIn) {
		entity = entityIn;
		avoidDistance = distance;
		farSpeed = nearSpeedIn;
		nearSpeed = farSpeedIn;
		navigation = entityIn.getNavigator();
		setMutexFlags(EnumSet.of(Goal.Flag.MOVE));
	}

	@Override
	public boolean shouldExecute() {
		avoidTarget = getClosestEntity(entity.world, entity, entity.getPosX(), entity.getPosY(), entity.getPosZ(), entity.getBoundingBox().grow(avoidDistance, 3.0D, avoidDistance));
		if(avoidTarget == null)
			return false;
		
		Vector3d posToMove = RandomPositionGenerator.findRandomTargetBlockAwayFrom(entity, 16, 7, avoidTarget.getPositionVec());
		if(posToMove == null)
			return false;
		
		if(avoidTarget.getDistanceSq(posToMove.x, posToMove.y, posToMove.z) < avoidTarget.getDistanceSq(entity))
			return false;
			
			
		path = navigation.getPathToPos(posToMove.x, posToMove.y, posToMove.z, 0);
		return path != null;
	}

	@Nullable
	private SkullPikeEntity getClosestEntity(World world, LivingEntity p_225318_3_, double p_225318_4_, double p_225318_6_, double p_225318_8_, AxisAlignedBB p_225318_10_) {
		return getClosestEntity(world.getLoadedEntitiesWithinAABB(SkullPikeEntity.class, p_225318_10_, null), p_225318_3_, p_225318_4_, p_225318_6_, p_225318_8_);
	}

	@Nullable
	private SkullPikeEntity getClosestEntity(List<SkullPikeEntity> entities, LivingEntity target, double x, double y, double z) {
		double d0 = -1.0D;
		SkullPikeEntity t = null;

		for(SkullPikeEntity t1 : entities) {
			if(!t1.isVisible(target))
				continue;
			
			double d1 = t1.getDistanceSq(x, y, z);
			if (d0 == -1.0D || d1 < d0) {
				d0 = d1;
				t = t1;
			}
		}

		return t;
	}

	@Override
	public boolean shouldContinueExecuting() {
		return !this.navigation.noPath();
	}

	@Override
	public void startExecuting() {
		this.navigation.setPath(this.path, this.farSpeed);
	}

	@Override
	public void resetTask() {
		this.avoidTarget = null;
	}

	@Override
	public void tick() {
		if (this.entity.getDistanceSq(this.avoidTarget) < 49.0D) {
			this.entity.getNavigator().setSpeed(this.nearSpeed);
		} else {
			this.entity.getNavigator().setSpeed(this.farSpeed);
		}

	}
}