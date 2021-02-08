package vazkii.quark.content.world.config;

import net.minecraft.world.biome.Biome;
import vazkii.quark.base.world.config.ClusterSizeConfig;
import vazkii.quark.content.world.gen.underground.UndergroundBiome;

public class UndergroundBiomeConfig extends ClusterSizeConfig {

	public final UndergroundBiome biomeObj;

	public UndergroundBiomeConfig(UndergroundBiome biomeObj, int rarity, boolean isBlacklist, Biome.Category... categories) {
		super(rarity, 26, 14, 14, 6, isBlacklist, categories);
		this.biomeObj = biomeObj;
	}

	public UndergroundBiomeConfig(UndergroundBiome biomeObj, int rarity, Biome.Category... categories) {
		this(biomeObj, rarity, false, categories);
	}
	
	public UndergroundBiomeConfig setDefaultSize(int horizontal, int vertical, int horizontalVariation, int verticalVariation) {
		this.horizontalSize = horizontal;
		this.verticalSize = vertical;
		this.horizontalVariation = horizontalVariation;
		this.verticalVariation = verticalVariation;
		return this;
	}

}
