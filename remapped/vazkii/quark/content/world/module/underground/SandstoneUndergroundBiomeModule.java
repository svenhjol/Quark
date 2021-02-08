package vazkii.quark.content.world.module.underground;

import net.minecraft.world.biome.Biome;
import vazkii.quark.base.module.LoadModule;
import vazkii.quark.base.module.ModuleCategory;
import vazkii.quark.content.world.config.UndergroundBiomeConfig;
import vazkii.quark.content.world.gen.underground.SandstoneUndergroundBiome;

@LoadModule(category = ModuleCategory.WORLD)
public class SandstoneUndergroundBiomeModule extends UndergroundBiomeModule {

	@Override
	protected UndergroundBiomeConfig getBiomeConfig() {
		return new UndergroundBiomeConfig(new SandstoneUndergroundBiome(), 80, Biome.Category.DESERT);
	}
	
	@Override
	protected String getBiomeName() {
		return "sandstone";
	}

}
