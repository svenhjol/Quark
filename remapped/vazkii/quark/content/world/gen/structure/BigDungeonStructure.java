package vazkii.quark.content.world.gen.structure;

import java.util.List;
import java.util.Set;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Lists;
import com.mojang.serialization.Codec;

import net.minecraft.entity.EntityType;
import net.minecraft.structure.StructureManager;
import net.minecraft.structure.pool.StructurePool;
import net.minecraft.structure.pool.StructurePool.Projection;
import net.minecraft.structure.processor.StructureProcessorType;
import net.minecraft.util.math.BlockBox;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.registry.DynamicRegistryManager;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.SpawnSettings;
import net.minecraft.world.biome.source.BiomeSource;
import net.minecraft.world.gen.ChunkRandom;
import net.minecraft.world.gen.GenerationStep.Feature;
import net.minecraft.world.gen.chunk.ChunkGenerator;
import net.minecraft.world.gen.chunk.StructureConfig;
import net.minecraft.world.gen.feature.JigsawFeature;
import net.minecraft.world.gen.feature.StructureFeature;
import net.minecraft.world.gen.feature.StructurePoolFeatureConfig;
import vazkii.quark.base.Quark;
import vazkii.quark.base.world.JigsawRegistryHelper;
import vazkii.quark.content.world.gen.structure.processor.BigDungeonChestProcessor;
import vazkii.quark.content.world.gen.structure.processor.BigDungeonSpawnerProcessor;
import vazkii.quark.content.world.module.BigDungeonModule;

public class BigDungeonStructure extends JigsawFeature {

	private static final List<SpawnSettings.SpawnEntry> ENEMIES = Lists.newArrayList(
			new SpawnSettings.SpawnEntry(EntityType.ZOMBIE, 8, 1, 3),
			new SpawnSettings.SpawnEntry(EntityType.SKELETON, 8, 1, 3),
			new SpawnSettings.SpawnEntry(EntityType.CREEPER, 8, 1, 3),
			new SpawnSettings.SpawnEntry(EntityType.WITCH, 4, 1, 1),
			new SpawnSettings.SpawnEntry(EntityType.ILLUSIONER, 10, 1, 1)
			);

	private static final String NAMESPACE = "big_dungeon";

	private static final String STARTS_DIR = "starts";
	private static final Set<String> STARTS = ImmutableSet.of(
			"3x3_pillars", "3x3_tnt", "3x3_water",
			"plus_barricade", "plus_ores", "plus_plain",
			"triplex_3sect", "triplex_lava", "triplex_plain");

	private static final String ROOMS_DIR = "rooms";
	private static final Set<String> ROOMS = ImmutableSet.of(
			"4room_plain", "4room_trapped",
			"ascend_intersection", "ascend_ruined", "ascend_plain",
			"climb_parkour", "climb_redstone", "climb_plain",
			"double_hall_plain", "double_hall_silverfish",
			"laddered_bridge", "laddered_tnt", "laddered_plain",
			"triple_library", "triple_plain",
			"connector_base", "connector_bush", "connector_fountain", "connector_melon", "connector_room");

	private static final String CORRIDORS_DIR = "corridors";
	private static final Set<String> CORRIDORS = ImmutableSet.of(
			"forward_cobweb", "forward_plain",
			"left_cobweb", "left_plain",
			"right_cobweb", "right_plain",
			"t_cobweb", "t_plain");

	private static final String ENDPOINT = "misc/endpoint";

	public static StructurePool startPattern;
	
	private static final BigDungeonChestProcessor CHEST_PROCESSOR = new BigDungeonChestProcessor();
	private static final BigDungeonSpawnerProcessor SPAWN_PROCESSOR = new BigDungeonSpawnerProcessor();

	private static Codec<BigDungeonChestProcessor> CHEST_CODEC = Codec.unit(CHEST_PROCESSOR);
	private static Codec<BigDungeonSpawnerProcessor> SPAWN_CODEC = Codec.unit(SPAWN_PROCESSOR);

	public static StructureProcessorType<BigDungeonChestProcessor> CHEST_PROCESSOR_TYPE = () -> CHEST_CODEC;
	public static StructureProcessorType<BigDungeonSpawnerProcessor> SPAWN_PROCESSOR_TYPE = () -> SPAWN_CODEC;

	static {
		startPattern = JigsawRegistryHelper.pool(NAMESPACE, STARTS_DIR)
		.processor(CHEST_PROCESSOR)
		.addMult(STARTS_DIR, STARTS, 1)
		.register(Projection.RIGID);

		JigsawRegistryHelper.pool(NAMESPACE, ROOMS_DIR)
		.processor(CHEST_PROCESSOR, SPAWN_PROCESSOR)
		.addMult(ROOMS_DIR, ROOMS, 1)
		.register(Projection.RIGID);

		JigsawRegistryHelper.pool(NAMESPACE, CORRIDORS_DIR)
		.addMult(CORRIDORS_DIR, CORRIDORS, 1)
		.register(Projection.RIGID);

		final int roomWeight = 100;
		final int corridorWeight = 120;
		final double endpointWeightMult = 1.2;

		JigsawRegistryHelper.pool(NAMESPACE, "rooms_or_endpoint")
		.processor(CHEST_PROCESSOR, SPAWN_PROCESSOR)
		.addMult(ROOMS_DIR, ROOMS, roomWeight)
		.addMult(CORRIDORS_DIR, CORRIDORS, corridorWeight)
		.add(ENDPOINT, (int) ((ROOMS.size() * roomWeight + CORRIDORS.size() * corridorWeight) * endpointWeightMult))
		.register(Projection.RIGID);
	}

