package vazkii.quark.content.automation.module;

import net.minecraft.block.Block;
import net.minecraft.tileentity.TileEntityType;
import vazkii.arl.util.RegistryHelper;
import vazkii.quark.base.module.LoadModule;
import vazkii.quark.base.module.QuarkModule;
import vazkii.quark.content.automation.block.EnderWatcherBlock;
import vazkii.quark.content.automation.tile.EnderWatcherTileEntity;
import vazkii.quark.base.module.ModuleCategory;

@LoadModule(category = ModuleCategory.AUTOMATION)
public class EnderWatcherModule extends QuarkModule {

	public static TileEntityType<EnderWatcherTileEntity> enderWatcherTEType;

	@Override
	public void construct() {
		Block ender_watcher = new EnderWatcherBlock(this);
		enderWatcherTEType = TileEntityType.Builder.create(EnderWatcherTileEntity::new, ender_watcher).build(null);
		RegistryHelper.register(enderWatcherTEType, "ender_watcher");
	}
	
}
