package vazkii.quark.content.automation.block;

import javax.annotation.Nonnull;

import net.minecraft.block.Block;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.item.ItemGroup;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.SoundEvents;
import vazkii.quark.base.block.QuarkButtonBlock;
import vazkii.quark.base.module.QuarkModule;

/**
 * @author WireSegal
 * Created at 9:14 PM on 10/8/19.
 */
public class MetalButtonBlock extends QuarkButtonBlock {

    private final int speed;

    public MetalButtonBlock(String regname, QuarkModule module, int speed) {
        super(regname, module, ItemGroup.REDSTONE,
                Block.Properties.create(Material.MISCELLANEOUS)
                        .doesNotBlockMovement()
                        .hardnessAndResistance(0.5F)
                        .sound(SoundType.METAL));
        this.speed = speed;
    }

    @Override
    public int getActiveDuration() {
        return speed;
    }

    @Nonnull
    @Override
    protected SoundEvent getSoundEvent(boolean powered) {
        return powered ? SoundEvents.BLOCK_STONE_BUTTON_CLICK_ON : SoundEvents.BLOCK_STONE_BUTTON_CLICK_OFF;
    }
}
