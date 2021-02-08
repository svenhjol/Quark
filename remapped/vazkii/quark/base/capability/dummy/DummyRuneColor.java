package vazkii.quark.base.capability.dummy;

import net.minecraft.item.ItemStack;
import vazkii.quark.api.IRuneColorProvider;

public class DummyRuneColor implements IRuneColorProvider {
    @Override
    public int getRuneColor(ItemStack stack) {
        return -1;
    }
}
