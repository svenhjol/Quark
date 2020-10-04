package vazkii.quark.base.client;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.options.KeyBinding;
import net.minecraft.client.util.InputUtil.Type;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

/**
 * @author WireSegal
 * Created at 12:19 PM on 10/6/19.
 */
@Environment(EnvType.CLIENT)
public class SortedKeyBinding extends KeyBinding {
    private final int priority;

    public SortedKeyBinding(String description, Type type, int keyCode, String category, int priority) {
        super(description, type, keyCode, category);
        this.priority = priority;
    }

    @Override
    public int compareTo(KeyBinding keyBinding) {
        if (this.getCategory().equals(keyBinding.getCategory()) && keyBinding instanceof SortedKeyBinding)
            return Integer.compare(priority, ((SortedKeyBinding) keyBinding).priority);
        return super.compareTo(keyBinding);
    }
}
