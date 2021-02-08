package vazkii.quark.base.util;

import net.minecraft.entity.ai.goal.Goal;

import javax.annotation.Nonnull;
import java.util.EnumSet;
import java.util.function.BooleanSupplier;

/**
 * @author WireSegal
 * Created at 12:32 PM on 9/8/19.
 */
public class IfFlagGoal extends Goal {
    private final Goal parent;
    private final BooleanSupplier isEnabled;

    public IfFlagGoal(Goal parent, BooleanSupplier isEnabled) {
        super();
        this.parent = parent;
        this.isEnabled = isEnabled;
    }

    @Override
    public boolean shouldExecute() {
        return isEnabled.getAsBoolean() && parent.shouldExecute();
    }

    @Override
    public boolean shouldContinueExecuting() {
        return isEnabled.getAsBoolean() && parent.shouldContinueExecuting();
    }

    @Override
    public boolean isPreemptible() {
        return parent.isPreemptible();
    }

    @Override
    public void startExecuting() {
        parent.startExecuting();
    }

    @Override
    public void resetTask() {
        parent.resetTask();
    }

    @Override
    public void tick() {
        parent.tick();
    }

    @Nonnull
    @Override
    public EnumSet<Flag> getMutexFlags() {
        return parent.getMutexFlags();
    }
}
