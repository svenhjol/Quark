package vazkii.quark.base.world.config;

import vazkii.quark.base.module.config.Config;

public class CostSensitiveEntitySpawnConfig extends EntitySpawnConfig {

	@Config 
	public double maxCost;
	
	@Config 
	public double spawnCost;
	
	public CostSensitiveEntitySpawnConfig(int spawnWeight, int minGroupSize, int maxGroupSize, double maxCost, double spawnCost, IBiomeConfig biomes) {
		super(spawnWeight, minGroupSize, maxGroupSize, biomes);
		this.maxCost = maxCost;
		this.spawnCost = spawnCost;
	}

}
