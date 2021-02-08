package vazkii.quark.content.tweaks.block;

import javax.annotation.Nullable;

import net.minecraft.block.BlockState;
import net.minecraft.item.ItemGroup;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IWorldReader;
import vazkii.quark.base.block.QuarkGlassBlock;
import vazkii.quark.base.handler.RenderLayerHandler;
import vazkii.quark.base.handler.RenderLayerHandler.RenderTypeSkeleton;
import vazkii.quark.base.module.QuarkModule;

/**
 * @author WireSegal
 * Created at 12:49 PM on 8/24/19.
 */
public class DirtyGlassBlock extends QuarkGlassBlock {

    private static final float[] BEACON_COLOR_MULTIPLIER = new float[] { 0.25F, 0.125F, 0F };

    public DirtyGlassBlock(String regname, QuarkModule module, ItemGroup creativeTab, Properties properties) {
        super(regname, module, creativeTab, properties);
        RenderLayerHandler.setRenderType(this, RenderTypeSkeleton.TRANSLUCENT);
    }

    @Nullable
    @Override
    public float[] getBeaconColorMultiplier(BlockState state, IWorldReader world, BlockPos pos, BlockPos beaconPos) {
        return BEACON_COLOR_MULTIPLIER;
    }

}
