package vazkii.quark.content.building.module;

import java.util.LinkedList;
import java.util.List;

import net.minecraft.block.Block;
import net.minecraft.block.Material;
import net.minecraft.item.ItemGroup;
import net.minecraft.sound.BlockSoundGroup;
import net.minecraft.util.DyeColor;
import vazkii.quark.base.block.QuarkBlock;
import vazkii.quark.base.handler.FuelHandler;
import vazkii.quark.base.module.LoadModule;
import vazkii.quark.base.module.QuarkModule;
import vazkii.quark.base.module.ModuleCategory;

@LoadModule(category = ModuleCategory.BUILDING)
public class QuiltedWoolModule extends QuarkModule {

	private static List<Block> quiltedWoolColors = new LinkedList<>();
	
	@Override
	public void construct() {
		for(DyeColor dye : DyeColor.values()) {
			Block b = new QuarkBlock(dye.getName() + "_quilted_wool", this, ItemGroup.BUILDING_BLOCKS,
					Block.Properties.of(Material.WOOL, dye.getMaterialColor())
					.strength(0.8F)
					.sounds(BlockSoundGroup.WOOL));
			
			quiltedWoolColors.add(b);
		}
	}
	
	@Override
	public void setup() {
		for(Block b : quiltedWoolColors)
			FuelHandler.addFuel(b, 100);
	}
	
}
