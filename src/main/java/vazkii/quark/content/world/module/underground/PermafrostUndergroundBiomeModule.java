package vazkii.quark.content.world.module.underground;

import net.minecraft.block.Block;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.block.material.MaterialColor;
import net.minecraft.item.ItemGroup;
import net.minecraft.world.biome.Biome;
import net.minecraftforge.common.ToolType;
import vazkii.quark.base.block.QuarkBlock;
import vazkii.quark.base.handler.VariantHandler;
import vazkii.quark.base.module.LoadModule;
import vazkii.quark.base.module.ModuleCategory;
import vazkii.quark.content.world.config.UndergroundBiomeConfig;
import vazkii.quark.content.world.gen.underground.PermafrostUndergroundBiome;

@LoadModule(category = ModuleCategory.WORLD)
public class PermafrostUndergroundBiomeModule extends UndergroundBiomeModule {

	public static QuarkBlock permafrost;
	
	@Override
	public void construct() {
		permafrost = new QuarkBlock("permafrost", this, ItemGroup.BUILDING_BLOCKS, 
				Block.Properties.create(Material.ROCK, MaterialColor.LIGHT_BLUE)
				.setRequiresTool()
        		.harvestTool(ToolType.PICKAXE)
				.hardnessAndResistance(1.5F, 10F)
				.sound(SoundType.STONE));
		
		VariantHandler.addSlabStairsWall(permafrost);
		VariantHandler.addSlabStairsWall(new QuarkBlock("permafrost_bricks", this, ItemGroup.BUILDING_BLOCKS, Block.Properties.from(permafrost)));
		
		super.construct();
	}
	
	@Override
	protected UndergroundBiomeConfig getBiomeConfig() {
		return new UndergroundBiomeConfig(new PermafrostUndergroundBiome(), 80, Biome.Category.ICY);
	}
	
	@Override
	protected String getBiomeName() {
		return "permafrost";
	}

}
