package vazkii.quark.content.world.gen;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Random;

import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.gen.ChunkGenerator;
import net.minecraft.world.gen.WorldGenRegion;
import vazkii.quark.base.world.generator.multichunk.ClusterBasedGenerator;
import vazkii.quark.content.world.config.UndergroundBiomeConfig;

public class UndergroundBiomeGenerator extends ClusterBasedGenerator {

	public final UndergroundBiomeConfig info;

	public UndergroundBiomeGenerator(UndergroundBiomeConfig info, String name) {
		super(info.dimensions, info, name.hashCode());
		this.info = info;
	}

	@Override
	public int getFeatureRadius() {
		return info.horizontalSize + info.horizontalVariation;
	}

	@Override
	public BlockPos[] getSourcesInChunk(WorldGenRegion world, Random random, ChunkGenerator generator, BlockPos chunkCorner) {
		if(info.rarity > 0 && random.nextInt(info.rarity) == 0) {
			return new BlockPos[] {
					chunkCorner.add(random.nextInt(16), info.minYLevel + random.nextInt(info.maxYLevel - info.minYLevel), random.nextInt(16))
			};
		}

		return new BlockPos[0];
	}
	
	@Override
	public IGenerationContext createContext(BlockPos src, ChunkGenerator generator, Random random, BlockPos chunkCorner, WorldGenRegion world) {
		return new Context(world, src, generator, random, info);
	}

	@Override
	public boolean isSourceValid(WorldGenRegion world, ChunkGenerator generator, BlockPos pos) {
		Biome biome = getBiome(world, pos);
		return info.biomes.canSpawn(biome);
	}
	
	@Override
	public String toString() {
		return "UndergroundBiomeGenerator[" + info.biomeObj + "]";
	}

	public static class Context implements IFinishableContext {

		public final WorldGenRegion world;
		public final BlockPos source;
		public final ChunkGenerator generator;
		public final Random random;
		public final UndergroundBiomeConfig info;

		public final List<BlockPos> floorList = new LinkedList<>();
		public final List<BlockPos> ceilingList = new LinkedList<>();
		public final List<BlockPos> insideList = new LinkedList<>();

		public final Map<BlockPos, Direction> wallMap = new HashMap<>();
		
		public Context(WorldGenRegion world, BlockPos source, ChunkGenerator generator, Random random, UndergroundBiomeConfig info) {
			this.world = world;
			this.source = source;
			this.generator = generator;
			this.random = random;
			this.info = info;
		}

		@Override
		public void consume(BlockPos pos, double noise) {
			info.biomeObj.fill(this, pos);			
		}

		@Override
		public void finish() {
			floorList.forEach(pos -> info.biomeObj.finalFloorPass(this, pos));
			ceilingList.forEach(pos -> info.biomeObj.finalCeilingPass(this, pos));
			wallMap.keySet().forEach(pos -> info.biomeObj.finalWallPass(this, pos));
			insideList.forEach(pos -> info.biomeObj.finalInsidePass(this, pos));			
		}
		
	}
}
