package vazkii.quark.content.world.module;

import net.minecraft.world.biome.Biome;
import net.minecraft.world.gen.GenerationStage;
import vazkii.quark.base.module.LoadModule;
import vazkii.quark.base.module.QuarkModule;
import vazkii.quark.base.module.ModuleCategory;
import vazkii.quark.base.module.config.Config;
import vazkii.quark.base.world.WorldGenHandler;
import vazkii.quark.base.world.WorldGenWeights;
import vazkii.quark.base.world.config.ClusterSizeConfig;
import vazkii.quark.base.world.config.DimensionConfig;
import vazkii.quark.content.world.gen.UndergroundSpaceGenerator;

@LoadModule(category = ModuleCategory.WORLD)
public class MegaCavesModule extends QuarkModule {

	@Config public DimensionConfig dimensions = DimensionConfig.overworld(false);
	@Config public ClusterSizeConfig spawnSettings = new ClusterSizeConfig(800, 80, 25, 30, 10, true, Biome.Category.OCEAN, Biome.Category.BEACH)
			.setYLevels(10, 20);
	
	@Override
	public void setup() {
		WorldGenHandler.addGenerator(this, new UndergroundSpaceGenerator(dimensions, spawnSettings, 4), GenerationStage.Decoration.UNDERGROUND_DECORATION, WorldGenWeights.UNDERGROUND_OPEN_ROOMS);
	}
	
}
