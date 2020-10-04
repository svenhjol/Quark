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

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.goal.Goal;
import vazkii.quark.mobs.entity.FoxhoundEntity;

import java.util.EnumSet;

public class SleepGoal extends Goal {

	private final FoxhoundEntity foxhound;
	private boolean isSleeping;
	private boolean wasSitting;

	public SleepGoal(FoxhoundEntity foxhound) {
		this.foxhound = foxhound;
		this.setControls(EnumSet.of(Control.MOVE, Control.JUMP, Control.LOOK, Control.TARGET));
	}

	@Override
	public boolean canStart() {
		if (!this.foxhound.isTamed() || this.foxhound.isTouchingWater() || !this.foxhound.onGround)
			return false;
		else {
			LivingEntity living = this.foxhound.getOwner();

			if (living == null) return true;
			else
				return (!(this.foxhound.squaredDistanceTo(living) < 144.0D) || living.getAttacker() == null) && this.isSleeping;
		}
	}

	@Override
	public void start() {
		this.foxhound.getNavigation().stop();
		wasSitting = foxhound.isInSittingPose();
		this.foxhound.setInSittingPose(true);
		this.foxhound.setSleeping(true);
	}

	@Override
	public void stop() {
		this.foxhound.setInSittingPose(wasSitting);
		this.foxhound.setSleeping(false);
	}

	public void setSleeping(boolean sitting) {
		this.isSleeping = sitting;
	}
}
