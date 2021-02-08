package vazkii.quark.content.tools.item;

import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.Direction;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.util.LazyOptional;
import vazkii.quark.api.IRuneColorProvider;
import vazkii.quark.api.QuarkCapabilities;
import vazkii.quark.base.item.QuarkItem;
import vazkii.quark.base.module.QuarkModule;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * @author WireSegal
 * Created at 2:27 PM on 8/17/19.
 */
public class RuneItem extends QuarkItem implements IRuneColorProvider {

    private final int color;

    public RuneItem(String regname, QuarkModule module, int color) {
        super(regname, module, new Item.Properties().group(ItemGroup.MATERIALS));
        this.color = color;
    }

    @Override
    public boolean hasEffect(ItemStack stack) {
        return true;
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public int getRuneColor(ItemStack stack) {
        return color;
    }

    @Nullable
    @Override
    public ICapabilityProvider initCapabilities(ItemStack stack, @Nullable CompoundNBT nbt) {
        final LazyOptional<IRuneColorProvider> holder = LazyOptional.of(() -> this);

        return new ICapabilityProvider() {

            @Nonnull
            @Override
            public <T> LazyOptional<T> getCapability(@Nonnull Capability<T> cap, @Nullable Direction side) {
                return QuarkCapabilities.RUNE_COLOR.orEmpty(cap, holder);
            }

        };
    }
}
