package vazkii.quark.content.world.module.underground;

import net.minecraft.world.biome.Biome;
import vazkii.quark.base.module.LoadModule;
import vazkii.quark.base.module.ModuleCategory;
import vazkii.quark.content.world.config.UndergroundBiomeConfig;
import vazkii.quark.content.world.gen.underground.SlimeUndergroundBiome;

@LoadModule(category = ModuleCategory.WORLD)
public class SlimeUndergroundBiomeModule extends UndergroundBiomeModule {

	@Override
	protected UndergroundBiomeConfig getBiomeConfig() {
		return new UndergroundBiomeConfig(new SlimeUndergroundBiome(), 120, Biome.Category.SWAMP);
	}
	
	@Override
	protected String getBiomeName() {
		return "slime";
	}

}
