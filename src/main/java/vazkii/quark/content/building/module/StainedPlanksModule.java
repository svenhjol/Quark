package vazkii.quark.content.building.module;

import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.item.DyeColor;
import net.minecraft.item.ItemGroup;
import vazkii.quark.base.block.QuarkBlock;
import vazkii.quark.base.handler.VariantHandler;
import vazkii.quark.base.module.LoadModule;
import vazkii.quark.base.module.QuarkModule;
import vazkii.quark.base.module.ModuleCategory;

@LoadModule(category = ModuleCategory.BUILDING)
public class StainedPlanksModule extends QuarkModule {

	@Override
	public void construct() {
		for(DyeColor dye : DyeColor.values())
			VariantHandler.addSlabAndStairs(new QuarkBlock(dye.getTranslationKey() + "_stained_planks", this, ItemGroup.BUILDING_BLOCKS, Block.Properties.from(Blocks.OAK_PLANKS)));
	}
	
}
