package vazkii.quark.content.tools.ai;

import java.util.EnumSet;
import java.util.List;

import javax.annotation.Nullable;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.TargetFinder;
import net.minecraft.entity.ai.goal.Goal;
import net.minecraft.entity.ai.pathing.EntityNavigation;
import net.minecraft.entity.ai.pathing.Path;
import net.minecraft.entity.mob.PathAwareEntity;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import vazkii.quark.content.tools.entity.SkullPikeEntity;

// Mostly a copy of AvoidEntityGoal cleaned up to work with pikes
public class RunAwayFromPikesGoal extends Goal {

	protected final PathAwareEntity entity;
	private final double farSpeed;
	private final double nearSpeed;
	protected SkullPikeEntity avoidTarget;
	protected final float avoidDistance;
	protected Path path;
	protected final EntityNavigation navigation;

	public RunAwayFromPikesGoal(PathAwareEntity entityIn, float distance, double nearSpeedIn, double farSpeedIn) {
		entity = entityIn;
		avoidDistance = distance;
		farSpeed = nearSpeedIn;
		nearSpeed = farSpeedIn;
		navigation = entityIn.getNavigation();
		setControls(EnumSet.of(Goal.Control.MOVE));
	}

	@Override
	public boolean canStart() {
		avoidTarget = getClosestEntity(entity.world, entity, entity.getX(), entity.getY(), entity.getZ(), entity.getBoundingBox().expand(avoidDistance, 3.0D, avoidDistance));
		if(avoidTarget == null)
			return false;
		
		Vec3d posToMove = TargetFinder.findTargetAwayFrom(entity, 16, 7, avoidTarget.getPos());
		if(posToMove == null)
			return false;
		
		if(avoidTarget.squaredDistanceTo(posToMove.x, posToMove.y, posToMove.z) < avoidTarget.squaredDistanceTo(entity))
			return false;
			
			
		path = navigation.findPathTo(posToMove.x, posToMove.y, posToMove.z, 0);
		return path != null;
	}

	@Nullable
	private SkullPikeEntity getClosestEntity(World world, LivingEntity p_225318_3_, double p_225318_4_, double p_225318_6_, double p_225318_8_, Box p_225318_10_) {
		return getClosestEntity(world.getEntitiesIncludingUngeneratedChunks(SkullPikeEntity.class, p_225318_10_, null), p_225318_3_, p_225318_4_, p_225318_6_, p_225318_8_);
	}

	@Nullable
	private SkullPikeEntity getClosestEntity(List<SkullPikeEntity> entities, LivingEntity target, double x, double y, double z) {
		double d0 = -1.0D;
		SkullPikeEntity t = null;

		for(SkullPikeEntity t1 : entities) {
			if(!t1.isVisible(target))
				continue;
			
			double d1 = t1.squaredDistanceTo(x, y, z);
			if (d0 == -1.0D || d1 < d0) {
				d0 = d1;
				t = t1;
			}
		}

		return t;
	}

	@Override
	public boolean shouldContinue() {
		return !this.navigation.isIdle();
	}

	@Override
	public void start() {
		this.navigation.startMovingAlong(this.path, this.farSpeed);
	}

	@Override
	public void stop() {
		this.avoidTarget = null;
	}

	@Override
	public void tick() {
		if (this.entity.squaredDistanceTo(this.avoidTarget) < 49.0D) {
			this.entity.getNavigation().setSpeed(this.nearSpeed);
		} else {
			this.entity.getNavigation().setSpeed(this.farSpeed);
		}

	}
}