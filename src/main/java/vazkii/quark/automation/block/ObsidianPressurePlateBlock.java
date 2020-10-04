package vazkii.quark.automation.block;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemGroup;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.BooleanProperty;
import net.minecraft.state.property.Properties;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.world.World;
import net.minecraft.world.WorldAccess;
import vazkii.quark.base.block.QuarkPressurePlateBlock;
import vazkii.quark.base.module.Module;

import javax.annotation.Nonnull;
import java.util.List;

/**
 * @author WireSegal
 * Created at 9:47 PM on 10/8/19.
 */
public class ObsidianPressurePlateBlock extends QuarkPressurePlateBlock {
    public static final BooleanProperty POWERED = Properties.POWERED;

    public ObsidianPressurePlateBlock(String regname, Module module, ItemGroup creativeTab, Settings properties) {
        super(regname, module, creativeTab, properties);
        this.setDefaultState(getDefaultState().with(POWERED, false));
    }

    @Override
    protected void playPressSound(@Nonnull WorldAccess worldIn, @Nonnull BlockPos pos) {
        worldIn.playSound(null, pos, SoundEvents.BLOCK_STONE_PRESSURE_PLATE_CLICK_ON, SoundCategory.BLOCKS, 0.3F, 0.5F);
    }

    @Override
    protected void playDepressSound(@Nonnull WorldAccess worldIn, @Nonnull BlockPos pos) {
        worldIn.playSound(null, pos, SoundEvents.BLOCK_STONE_PRESSURE_PLATE_CLICK_OFF, SoundCategory.BLOCKS, 0.3F, 0.4F);
    }

    @Override
    protected int getRedstoneOutput(@Nonnull World worldIn, @Nonnull BlockPos pos) {
        Box bounds = BOX.offset(pos);
        List<? extends Entity> entities = worldIn.getNonSpectatingEntities(PlayerEntity.class, bounds);

        if (!entities.isEmpty()) {
            for(Entity entity : entities) {
                if (!entity.canAvoidTraps()) {
                    return 15;
                }
            }
        }

        return 0;
    }

    @Override
    protected int getRedstoneOutput(@Nonnull BlockState state) {
        return state.get(POWERED) ? 15 : 0;
    }

    @Nonnull
    @Override
    protected BlockState setRedstoneOutput(@Nonnull BlockState state, int strength) {
        return state.with(POWERED, strength > 0);
    }

    @Override
    protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
        builder.add(POWERED);
    }
}
