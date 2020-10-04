package vazkii.quark.building.module;

import net.minecraft.block.ComposterBlock;
import vazkii.quark.base.handler.VariantHandler;
import vazkii.quark.base.module.LoadModule;
import vazkii.quark.base.module.Module;
import vazkii.quark.base.module.ModuleCategory;
import vazkii.quark.base.module.config.Config;
import vazkii.quark.building.block.ThatchBlock;

@LoadModule(category = ModuleCategory.BUILDING)
public class ThatchModule extends Module {

	@Config.Min(0)
	@Config.Max(1)
	@Config public static double fallDamageMultiplier = 0.5;
	
	public static ThatchBlock thatch;
	
	@Override
	public void construct() {
		thatch = new ThatchBlock(this);
		VariantHandler.addSlabAndStairs(thatch);
	}
	
	@Override
	public void setup() {
		ComposterBlock.ITEM_TO_LEVEL_INCREASE_CHANCE.put(thatch.asItem(), 0.65F);
	}
	
}
