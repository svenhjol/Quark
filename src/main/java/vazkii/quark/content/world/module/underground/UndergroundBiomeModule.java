package vazkii.quark.content.world.module.underground;

import net.minecraft.world.gen.GenerationStage.Decoration;
import vazkii.quark.base.module.QuarkModule;
import vazkii.quark.base.module.config.Config;
import vazkii.quark.base.world.WorldGenHandler;
import vazkii.quark.base.world.WorldGenWeights;
import vazkii.quark.base.world.generator.Generator;
import vazkii.quark.content.world.config.UndergroundBiomeConfig;
import vazkii.quark.content.world.gen.UndergroundBiomeGenerator;

public abstract class UndergroundBiomeModule extends QuarkModule {

	@Config
	public UndergroundBiomeConfig biomeSettings;

	@Override
	public void construct() {
		biomeSettings = getBiomeConfig();
	}

	@Override
	public void setup() {
		WorldGenHandler.addGenerator(this, new UndergroundBiomeGenerator(biomeSettings, getBiomeName()), Decoration.UNDERGROUND_DECORATION, WorldGenWeights.UNDERGROUND_BIOMES);
	}
	
	protected abstract String getBiomeName();

	protected abstract UndergroundBiomeConfig getBiomeConfig();

}
