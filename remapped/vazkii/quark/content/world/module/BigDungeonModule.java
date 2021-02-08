package vazkii.quark.content.world.module;

import com.google.common.collect.ImmutableSet;
import net.minecraft.util.registry.BuiltinRegistries;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.gen.chunk.ChunkGeneratorSettings;
import net.minecraft.world.gen.chunk.StructureConfig;
import net.minecraft.world.gen.chunk.StructuresConfig;
import net.minecraft.world.gen.feature.ConfiguredStructureFeature;
import net.minecraft.world.gen.feature.StructureFeature;
import net.minecraft.world.gen.feature.StructurePoolFeatureConfig;
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

	public static final BigDungeonStructure STRUCTURE = new BigDungeonStructure(StructurePoolFeatureConfig.CODEC);
	private static ConfiguredStructureFeature<?, ?> feature;

	@Override
	public void construct() {
		//		new FloodFillItem(this);
		RegistryHelper.register(STRUCTURE);

		StructureFeature.STRUCTURES.put(Quark.MOD_ID + ":big_dungeon", STRUCTURE);
	}

	@Override
	public void setup() {
		STRUCTURE.setup();	

		StructureConfig settings = new StructureConfig(20, 11, 79234823);

		ImmutableSet.of(ChunkGeneratorSettings.OVERWORLD, ChunkGeneratorSettings.AMPLIFIED, ChunkGeneratorSettings.NETHER, 
				ChunkGeneratorSettings.END, ChunkGeneratorSettings.CAVES, ChunkGeneratorSettings.FLOATING_ISLANDS)
		.stream()
		.map(BuiltinRegistries.CHUNK_GENERATOR_SETTINGS::get)
		.map(ChunkGeneratorSettings::getStructuresConfig)
		.map(StructuresConfig::getStructures) // get map
		.forEach(m -> m.put(STRUCTURE, settings));

		feature = STRUCTURE.configure(new StructurePoolFeatureConfig(() -> BigDungeonStructure.startPattern, maxRooms));
	}

	@SubscribeEvent
	public void onBiomeLoad(BiomeLoadingEvent event) {
		if(biomeTypes.canSpawn(event.getName(), event.getCategory()))
			event.getGeneration().getStructures().add(() -> feature);
	}

}
