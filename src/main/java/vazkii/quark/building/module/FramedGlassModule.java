package vazkii.quark.building.module;

import net.minecraft.block.Block;
import net.minecraft.block.Material;
import net.minecraft.item.ItemGroup;
import net.minecraft.sound.BlockSoundGroup;
import net.minecraftforge.common.ToolType;
import vazkii.quark.base.block.IQuarkBlock;
import vazkii.quark.base.block.QuarkInheritedPaneBlock;
import vazkii.quark.base.module.LoadModule;
import vazkii.quark.base.module.Module;
import vazkii.quark.base.module.ModuleCategory;
import vazkii.quark.building.block.FramedGlassBlock;

@LoadModule(category = ModuleCategory.BUILDING)
public class FramedGlassModule extends Module {

	@Override
	public void construct() {
		IQuarkBlock framedGlass = new FramedGlassBlock("framed_glass", this, ItemGroup.BUILDING_BLOCKS,
				Block.Properties.of(Material.GLASS)
						.strength(3F, 10F)
						.sounds(BlockSoundGroup.GLASS)
						.harvestLevel(1)
						.harvestTool(ToolType.PICKAXE));
		new QuarkInheritedPaneBlock(framedGlass);
	}

}
