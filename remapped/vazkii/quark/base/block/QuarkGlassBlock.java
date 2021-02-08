package vazkii.quark.base.block;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.block.BlockState;
import net.minecraft.block.ShapeContext;
import net.minecraft.item.ItemGroup;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.util.shape.VoxelShapes;
import net.minecraft.world.BlockView;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import vazkii.quark.base.module.QuarkModule;

import javax.annotation.Nonnull;

/**
 * @author WireSegal
 * Created at 12:46 PM on 8/24/19.
 */
public class QuarkGlassBlock extends QuarkBlock {

    public QuarkGlassBlock(String regname, QuarkModule module, ItemGroup creativeTab, Settings properties) {
        super(regname, module, creativeTab, properties
                .nonOpaque()
                .allowsSpawning((state, world, pos, entityType) -> false)
                .solidBlock((state, world, pos) -> false)
                .suffocates((state, world, pos) -> false)
                .blockVision((state, world, pos) -> false));
    }

    @Override
    @Environment(EnvType.CLIENT)
    @SuppressWarnings("deprecation")
    public boolean isSideInvisible(@Nonnull BlockState state, BlockState adjacentBlockState, @Nonnull Direction side) {
        return adjacentBlockState.isOf(this) || super.isSideInvisible(state, adjacentBlockState, side);
    }

    @Override
    @Nonnull
    @SuppressWarnings("deprecation")
    public VoxelShape getVisualShape(@Nonnull BlockState state, @Nonnull BlockView worldIn, @Nonnull BlockPos pos, @Nonnull ShapeContext context) {
        return VoxelShapes.empty();
    }

    @Override
    @Environment(EnvType.CLIENT)
    public float getAmbientOcclusionLightLevel(@Nonnull BlockState state, @Nonnull BlockView worldIn, @Nonnull BlockPos pos) {
        return 1.0F;
    }

    @Override
    public boolean isTranslucent(@Nonnull BlockState state, @Nonnull BlockView reader, @Nonnull BlockPos pos) {
        return true;
    }

}
