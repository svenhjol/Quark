package vazkii.quark.content.world.module;

import net.minecraft.block.Block;
import net.minecraft.world.gen.GenerationStage.Decoration;
import vazkii.quark.base.module.LoadModule;
import vazkii.quark.base.module.ModuleCategory;
import vazkii.quark.base.module.QuarkModule;
import vazkii.quark.base.module.config.Config;
import vazkii.quark.base.world.WorldGenHandler;
import vazkii.quark.base.world.WorldGenWeights;
import vazkii.quark.content.world.block.ChorusVegetationBlock;
import vazkii.quark.content.world.gen.ChorusVegetationGenerator;

@LoadModule(category = ModuleCategory.WORLD)
public class ChorusVegetationModule extends QuarkModule {

	@Config public static int rarity = 150;
	@Config public static int radius = 7;
	@Config public static int chunkAttempts = 120;
	@Config public static double highlandsChance = 1.0;
	@Config public static double midlandsChance = 0.2;
	@Config public static double otherEndBiomesChance = 0;
	
	@Config public static double passiveTeleportChance = 0.2;
	@Config public static double endermiteSpawnChance = 0.01;
	@Config public static double teleportDuplicationChance = 0.01;

	public static Block chorus_weeds, chorus_twist;
	
	@Override
	public void construct() {
		chorus_weeds = new ChorusVegetationBlock("chorus_weeds", this, true);
		chorus_twist = new ChorusVegetationBlock("chorus_twist", this, false);
	}
	
	@Override
	public void setup() {
		WorldGenHandler.addGenerator(this, new ChorusVegetationGenerator(), Decoration.VEGETAL_DECORATION, WorldGenWeights.CHORUS_VEGETATION);
	}
	
}
