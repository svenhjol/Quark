package vazkii.quark.world.module;

import net.minecraft.world.gen.GenerationStep;
import net.minecraftforge.common.BiomeDictionary.Type;
import vazkii.quark.base.module.LoadModule;
import vazkii.quark.base.module.Module;
import vazkii.quark.base.module.ModuleCategory;
import vazkii.quark.base.module.config.Config;
import vazkii.quark.base.world.WorldGenHandler;
import vazkii.quark.base.world.WorldGenWeights;
import vazkii.quark.base.world.config.ClusterSizeConfig;
import vazkii.quark.base.world.config.DimensionConfig;
import vazkii.quark.world.gen.UndergroundSpaceGenerator;

@LoadModule(category = ModuleCategory.WORLD)
public class CrevicesModule extends Module {

	@Config public DimensionConfig dimensions = DimensionConfig.overworld(false);
	@Config public ClusterSizeConfig spawnSettings = new ClusterSizeConfig(120, 60, 4, 20, 1, true, Type.OCEAN, Type.BEACH).setYLevels(15, 50);
	
	@Override
	public void setup() {
		WorldGenHandler.addGenerator(this, new UndergroundSpaceGenerator(dimensions, spawnSettings, 12), GenerationStep.Feature.UNDERGROUND_DECORATION, WorldGenWeights.UNDERGROUND_OPEN_ROOMS);
	}		
	
}
