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
public class ShinglesModule extends QuarkModule {

	@Override
	public void construct() {
		VariantHandler.addSlabAndStairs(new QuarkBlock("shingles", this, ItemGroup.BUILDING_BLOCKS, Block.Properties.from(Blocks.TERRACOTTA)));

		VariantHandler.addSlabAndStairs(new QuarkBlock("white_shingles", this, ItemGroup.BUILDING_BLOCKS, Block.Properties.from(Blocks.WHITE_TERRACOTTA)));
		VariantHandler.addSlabAndStairs(new QuarkBlock("orange_shingles", this, ItemGroup.BUILDING_BLOCKS, Block.Properties.from(Blocks.ORANGE_TERRACOTTA)));
		VariantHandler.addSlabAndStairs(new QuarkBlock("magenta_shingles", this, ItemGroup.BUILDING_BLOCKS, Block.Properties.from(Blocks.MAGENTA_TERRACOTTA)));
		VariantHandler.addSlabAndStairs(new QuarkBlock("light_blue_shingles", this, ItemGroup.BUILDING_BLOCKS, Block.Properties.from(Blocks.LIGHT_BLUE_TERRACOTTA)));
		VariantHandler.addSlabAndStairs(new QuarkBlock("yellow_shingles", this, ItemGroup.BUILDING_BLOCKS, Block.Properties.from(Blocks.YELLOW_TERRACOTTA)));
		VariantHandler.addSlabAndStairs(new QuarkBlock("lime_shingles", this, ItemGroup.BUILDING_BLOCKS, Block.Properties.from(Blocks.LIME_TERRACOTTA)));
		VariantHandler.addSlabAndStairs(new QuarkBlock("pink_shingles", this, ItemGroup.BUILDING_BLOCKS, Block.Properties.from(Blocks.PINK_TERRACOTTA)));
		VariantHandler.addSlabAndStairs(new QuarkBlock("gray_shingles", this, ItemGroup.BUILDING_BLOCKS, Block.Properties.from(Blocks.GRAY_TERRACOTTA)));
		VariantHandler.addSlabAndStairs(new QuarkBlock("light_gray_shingles", this, ItemGroup.BUILDING_BLOCKS, Block.Properties.from(Blocks.LIGHT_GRAY_TERRACOTTA)));
		VariantHandler.addSlabAndStairs(new QuarkBlock("cyan_shingles", this, ItemGroup.BUILDING_BLOCKS, Block.Properties.from(Blocks.CYAN_TERRACOTTA)));
		VariantHandler.addSlabAndStairs(new QuarkBlock("purple_shingles", this, ItemGroup.BUILDING_BLOCKS, Block.Properties.from(Blocks.PURPLE_TERRACOTTA)));
		VariantHandler.addSlabAndStairs(new QuarkBlock("blue_shingles", this, ItemGroup.BUILDING_BLOCKS, Block.Properties.from(Blocks.BLUE_TERRACOTTA)));
		VariantHandler.addSlabAndStairs(new QuarkBlock("brown_shingles", this, ItemGroup.BUILDING_BLOCKS, Block.Properties.from(Blocks.BROWN_TERRACOTTA)));
		VariantHandler.addSlabAndStairs(new QuarkBlock("green_shingles", this, ItemGroup.BUILDING_BLOCKS, Block.Properties.from(Blocks.GREEN_TERRACOTTA)));
		VariantHandler.addSlabAndStairs(new QuarkBlock("red_shingles", this, ItemGroup.BUILDING_BLOCKS, Block.Properties.from(Blocks.RED_TERRACOTTA)));
		VariantHandler.addSlabAndStairs(new QuarkBlock("black_shingles", this, ItemGroup.BUILDING_BLOCKS, Block.Properties.from(Blocks.BLACK_TERRACOTTA)));
	}

}
