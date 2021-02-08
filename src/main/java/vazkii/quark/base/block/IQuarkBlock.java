package vazkii.quark.base.block;

import net.minecraft.block.BlockState;
import net.minecraft.block.material.Material;
import net.minecraft.state.properties.BlockStateProperties;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockReader;
import net.minecraftforge.common.extensions.IForgeBlock;
import vazkii.quark.base.module.QuarkModule;

import javax.annotation.Nullable;
import java.util.function.BooleanSupplier;

/**
 * @author WireSegal
 * Created at 1:14 PM on 9/19/19.
 */
public interface IQuarkBlock extends IForgeBlock {

    @Nullable
    QuarkModule getModule();

    IQuarkBlock setCondition(BooleanSupplier condition);

    boolean doesConditionApply();

    default boolean isEnabled() {
        QuarkModule module = getModule();
        return module != null && module.enabled && doesConditionApply();
    }

    @Override
    default int getFlammability(BlockState state, IBlockReader world, BlockPos pos, Direction face) {
        if (state.getValues().containsKey(BlockStateProperties.WATERLOGGED) && state.get(BlockStateProperties.WATERLOGGED))
            return 0;

        Material material = state.getMaterial();
        if (material == Material.WOOL)
            return 60;
        return state.getMaterial().isFlammable() ? 20 : 0;
    }

    @Override
    default int getFireSpreadSpeed(BlockState state, IBlockReader world, BlockPos pos, Direction face) {
        if (state.getValues().containsKey(BlockStateProperties.WATERLOGGED) && state.get(BlockStateProperties.WATERLOGGED))
            return 0;

        Material material = state.getMaterial();
        if (material == Material.WOOL)
            return 30;
        return state.getMaterial().isFlammable() ? 5 : 0;
    }
}
