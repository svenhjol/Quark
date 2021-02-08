package vazkii.quark.content.building.module;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.block.material.MaterialColor;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraftforge.common.ToolType;
import vazkii.quark.base.block.QuarkBlock;
import vazkii.quark.base.block.QuarkPillarBlock;
import vazkii.quark.base.handler.VariantHandler;
import vazkii.quark.base.item.QuarkItem;
import vazkii.quark.base.module.LoadModule;
import vazkii.quark.base.module.QuarkModule;
import vazkii.quark.base.module.ModuleCategory;

@LoadModule(category = ModuleCategory.BUILDING)
public class MidoriModule extends QuarkModule {

	@Override
	public void construct() {
		new QuarkItem("cactus_paste", this, new Item.Properties().group(ItemGroup.MATERIALS));
		
		Block.Properties props = Block.Properties.create(Material.ROCK, MaterialColor.LIME)
				.setRequiresTool()
        		.harvestTool(ToolType.PICKAXE)
        		.hardnessAndResistance(1.5F, 6.0F);
		
		VariantHandler.addSlabAndStairs(new QuarkBlock("midori_block", this, ItemGroup.BUILDING_BLOCKS, props));
		new QuarkPillarBlock("midori_pillar", this, ItemGroup.BUILDING_BLOCKS, props);
	}
	
}
