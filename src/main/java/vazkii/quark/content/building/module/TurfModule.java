package vazkii.quark.content.building.module;

import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.item.ItemGroup;
import vazkii.quark.base.block.IQuarkBlock;
import vazkii.quark.base.handler.VariantHandler;
import vazkii.quark.base.module.LoadModule;
import vazkii.quark.base.module.QuarkModule;
import vazkii.quark.content.building.block.TurfBlock;
import vazkii.quark.base.module.ModuleCategory;

/**
 * @author WireSegal
 * Created at 11:18 AM on 10/4/19.
 */
@LoadModule(category = ModuleCategory.BUILDING)
public class TurfModule extends QuarkModule {
    @Override
    public void construct() {
        IQuarkBlock turf = new TurfBlock("turf", this, ItemGroup.BUILDING_BLOCKS,
                Block.Properties.from(Blocks.GRASS_BLOCK));
        VariantHandler.addSlabAndStairs(turf);
    }
}
