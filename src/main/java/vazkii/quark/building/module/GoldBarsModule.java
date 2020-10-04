package vazkii.quark.building.module;

import net.minecraft.block.AbstractBlock.Settings;
import net.minecraft.block.Blocks;
import vazkii.quark.base.block.QuarkPaneBlock;
import vazkii.quark.base.handler.RenderLayerHandler.RenderTypeSkeleton;
import vazkii.quark.base.module.LoadModule;
import vazkii.quark.base.module.Module;
import vazkii.quark.base.module.ModuleCategory;

@LoadModule(category = ModuleCategory.BUILDING)
public class GoldBarsModule extends Module {

	@Override
	public void construct() {
		new QuarkPaneBlock("gold_bars", this, Settings.copy(Blocks.IRON_BARS), RenderTypeSkeleton.CUTOUT);
	}
	
}
