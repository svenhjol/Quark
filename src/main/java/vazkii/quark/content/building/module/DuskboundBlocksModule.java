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
public class DuskboundBlocksModule extends QuarkModule {

	@Override
	public void construct() {
		VariantHandler.addSlabAndStairs(new QuarkBlock("duskbound_block", this, ItemGroup.BUILDING_BLOCKS, Block.Properties.from(Blocks.PURPUR_BLOCK)));
		
		new QuarkBlock("duskbound_lantern", this, ItemGroup.BUILDING_BLOCKS, 
				Block.Properties.from(Blocks.PURPUR_BLOCK)
				.setLightLevel(b -> 15));
	}
	
}
