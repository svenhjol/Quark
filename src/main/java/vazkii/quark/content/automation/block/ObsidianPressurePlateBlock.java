package vazkii.quark.content.automation.block;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemGroup;
import net.minecraft.state.BooleanProperty;
import net.minecraft.state.StateContainer;
import net.minecraft.state.properties.BlockStateProperties;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvents;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IWorld;
import net.minecraft.world.World;
import vazkii.quark.base.block.QuarkPressurePlateBlock;
import vazkii.quark.base.module.QuarkModule;

import javax.annotation.Nonnull;
import java.util.List;

/**
 * @author WireSegal
 * Created at 9:47 PM on 10/8/19.
 */
public class ObsidianPressurePlateBlock extends QuarkPressurePlateBlock {
    public static final BooleanProperty POWERED = BlockStateProperties.POWERED;

    public ObsidianPressurePlateBlock(String regname, QuarkModule module, ItemGroup creativeTab, Properties properties) {
        super(regname, module, creativeTab, properties);
        this.setDefaultState(getDefaultState().with(POWERED, false));
    }

    @Override
    protected void playClickOnSound(@Nonnull IWorld worldIn, @Nonnull BlockPos pos) {
        worldIn.playSound(null, pos, SoundEvents.BLOCK_STONE_PRESSURE_PLATE_CLICK_ON, SoundCategory.BLOCKS, 0.3F, 0.5F);
    }

    @Override
    protected void playClickOffSound(@Nonnull IWorld worldIn, @Nonnull BlockPos pos) {
        worldIn.playSound(null, pos, SoundEvents.BLOCK_STONE_PRESSURE_PLATE_CLICK_OFF, SoundCategory.BLOCKS, 0.3F, 0.4F);
    }

    @Override
    protected int computeRedstoneStrength(@Nonnull World worldIn, @Nonnull BlockPos pos) {
        AxisAlignedBB bounds = PRESSURE_AABB.offset(pos);
        List<? extends Entity> entities = worldIn.getEntitiesWithinAABB(PlayerEntity.class, bounds);

        if (!entities.isEmpty()) {
            for(Entity entity : entities) {
                if (!entity.doesEntityNotTriggerPressurePlate()) {
                    return 15;
                }
            }
        }

        return 0;
    }

    @Override
    protected int getRedstoneStrength(@Nonnull BlockState state) {
        return state.get(POWERED) ? 15 : 0;
    }

    @Nonnull
    @Override
    protected BlockState setRedstoneStrength(@Nonnull BlockState state, int strength) {
        return state.with(POWERED, strength > 0);
    }

    @Override
    protected void fillStateContainer(StateContainer.Builder<Block, BlockState> builder) {
        builder.add(POWERED);
    }
}
