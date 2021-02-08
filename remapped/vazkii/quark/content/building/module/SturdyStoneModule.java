package vazkii.quark.content.building.module;

import vazkii.quark.base.module.LoadModule;
import vazkii.quark.base.module.QuarkModule;
import vazkii.quark.content.building.block.SturdyStoneBlock;
import vazkii.quark.base.module.ModuleCategory;

@LoadModule(category = ModuleCategory.BUILDING)
public class SturdyStoneModule extends QuarkModule {

	@Override
	public void construct() {
		new SturdyStoneBlock(this);
	}
	
}
