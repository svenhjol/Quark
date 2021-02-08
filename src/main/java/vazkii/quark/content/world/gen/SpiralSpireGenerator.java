package vazkii.quark.content.world.gen;

import java.util.Random;
import java.util.function.Supplier;

import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.vector.Vector3i;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.gen.ChunkGenerator;
import net.minecraft.world.gen.WorldGenRegion;
import vazkii.quark.base.world.config.DimensionConfig;
import vazkii.quark.base.world.generator.multichunk.MultiChunkFeatureGenerator;
import vazkii.quark.content.world.module.SpiralSpiresModule;

public class SpiralSpireGenerator extends MultiChunkFeatureGenerator {

	public SpiralSpireGenerator(DimensionConfig dimConfig) {
		super(dimConfig, NO_COND, 1892);
	}

	@Override
	public int getFeatureRadius() {
		return SpiralSpiresModule.radius;
	}

	@Override
	public void generateChunkPart(BlockPos src, ChunkGenerator generator, Random random, BlockPos chunkCorner, WorldGenRegion world) {
		double dist = (chunkCorner.distanceSq(src)) / ((16 * SpiralSpiresModule.radius) * (16 * SpiralSpiresModule.radius));
		if(dist > 0.5 && random.nextDouble() < (1.5F - dist))
			return;
		
		BlockPos pos = chunkCorner.add(random.nextInt(16), 256, random.nextInt(16));
		Biome biome = getBiome(world, pos);
		if(!SpiralSpiresModule.biomes.canSpawn(biome))
			return;
		
		while(world.getBlockState(pos).getBlock() != Blocks.END_STONE) {
			pos = pos.down();
			
			if(pos.getY() < 10)
				return;
		}
		
		makeSpike(world, generator, random, pos);
	}

	@Override
	public BlockPos[] getSourcesInChunk(WorldGenRegion world, Random random, ChunkGenerator generator, BlockPos chunkCorner) {
		if(!chunkCorner.withinDistance(Vector3i.NULL_VECTOR, 1050) && SpiralSpiresModule.rarity > 0 && random.nextInt(SpiralSpiresModule.rarity) == 0)
			return new BlockPos[] { chunkCorner };
		
		return new BlockPos[0];
	}
	
	public void makeSpike(WorldGenRegion world, ChunkGenerator chunk, Random rand, BlockPos pos) {
		int height = 50 + rand.nextInt(20);
		double heightComposition = 5 + rand.nextDouble() * 1;
		int start = -5;
		int y = start;
		
		Supplier<BlockState> prov = () -> (rand.nextFloat() < 0.05 ? Blocks.CRYING_OBSIDIAN : Blocks.OBSIDIAN).getDefaultState();
		
		for(; y < height; y++) {
			BlockPos test = pos.up(y);
			BlockState state = world.getBlockState(test);
			if(!state.isAir() && !(state.getBlock() == Blocks.END_STONE || state.getBlock() == Blocks.CRYING_OBSIDIAN || state.getBlock() == Blocks.OBSIDIAN || state.getBlock() == SpiralSpiresModule.myalite_crystal))
				return;
		}
		y = start;
		
		for(; y < height; y++) {
			if(y < 0 && !world.getBlockState(pos.up(y)).isSolid())
				continue;
			
			double r = Math.abs(((Math.PI / 2) - Math.atan((Math.max(0, y) + 0.5) / (height / heightComposition))) * 4);
			int ri = (int) Math.ceil(r);
			
			for(int i = -ri; i < ri; i++)
				for(int j = -ri; j < ri; j++) 
					if(i * i + j * j < ri * ri)
						world.setBlockState(pos.add(i, y, j), prov.get(), 2);
		}
		
		int steps = 80 + rand.nextInt(30);
		int substeps = 10;
		
		int fullSteps = steps * substeps;
		int deteroirate = (int) ((0.5 + rand.nextDouble() * 0.3) * fullSteps);
		
		double spin = 0.12 + rand.nextDouble() * 0.16;
		double spread = 0.12 + rand.nextDouble() * 0.04;
		double upwardMotion = rand.nextDouble() * 0.2;
		
		if(rand.nextBoolean())
			spin *= -1;
		
		BlockState state = SpiralSpiresModule.myalite_crystal.getDefaultState();
		
		for(int i = 0; i < fullSteps; i++) {
			double t = (double) i * spin;
			int x = (int) (Math.sin(t / substeps) * i * spread / substeps);
			int z = (int) (Math.cos(t / substeps) * i * spread / substeps);
			int yp = y + (int) Math.round(((double) i / substeps) * upwardMotion);
			
			BlockPos next = pos.add(x, yp, z);
			
			float chance = 1F;
			if(i > deteroirate) {
				int deterStep = i - deteroirate;
				int maxSteps = (fullSteps - deteroirate);
				chance -= (float) deterStep / (float) maxSteps;
			}
			
			if(rand.nextFloat() < chance)
				world.setBlockState(next, state, 2);
		}
	}

}
