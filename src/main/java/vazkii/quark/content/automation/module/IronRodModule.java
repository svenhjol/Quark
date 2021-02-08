package vazkii.quark.content.automation.module;

import net.minecraft.block.Block;
import vazkii.quark.base.module.LoadModule;
import vazkii.quark.base.module.QuarkModule;
import vazkii.quark.content.automation.block.IronRodBlock;
import vazkii.quark.base.module.ModuleCategory;

@LoadModule(category = ModuleCategory.AUTOMATION)
public class IronRodModule extends QuarkModule {
	
	public static Block iron_rod;
	
	@Override
	public void construct() {
		iron_rod = new IronRodBlock(this);
	}
	
}
