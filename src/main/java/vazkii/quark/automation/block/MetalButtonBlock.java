package vazkii.quark.automation.block;

import javax.annotation.Nonnull;

import net.minecraft.block.Block;
import net.minecraft.block.Material;
import net.minecraft.item.ItemGroup;
import net.minecraft.sound.BlockSoundGroup;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;
import vazkii.quark.base.block.QuarkButtonBlock;
import vazkii.quark.base.module.Module;

/**
 * @author WireSegal
 * Created at 9:14 PM on 10/8/19.
 */
public class MetalButtonBlock extends QuarkButtonBlock {

    private final int speed;

    public MetalButtonBlock(String regname, Module module, int speed) {
        super(regname, module, ItemGroup.REDSTONE,
                Block.Properties.of(Material.SUPPORTED)
                        .noCollision()
                        .strength(0.5F)
                        .sounds(BlockSoundGroup.METAL));
        this.speed = speed;
    }

    @Override // tickRate
    public int getPressTicks() {
        return speed;
    }

    @Nonnull
    @Override
    protected SoundEvent getClickSound(boolean powered) {
        return powered ? SoundEvents.BLOCK_STONE_BUTTON_CLICK_ON : SoundEvents.BLOCK_STONE_BUTTON_CLICK_OFF;
    }
}
