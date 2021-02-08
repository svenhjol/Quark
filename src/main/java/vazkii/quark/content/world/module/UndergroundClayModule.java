package vazkii.quark.content.world.module;

import net.minecraft.block.Blocks;
import net.minecraft.world.gen.GenerationStage.Decoration;
import vazkii.quark.base.module.LoadModule;
import vazkii.quark.base.module.ModuleCategory;
import vazkii.quark.base.module.QuarkModule;
import vazkii.quark.base.module.config.Config;
import vazkii.quark.base.world.WorldGenHandler;
import vazkii.quark.base.world.WorldGenWeights;
import vazkii.quark.base.world.config.DimensionConfig;
import vazkii.quark.base.world.config.OrePocketConfig;
import vazkii.quark.base.world.generator.Generator;
import vazkii.quark.base.world.generator.OreGenerator;

@LoadModule(category = ModuleCategory.WORLD)
public class UndergroundClayModule extends QuarkModule {

	@Config
	public static DimensionConfig dimensions = DimensionConfig.overworld(false);
	@Config
	public static OrePocketConfig oreSettings = new OrePocketConfig(20, 60, 20, 3);

	@Override
	public void setup() {
		WorldGenHandler.addGenerator(this, new OreGenerator(dimensions, oreSettings, Blocks.CLAY.getDefaultState(), OreGenerator.STONE_MATCHER, Generator.NO_COND), Decoration.UNDERGROUND_ORES, WorldGenWeights.CLAY);
	}

}
