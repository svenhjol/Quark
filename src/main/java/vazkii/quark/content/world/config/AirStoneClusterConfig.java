package vazkii.quark.content.world.config;

import vazkii.quark.base.module.config.Config;
import vazkii.quark.base.world.config.DimensionConfig;
import vazkii.quark.base.world.config.IBiomeConfig;

public class AirStoneClusterConfig extends BigStoneClusterConfig {

	@Config public boolean generateInAir = true;
	
	public AirStoneClusterConfig(DimensionConfig dimensions, int clusterSize, int sizeVariation, int rarity, int minYLevel, int maxYLevel, IBiomeConfig biomes) {
		super(dimensions, clusterSize, sizeVariation, rarity, minYLevel, maxYLevel, biomes);
	}

	
}
