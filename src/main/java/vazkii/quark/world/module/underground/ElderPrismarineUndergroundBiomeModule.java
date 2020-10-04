package vazkii.quark.world.module.underground;

import net.minecraft.block.Block;
import net.minecraft.block.Material;
import net.minecraft.block.MaterialColor;
import net.minecraft.item.ItemGroup;
import net.minecraft.sound.BlockSoundGroup;
import net.minecraftforge.common.ToolType;
import net.minecraftforge.common.BiomeDictionary.Type;
import vazkii.quark.base.block.QuarkBlock;
import vazkii.quark.base.handler.VariantHandler;
import vazkii.quark.base.module.LoadModule;
import vazkii.quark.base.module.ModuleCategory;
import vazkii.quark.base.module.config.Config;
import vazkii.quark.world.block.ElderPrismarineBlock;
import vazkii.quark.world.config.UndergroundBiomeConfig;
import vazkii.quark.world.gen.underground.ElderPrismarineUndergroundBiome;

@LoadModule(category = ModuleCategory.WORLD)
public class ElderPrismarineUndergroundBiomeModule extends UndergroundBiomeModule {

	public static QuarkBlock elder_prismarine;
	public static Block elder_sea_lantern;

	@Config
	@Config.Min(0)
	@Config.Max(1)
	public static double waterChance = 0.25;

	@Config
	@Config.Min(0)
	@Config.Max(1)
	public static double lanternChance = 0.0085;

    @Override
	public void construct() {
		elder_prismarine = new ElderPrismarineBlock("elder_prismarine", this, ItemGroup.BUILDING_BLOCKS, 
				Block.Properties.of(Material.STONE, MaterialColor.ORANGE)
				.requiresTool() // needs tool
        		.harvestTool(ToolType.PICKAXE)
				.strength(1.5F, 10F)
				.sounds(BlockSoundGroup.STONE));
		
		VariantHandler.addSlabStairsWall(elder_prismarine);
		VariantHandler.addSlabAndStairs(new ElderPrismarineBlock("elder_prismarine_bricks", this, ItemGroup.BUILDING_BLOCKS, Block.Properties.copy(elder_prismarine)));
		VariantHandler.addSlabAndStairs(new ElderPrismarineBlock("dark_elder_prismarine", this, ItemGroup.BUILDING_BLOCKS, Block.Properties.copy(elder_prismarine)));
		
		elder_sea_lantern = new QuarkBlock("elder_sea_lantern", this, ItemGroup.BUILDING_BLOCKS, 
				Block.Properties.of(Material.GLASS, MaterialColor.ORANGE)
				.strength(0.3F)
				.lightLevel(b -> 15) // lightValue
				.sounds(BlockSoundGroup.GLASS));
		
		super.construct();
	}
    
	@Override
	protected String getBiomeName() {
		return "elder_prismarine";
	}
	
	@Override
	protected UndergroundBiomeConfig getBiomeConfig() {
		return new UndergroundBiomeConfig(new ElderPrismarineUndergroundBiome(), 200, Type.OCEAN);
	}

}
