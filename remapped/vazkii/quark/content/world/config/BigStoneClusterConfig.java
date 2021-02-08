package vazkii.quark.content.world.config;

import net.minecraft.world.biome.Biome;
import vazkii.quark.base.module.config.Config;
import vazkii.quark.base.world.config.BiomeTypeConfig;
import vazkii.quark.base.world.config.ClusterSizeConfig;
import vazkii.quark.base.world.config.DimensionConfig;
import vazkii.quark.base.world.config.IBiomeConfig;

public class BigStoneClusterConfig extends ClusterSizeConfig {

	@Config
	public boolean enabled = true;

	public BigStoneClusterConfig(Biome.Category... types) {
		this(DimensionConfig.overworld(false), 14, 9, 4, 20, 80, new BiomeTypeConfig(false, types));
	}

	public BigStoneClusterConfig(DimensionConfig dimensions, int clusterSize, int sizeVariation, int rarity, int minYLevel, int maxYLevel, IBiomeConfig biomes) {
		super(rarity, clusterSize, clusterSize, sizeVariation, sizeVariation, biomes);
		this.dimensions = dimensions;
		
		this.minYLevel = minYLevel;
		this.maxYLevel = maxYLevel;
	}
	
	public BigStoneClusterConfig setVertical(int vertical, int verticalVariation) {
		this.verticalSize = vertical;
		this.verticalVariation = verticalVariation;
		return this;
	}
	

}
