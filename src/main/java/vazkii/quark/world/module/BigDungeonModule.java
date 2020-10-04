package vazkii.quark.world.module;

import com.google.common.collect.ImmutableSet;

import net.minecraft.world.biome.Biome;
import net.minecraft.world.gen.chunk.ChunkGeneratorType;
import net.minecraft.world.gen.chunk.StructureConfig;
import net.minecraft.world.gen.feature.ConfiguredStructureFeature;
import net.minecraft.world.gen.feature.DefaultFeatureConfig;
import net.minecraft.world.gen.feature.StructureFeature;
import net.minecraftforge.common.BiomeDictionary.Type;
import net.minecraftforge.registries.ForgeRegistries;
import vazkii.arl.util.RegistryHelper;
import vazkii.quark.base.Quark;
import vazkii.quark.base.module.LoadModule;
import vazkii.quark.base.module.Module;
import vazkii.quark.base.module.ModuleCategory;
import vazkii.quark.base.module.config.Config;
import vazkii.quark.base.world.config.BiomeTypeConfig;
import vazkii.quark.world.gen.structure.BigDungeonStructure;

@LoadModule(category = ModuleCategory.WORLD)
public class BigDungeonModule extends Module {

	@Config(description = "The chance that a big dungeon spawn candidate will be allowed to spawn. 0.2 is 20%, which is the same as the Pillager Outpost.")
	public static double spawnChance = 0.1;

	@Config
	public static String lootTable = "minecraft:chests/simple_dungeon";

	@Config 
	public static int maxRooms = 10;

	@Config
	public static double chestChance = 0.5;

	@Config
	public static BiomeTypeConfig biomeTypes = new BiomeTypeConfig(true, Type.OCEAN, Type.BEACH, Type.NETHER, Type.END);

	public static final BigDungeonStructure STRUCTURE = new BigDungeonStructure(DefaultFeatureConfig.CODEC);
	
	@Override
	public void construct() {
		//		new FloodFillItem(this);
		RegistryHelper.register(STRUCTURE);
		
		StructureFeature.STRUCTURES.put(Quark.MOD_ID + ":big_dungeon", STRUCTURE);
	}

	@Override
	@SuppressWarnings({ "rawtypes" })
	public void setup() {
		STRUCTURE.setup();	
		
		StructureConfig settings = new StructureConfig(20, 11, 79234823);
		
		// Register separation settings for big dungeon in the settings presets
		ImmutableSet.of(ChunkGeneratorType.Preset.OVERWORLD, ChunkGeneratorType.Preset.AMPLIFIED, ChunkGeneratorType.Preset.NETHER, 
				ChunkGeneratorType.Preset.END, ChunkGeneratorType.Preset.CAVES, ChunkGeneratorType.Preset.FLOATING_ISLANDS)
		.forEach(p -> p.getChunkGeneratorType().getConfig().getStructures().put(STRUCTURE, settings));

		ConfiguredStructureFeature structure = STRUCTURE.configure(DefaultFeatureConfig.DEFAULT);

		if(enabled) 
			for(Biome b : ForgeRegistries.BIOMES.getValues()) { 
				if(biomeTypes.canSpawn(b))
					b.addStructureFeature(structure);
			}
	}

}
