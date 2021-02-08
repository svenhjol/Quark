package vazkii.quark.content.building.module;

import net.minecraft.block.AbstractBlock.Properties;
import net.minecraft.block.Blocks;
import vazkii.quark.base.block.QuarkPaneBlock;
import vazkii.quark.base.handler.RenderLayerHandler.RenderTypeSkeleton;
import vazkii.quark.base.module.LoadModule;
import vazkii.quark.base.module.QuarkModule;
import vazkii.quark.base.module.ModuleCategory;

@LoadModule(category = ModuleCategory.BUILDING)
public class GoldBarsModule extends QuarkModule {

	@Override
	public void construct() {
		new QuarkPaneBlock("gold_bars", this, Properties.from(Blocks.IRON_BARS), RenderTypeSkeleton.CUTOUT);
	}
	
}
