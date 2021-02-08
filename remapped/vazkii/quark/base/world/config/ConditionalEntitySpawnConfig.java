package vazkii.quark.base.world.config;

import vazkii.quark.base.module.config.Config;
import vazkii.quark.base.module.config.ConfigFlagManager;

public class ConditionalEntitySpawnConfig extends EntitySpawnConfig {

	@Config
	public boolean enabled = true;

	public final String flag;
	
	public ConditionalEntitySpawnConfig(String flag, int spawnWeight, int minGroupSize, int maxGroupSize, BiomeTypeConfig biomes) {
		super(spawnWeight, minGroupSize, maxGroupSize, biomes);
		this.flag = flag;
	}
	
	@Override
	public void onReload(ConfigFlagManager flagManager) {
		if(module != null)
			flagManager.putFlag(module, flag, enabled);
	}
	
	@Override 
	public boolean isEnabled() {
		return enabled && super.isEnabled();
	}
	

}
