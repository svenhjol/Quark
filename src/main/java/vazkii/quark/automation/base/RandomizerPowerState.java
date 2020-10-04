package vazkii.quark.automation.base;

import java.util.Locale;
import net.minecraft.util.StringIdentifiable;

/**
 * @author WireSegal
 * Created at 10:12 AM on 8/26/19.
 */
public enum RandomizerPowerState implements StringIdentifiable {
    OFF, LEFT, RIGHT;


    @Override
    public String asString() { // getName
        return name().toLowerCase(Locale.ROOT);
    }
}
