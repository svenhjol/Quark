package vazkii.quark.content.world.module;

import com.google.common.collect.ImmutableSet;

import net.minecraft.util.registry.WorldGenRegistries;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.gen.DimensionSettings;
import net.minecraft.world.gen.feature.StructureFeature;
import net.minecraft.world.gen.feature.structure.Structure;
import net.minecraft.world.gen.feature.structure.VillageConfig;
import net.minecraft.world.gen.settings.DimensionStructuresSettings;
import net.minecraft.world.gen.settings.StructureSeparationSettings;
import net.minecraftforge.event.world.BiomeLoadingEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import vazkii.arl.util.RegistryHelper;
import vazkii.quark.base.Quark;
import vazkii.quark.base.module.LoadModule;
import vazkii.quark.base.module.QuarkModule;
import vazkii.quark.base.module.ModuleCategory;
import vazkii.quark.base.module.config.Config;
import vazkii.quark.base.world.config.BiomeTypeConfig;
import vazkii.quark.content.world.gen.structure.BigDungeonStructure;

@LoadModule(category = ModuleCategory.WORLD, hasSubscriptions = true)
public class BigDungeonModule extends QuarkModule {

	@Config(description = "The chance that a big dungeon spawn candidate will be allowed to spawn. 0.2 is 20%, which is the same as the Pillager Outpost.")
	public static double spawnChance = 0.1;

	@Config
	public static String lootTable = "minecraft:chests/simple_dungeon";

	@Config 
	public static int maxRooms = 10;

	@Config
	public static double chestChance = 0.5;

	@Config
	public static BiomeTypeConfig biomeTypes = new BiomeTypeConfig(true, Biome.Category.OCEAN, Biome.Category.BEACH, Biome.Category.NETHER, Biome.Category.THEEND);

	public static final BigDungeonStructure STRUCTURE = new BigDungeonStructure(VillageConfig.field_236533_a_);
	private static StructureFeature<?, ?> feature;

	@Override
	public void construct() {
		//		new FloodFillItem(this);
		RegistryHelper.register(STRUCTURE);

		Structure.field_236365_a_.put(Quark.MOD_ID + ":big_dungeon", STRUCTURE);
	}

	@Override
	public void setup() {
		STRUCTURE.setup();	

		StructureSeparationSettings settings = new StructureSeparationSettings(20, 11, 79234823);

		ImmutableSet.of(DimensionSettings.field_242734_c, DimensionSettings.field_242735_d, DimensionSettings.field_242736_e, 
				DimensionSettings.field_242737_f, DimensionSettings.field_242738_g, DimensionSettings.field_242739_h)
		.stream()
		.map(WorldGenRegistries.NOISE_SETTINGS::getValueForKey)
		.map(DimensionSettings::getStructures)
		.map(DimensionStructuresSettings::func_236195_a_) // get map
		.forEach(m -> m.put(STRUCTURE, settings));

		feature = STRUCTURE.func_236391_a_(new VillageConfig(() -> BigDungeonStructure.startPattern, maxRooms));
	}

	@SubscribeEvent
	public void onBiomeLoad(BiomeLoadingEvent event) {
		if(biomeTypes.canSpawn(event.getName(), event.getCategory()))
			event.getGeneration().getStructures().add(() -> feature);
	}

}
