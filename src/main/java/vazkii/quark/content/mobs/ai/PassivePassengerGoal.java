/**
 * This class was created by <WireSegal>. It's distributed as
 * part of the Quark Mod. Get the Source Code in github:
 * https://github.com/Vazkii/Quark
 * <p>
 * Quark is Open Source and distributed under the
 * CC-BY-NC-SA 3.0 License: https://creativecommons.org/licenses/by-nc-sa/3.0/deed.en_GB
 * <p>
 * File Created @ [Aug 09, 2019, 09:59 AM (EST)]
 */
package vazkii.quark.content.mobs.ai;

import net.minecraft.entity.MobEntity;
import net.minecraft.entity.ai.goal.Goal;

import java.util.EnumSet;

public class PassivePassengerGoal extends Goal {
	private final MobEntity entity;

	public PassivePassengerGoal(MobEntity entity) {
		this.entity = entity;
		setMutexFlags(EnumSet.of(Flag.MOVE, Flag.LOOK, Flag.JUMP, Flag.TARGET));
	}

	@Override
	public boolean shouldExecute() {
		return entity.isPassenger();
	}
}
