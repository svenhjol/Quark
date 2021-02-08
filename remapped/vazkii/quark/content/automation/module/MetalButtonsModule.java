package vazkii.quark.content.automation.module;

import vazkii.quark.base.module.LoadModule;
import vazkii.quark.base.module.QuarkModule;
import vazkii.quark.base.module.ModuleCategory;
import vazkii.quark.base.module.config.Config;
import vazkii.quark.content.automation.block.MetalButtonBlock;

@LoadModule(category = ModuleCategory.AUTOMATION)
public class MetalButtonsModule extends QuarkModule {

	@Config(flag = "iron_metal_button")
	public static boolean enableIron = true;
	@Config(flag = "gold_metal_button")
	public static boolean enableGold = true;

	@Override
	public void construct() {
		new MetalButtonBlock("iron_button", this, 100).setCondition(() -> enableIron);
		new MetalButtonBlock("gold_button", this, 4).setCondition(() -> enableGold);
	}
	
}
