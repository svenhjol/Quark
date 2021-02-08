package vazkii.quark.content.building.module;

import net.minecraft.block.Blocks;
import vazkii.quark.base.handler.ItemOverrideHandler;
import vazkii.quark.base.handler.MiscUtil;
import vazkii.quark.base.module.LoadModule;
import vazkii.quark.base.module.QuarkModule;
import vazkii.quark.base.module.ModuleCategory;
import vazkii.quark.base.module.config.Config;
import vazkii.quark.content.building.block.VariantBookshelfBlock;

@LoadModule(category = ModuleCategory.BUILDING)
public class VariantBookshelvesModule extends QuarkModule {

	@Config public static boolean changeNames = true;

	@Override
	public void construct() {
		for(String type : MiscUtil.OVERWORLD_VARIANT_WOOD_TYPES)
			new VariantBookshelfBlock(type, this, true);
		for(String type : MiscUtil.NETHER_WOOD_TYPES)
			new VariantBookshelfBlock(type, this, false);
	}

	@Override
	public void configChanged() {
		ItemOverrideHandler.changeBlockLocalizationKey(Blocks.BOOKSHELF, "block.quark.oak_bookshelf", changeNames && enabled);
	}
}
