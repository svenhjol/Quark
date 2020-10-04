package vazkii.quark.building.module;

import net.minecraft.block.Block;
import net.minecraft.block.Material;
import net.minecraft.item.ItemGroup;
import net.minecraft.sound.BlockSoundGroup;
import net.minecraft.util.DyeColor;
import vazkii.quark.base.block.QuarkBlock;
import vazkii.quark.base.handler.FuelHandler;
import vazkii.quark.base.module.LoadModule;
import vazkii.quark.base.module.Module;
import vazkii.quark.base.module.ModuleCategory;

@LoadModule(category = ModuleCategory.BUILDING)
public class QuiltedWoolModule extends Module {

	@Override
	public void construct() {
		for(DyeColor dye : DyeColor.values()) {
			Block b = new QuarkBlock(dye.getName() + "_quilted_wool", this, ItemGroup.BUILDING_BLOCKS,
					Block.Properties.of(Material.WOOL, dye.getMaterialColor())
					.strength(0.8F)
					.sounds(BlockSoundGroup.WOOL));
			
			FuelHandler.addFuel(b, 100);
		}
	}
	
}
