package vazkii.quark.building.module;

import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.item.ItemGroup;
import vazkii.quark.base.block.QuarkBlock;
import vazkii.quark.base.handler.VariantHandler;
import vazkii.quark.base.module.LoadModule;
import vazkii.quark.base.module.Module;
import vazkii.quark.base.module.ModuleCategory;

@LoadModule(category = ModuleCategory.BUILDING)
public class DuskboundBlocksModule extends Module {

	@Override
	public void construct() {
		VariantHandler.addSlabAndStairs(new QuarkBlock("duskbound_block", this, ItemGroup.DECORATIONS, Block.Properties.copy(Blocks.PURPUR_BLOCK)));
		
		new QuarkBlock("duskbound_lantern", this, ItemGroup.DECORATIONS, 
				Block.Properties.copy(Blocks.PURPUR_BLOCK)
				.lightLevel(b -> 15)); // light value
	}
	
}
