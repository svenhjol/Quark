package vazkii.quark.content.building.module;

import net.minecraft.block.Block;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.item.ItemGroup;
import net.minecraftforge.common.ToolType;
import vazkii.quark.base.block.IQuarkBlock;
import vazkii.quark.base.block.QuarkInheritedPaneBlock;
import vazkii.quark.base.module.LoadModule;
import vazkii.quark.base.module.QuarkModule;
import vazkii.quark.content.building.block.FramedGlassBlock;
import vazkii.quark.base.module.ModuleCategory;

@LoadModule(category = ModuleCategory.BUILDING)
public class FramedGlassModule extends QuarkModule {

	@Override
	public void construct() {
		IQuarkBlock framedGlass = new FramedGlassBlock("framed_glass", this, ItemGroup.BUILDING_BLOCKS,
				Block.Properties.create(Material.GLASS)
						.hardnessAndResistance(3F, 10F)
						.sound(SoundType.GLASS)
						.harvestLevel(1)
						.harvestTool(ToolType.PICKAXE));
		new QuarkInheritedPaneBlock(framedGlass);
	}

}
