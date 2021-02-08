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
    public boolean canStart() {
        return isEnabled.getAsBoolean() && parent.canStart();
    }

    @Override
    public boolean shouldContinue() {
        return isEnabled.getAsBoolean() && parent.shouldContinue();
    }

    @Override
    public boolean canStop() {
        return parent.canStop();
    }

    @Override
    public void start() {
        parent.start();
    }

    @Override
    public void stop() {
        parent.stop();
    }

    @Override
    public void tick() {
        parent.tick();
    }

    @Nonnull
    @Override
    public EnumSet<Control> getControls() {
        return parent.getControls();
    }
}
