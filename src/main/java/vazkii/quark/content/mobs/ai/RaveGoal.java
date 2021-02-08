/**
 * This class was created by <WireSegal>. It's distributed as
 * part of the Quark Mod. Get the Source Code in github:
 * https://github.com/Vazkii/Quark
 * <p>
 * Quark is Open Source and distributed under the
 * CC-BY-NC-SA 3.0 License: https://creativecommons.org/licenses/by-nc-sa/3.0/deed.en_GB
 * <p>
 * File Created @ [Jul 14, 2019, 19:51 AM (EST)]
 */
package vazkii.quark.content.mobs.ai;

import net.minecraft.entity.ai.goal.Goal;
import vazkii.quark.content.mobs.entity.CrabEntity;

import java.util.EnumSet;

public class RaveGoal extends Goal {
	private final CrabEntity crab;

	public RaveGoal(CrabEntity crab) {
		this.crab = crab;
		this.setMutexFlags(EnumSet.of(Flag.MOVE, Flag.JUMP));
	}

	@Override
	public boolean shouldExecute() {
		return crab.isRaving();
	}

	@Override
	public void startExecuting() {
		this.crab.getNavigator().clearPath();
	}
}