	public BigDungeonStructure(Codec<StructurePoolFeatureConfig> codec) {
		super(codec, 40, false, true);
		setRegistryName(Quark.MOD_ID, NAMESPACE);
	}

	public void setup() {
		Registry.register(Registry.STRUCTURE_PROCESSOR, Quark.MOD_ID + ":big_dungeon_chest", CHEST_PROCESSOR_TYPE);
		Registry.register(Registry.STRUCTURE_PROCESSOR, Quark.MOD_ID + ":big_dungeon_spawner", SPAWN_PROCESSOR_TYPE);
	}

	@Override
	public List<SpawnSettings.SpawnEntry> getMonsterSpawns() {
		return ENEMIES;
	}

	@Override
	public Feature getGenerationStep() {
		return Feature.UNDERGROUND_STRUCTURES;
	}

	@Override // hasStartAt
	protected boolean func_230363_a_(ChunkGenerator chunkGen, BiomeSource biomeProvider, long seed, ChunkRandom rand, int chunkPosX, int chunkPosZ, Biome biome, ChunkPos chunkpos, StructurePoolFeatureConfig config) { 
		if(chunkPosX == chunkpos.x && chunkPosZ == chunkpos.z && chunkGen.getStructuresConfig().getStructures().containsKey(this) && BigDungeonModule.biomeTypes.canSpawn(biome)) {
			int i = chunkPosX >> 4;
			int j = chunkPosZ >> 4;
			rand.setSeed((long)(i ^ j << 4) ^ seed);
			rand.nextInt();
			return rand.nextDouble() < BigDungeonModule.spawnChance;
		}

		return !func_242782_a(chunkGen, seed, rand, chunkPosX, chunkPosZ);
	}

	// copy from PillagerOutpostStructure, seems to check village distancing
	private boolean func_242782_a(ChunkGenerator p_242782_1_, long p_242782_2_, ChunkRandom p_242782_4_, int p_242782_5_, int p_242782_6_) {
		StructureConfig structureseparationsettings = p_242782_1_.getStructuresConfig().getForType(StructureFeature.VILLAGE);
		if (structureseparationsettings == null) {
			return false;
		} else {
			for(int i = p_242782_5_ - 10; i <= p_242782_5_ + 10; ++i) {
				for(int j = p_242782_6_ - 10; j <= p_242782_6_ + 10; ++j) {
					ChunkPos chunkpos = StructureFeature.VILLAGE.getStartChunk(structureseparationsettings, p_242782_2_, p_242782_4_, i, j);
					if (i == chunkpos.x && j == chunkpos.z) {
						return true;
					}
				}
			}

			return false;
		}
	}

	@Override
	public StructureStartFactory<StructurePoolFeatureConfig> getStructureStartFactory() {
		return (s, x, z, bb, r, sd) -> new BigDungeonStructure.Start(this, x, z, bb, r, sd);
	}

	@Override
	public String getName() {
		return getRegistryName().toString();
	}

	//	@Override
	//	public int getSize() {
	//		return (int) Math.ceil((double) BigDungeonModule.maxRooms / 1.5);
	//	}

	public static class Start extends JigsawFeature.Start {

		public Start(JigsawFeature structureIn, int chunkX, int chunkZ, BlockBox boundsIn, int referenceIn, long seed) {
			super(structureIn, chunkX, chunkZ, boundsIn, referenceIn, seed);
		}

		@Override // init
		public void init(DynamicRegistryManager dynreg, ChunkGenerator generator, StructureManager templateManagerIn, int chunkX, int chunkZ, Biome biomeIn, StructurePoolFeatureConfig config) {
			super.init(dynreg, generator, templateManagerIn, chunkX, chunkZ, biomeIn, config);

			int maxTop = 60;
			if(boundingBox.maxY >= maxTop) {
				int shift = 5 + (boundingBox.maxY - maxTop);
				boundingBox.move(0, -shift, 0);
				children.forEach(p -> p.translate(0, -shift, 0));
			}

			if(boundingBox.minY < 6) {
				int shift = 6 - boundingBox.minY;
				boundingBox.move(0, shift, 0);
				children.forEach(p -> p.translate(0, shift, 0));
			}

			children.removeIf(c -> c.getBoundingBox().maxY >= maxTop);			
		}

	}

//	public static class Piece extends AbstractVillagePiece {
//
//		public static IStructurePieceType PIECE_TYPE = Registry.register(Registry.STRUCTURE_PIECE, "bigdungeon", BigDungeonStructure.Piece::new);
//
//		public Piece(TemplateManager templateManagerIn, JigsawPiece jigsawPieceIn, BlockPos posIn, int p_i50560_4_, Rotation rotationIn, MutableBoundingBox boundsIn) {
//			super(PIECE_TYPE, templateManagerIn, jigsawPieceIn, posIn, p_i50560_4_, rotationIn, boundsIn);
//		}
//
//		public Piece(TemplateManager templateManagerIn, CompoundNBT nbt) {
//			super(templateManagerIn, nbt, PIECE_TYPE);
//		}
//
//	}

}
