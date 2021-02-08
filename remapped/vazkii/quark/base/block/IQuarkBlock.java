package vazkii.quark.base.block;

import net.minecraft.block.BlockState;
import net.minecraft.block.Material;
import net.minecraft.state.property.Properties;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.BlockView;
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
    default int getFlammability(BlockState state, BlockView world, BlockPos pos, Direction face) {
        if (state.getEntries().containsKey(Properties.WATERLOGGED) && state.get(Properties.WATERLOGGED))
            return 0;

        Material material = state.getMaterial();
        if (material == Material.WOOL)
            return 60;
        return state.getMaterial().isBurnable() ? 20 : 0;
    }

    @Override
    default int getFireSpreadSpeed(BlockState state, BlockView world, BlockPos pos, Direction face) {
        if (state.getEntries().containsKey(Properties.WATERLOGGED) && state.get(Properties.WATERLOGGED))
            return 0;

        Material material = state.getMaterial();
        if (material == Material.WOOL)
            return 30;
        return state.getMaterial().isBurnable() ? 5 : 0;
    }
}
