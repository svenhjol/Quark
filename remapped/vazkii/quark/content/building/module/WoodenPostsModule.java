package vazkii.quark.content.building.module;

import com.google.common.collect.ImmutableList;

import net.minecraft.block.Blocks;
import vazkii.quark.base.module.LoadModule;
import vazkii.quark.base.module.QuarkModule;
import vazkii.quark.content.building.block.WoodPostBlock;
import vazkii.quark.base.module.ModuleCategory;

@LoadModule(category = ModuleCategory.BUILDING)
public class WoodenPostsModule extends QuarkModule {

	@Override
	public void construct() {
		ImmutableList.of(Blocks.OAK_FENCE, Blocks.SPRUCE_FENCE, Blocks.BIRCH_FENCE, 
				Blocks.JUNGLE_FENCE, Blocks.ACACIA_FENCE, Blocks.DARK_OAK_FENCE, 
				Blocks.CRIMSON_FENCE, Blocks.WARPED_FENCE)
		.forEach(b -> new WoodPostBlock(this, b));
	}
	
}
