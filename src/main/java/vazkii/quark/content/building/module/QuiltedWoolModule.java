package vazkii.quark.content.building.module;

import java.util.LinkedList;
import java.util.List;

import net.minecraft.block.Block;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.item.DyeColor;
import net.minecraft.item.ItemGroup;
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
			Block b = new QuarkBlock(dye.getTranslationKey() + "_quilted_wool", this, ItemGroup.BUILDING_BLOCKS,
					Block.Properties.create(Material.WOOL, dye.getMapColor())
					.hardnessAndResistance(0.8F)
					.sound(SoundType.CLOTH));
			
			quiltedWoolColors.add(b);
		}
	}
	
	@Override
	public void setup() {
		for(Block b : quiltedWoolColors)
			FuelHandler.addFuel(b, 100);
	}
	
}
