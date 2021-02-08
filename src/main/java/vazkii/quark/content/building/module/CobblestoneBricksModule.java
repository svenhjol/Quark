package vazkii.quark.content.building.module;

import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.item.ItemGroup;
import vazkii.quark.base.block.QuarkBlock;
import vazkii.quark.base.handler.VariantHandler;
import vazkii.quark.base.module.LoadModule;
import vazkii.quark.base.module.QuarkModule;
import vazkii.quark.base.module.ModuleCategory;

@LoadModule(category = ModuleCategory.BUILDING)
public class CobblestoneBricksModule extends QuarkModule {

	@Override
	public void construct() {
		VariantHandler.addSlabStairsWall(new QuarkBlock("cobblestone_bricks", this, ItemGroup.BUILDING_BLOCKS, Block.Properties.from(Blocks.COBBLESTONE)));
		VariantHandler.addSlabStairsWall(new QuarkBlock("mossy_cobblestone_bricks", this, ItemGroup.BUILDING_BLOCKS, Block.Properties.from(Blocks.MOSSY_COBBLESTONE)));
	}
	
}
