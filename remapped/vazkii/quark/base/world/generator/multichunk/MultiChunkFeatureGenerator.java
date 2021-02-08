package vazkii.quark.base.world.generator.multichunk;

import java.util.Random;
import java.util.function.BooleanSupplier;
import java.util.function.Consumer;

import net.minecraft.util.math.BlockPos;
import net.minecraft.world.ChunkRegion;
import net.minecraft.world.gen.chunk.ChunkGenerator;
import vazkii.quark.base.world.config.DimensionConfig;
import vazkii.quark.base.world.generator.Generator;

public abstract class MultiChunkFeatureGenerator extends Generator {

	private final long seedXor;
	
	public MultiChunkFeatureGenerator(DimensionConfig dimConfig, BooleanSupplier condition, long seedXor) {
		super(dimConfig, condition);
		this.seedXor = seedXor;
	}

	@Override
	public final void generateChunk(ChunkRegion world, ChunkGenerator generator, Random rand, BlockPos pos) {
		int radius = getFeatureRadius();
		if(radius <= 0)
			return;
		
		int chunkRadius = (int) Math.ceil(radius / 16.0);
		
		long worldSeed = world.getSeed();
		Random worldRandom = new Random(worldSeed);
		long xSeed = worldRandom.nextLong();
		long zSeed = worldRandom.nextLong();
		
		int chunkX = pos.getX() >> 4;
		int chunkZ = pos.getZ() >> 4;
		
		long chunkSeed = (xSeed * chunkX + zSeed * chunkZ) ^ worldSeed ^ seedXor;
		Random ourRandom = new Random(chunkSeed);
		
		for(int x = chunkX - chunkRadius; x <= chunkX + chunkRadius; x++)
			for(int z = chunkZ - chunkRadius; z <= chunkZ + chunkRadius; z++) {
				chunkSeed = (xSeed * x + zSeed * z) ^ worldSeed ^ seedXor;
				Random chunkRandom = new Random(chunkSeed);
				BlockPos chunkCorner = new BlockPos(x << 4, 0, z << 4);

				BlockPos[] sources = getSourcesInChunk(world, chunkRandom, generator, chunkCorner);
				for(BlockPos source : sources)
					if(source != null && isSourceValid(world, generator, source))
						generateChunkPart(source, generator, ourRandom, pos, world);
			}
	}
	
	public boolean isSourceValid(ChunkRegion world, ChunkGenerator generator, BlockPos pos) {
		return true;
	}
	
	public abstract int getFeatureRadius();
	
	public abstract void generateChunkPart(BlockPos src, ChunkGenerator generator, Random random, BlockPos chunkCorner, ChunkRegion world);
	
	public abstract BlockPos[] getSourcesInChunk(ChunkRegion world, Random random, ChunkGenerator generator, BlockPos chunkLeft);
	
	public void forEachChunkBlock(BlockPos chunkCorner, int minY, int maxY, Consumer<BlockPos> func) {
		minY = Math.max(1, minY);
		maxY = Math.min(255, maxY);

		BlockPos.Mutable mutable = new BlockPos.Mutable(chunkCorner.getX(), chunkCorner.getY(), chunkCorner.getZ());
		for(int x = 0; x < 16; x++)
			for(int y = minY; y < maxY; y++)
				for(int z = 0; z < 16; z++) {
					mutable.set(chunkCorner.getX() + x, chunkCorner.getY() + y, chunkCorner.getZ() + z);
					func.accept(mutable);
				}
	}
	
	public boolean isInsideChunk(BlockPos pos, int chunkX, int chunkZ) {
		int x = chunkX * 16;
		int z = chunkZ * 16;
		return pos.getX() > x && pos.getZ() > z && pos.getX() < (x + 16) && pos.getZ() < (z + 16); 
	}
	
}
