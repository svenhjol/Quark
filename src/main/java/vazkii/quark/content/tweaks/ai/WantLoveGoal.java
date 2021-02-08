package vazkii.quark.content.tweaks.ai;

import java.util.EnumSet;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.goal.Goal;
import net.minecraft.entity.passive.TameableEntity;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.vector.Vector3d;
import vazkii.quark.content.tweaks.module.PatTheDogsModule;

/**
 * @author WireSegal
 * Created at 11:27 AM on 9/2/19.
 */
public class WantLoveGoal extends Goal {

    private static final String PET_TIME = "quark:PetTime";

    public static void setPetTime(TameableEntity entity) {
        entity.getPersistentData().putLong(PET_TIME, entity.world.getGameTime());
    }

    public static boolean canPet(TameableEntity entity) {
        return timeSinceLastPet(entity) > 20;
    }

    public static boolean needsPets(TameableEntity entity) {
        if (PatTheDogsModule.dogsWantLove <= 0)
            return false;

        return timeSinceLastPet(entity) > PatTheDogsModule.dogsWantLove;
    }

    public static long timeSinceLastPet(TameableEntity entity) {
        if (!entity.isTamed())
            return 0;

        long lastPetAt = entity.getPersistentData().getLong(PET_TIME);
        return entity.world.getGameTime() - lastPetAt;
    }

    private final TameableEntity creature;
    private LivingEntity leapTarget;
    public final float leapUpMotion;

    public WantLoveGoal(TameableEntity creature, float leapMotion) {
        this.creature = creature;
        this.leapUpMotion = leapMotion;
        this.setMutexFlags(EnumSet.of(Flag.MOVE, Flag.LOOK, Flag.JUMP, Flag.TARGET));
    }

    public boolean shouldExecute() {
        if (!needsPets(creature))
            return false;

        this.leapTarget = this.creature.getOwner();

        if (this.leapTarget == null)
            return false;
        else {
            double distanceToTarget = this.creature.getDistanceSq(this.leapTarget);

            return 4 <= distanceToTarget && distanceToTarget <= 16 &&
                    this.creature.onGround && this.creature.getRNG().nextInt(5) == 0;
        }
    }

    public boolean shouldContinueExecuting() {
        if (!WantLoveGoal.needsPets(creature))
            return false;
        return !this.creature.onGround;
    }

    public void startExecuting() {
    	Vector3d leapPos = leapTarget.getPositionVec();
    	Vector3d creaturePos = creature.getPositionVec();
    	
        double dX = leapPos.x - creaturePos.x;
        double dZ = leapPos.z - creaturePos.z;
        float leapMagnitude = MathHelper.sqrt(dX * dX + dZ * dZ);

        Vector3d motion = this.creature.getMotion();

        if (leapMagnitude >= 0.0001) {
            motion = motion.add(
                    dX / leapMagnitude * 0.4 + motion.x * 0.2,
                    0,
                    dZ / leapMagnitude * 0.4 + motion.z * 0.2);
        }

        motion = motion.add(0, leapUpMotion, 0);

        this.creature.setMotion(motion);
    }
}

