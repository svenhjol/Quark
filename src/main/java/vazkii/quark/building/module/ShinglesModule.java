package vazkii.quark.building.module;

import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.item.ItemGroup;
import net.minecraft.util.DyeColor;
import vazkii.quark.base.block.QuarkBlock;
import vazkii.quark.base.handler.VariantHandler;
import vazkii.quark.base.module.LoadModule;
import vazkii.quark.base.module.Module;
import vazkii.quark.base.module.ModuleCategory;

@LoadModule(category = ModuleCategory.BUILDING)
public class ShinglesModule extends Module {

	@Override
	public void construct() {
		VariantHandler.addSlabAndStairs(new QuarkBlock("shingles", this, ItemGroup.BUILDING_BLOCKS, Block.Properties.copy(Blocks.TERRACOTTA)));

		for(DyeColor dye : DyeColor.values())
			VariantHandler.addSlabAndStairs(new QuarkBlock(dye.getName() + "_shingles", this, ItemGroup.BUILDING_BLOCKS, Block.Properties.copy(Blocks.TERRACOTTA)));
	}

}
