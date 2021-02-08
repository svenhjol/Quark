package vazkii.quark.base.world.generator;

import java.util.Random;
import java.util.function.BooleanSupplier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.ChunkRegion;
import net.minecraft.world.ServerWorldAccess;
import net.minecraft.world.WorldAccess;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.gen.ChunkRandom;
import net.minecraft.world.gen.GenerationStep;
import net.minecraft.world.gen.chunk.ChunkGenerator;
import vazkii.quark.base.world.config.DimensionConfig;

public abstract class Generator implements IGenerator {
	
	public static final BooleanSupplier NO_COND = () -> true;
	
	public final DimensionConfig dimConfig;
	private final BooleanSupplier condition;
	
	public Generator(DimensionConfig dimConfig) {
		this(dimConfig, NO_COND);
	}
	
	public Generator(DimensionConfig dimConfig, BooleanSupplier condition) {
		this.dimConfig = dimConfig;
		this.condition = condition;
	}

	@Override
	public final int generate(int seedIncrement, long seed, GenerationStep.Feature stage, ChunkRegion worldIn, ChunkGenerator generator, ChunkRandom rand, BlockPos pos) {
		rand.setDecoratorSeed(seed, seedIncrement, stage.ordinal());
		generateChunk(worldIn, generator, rand, pos);
		return seedIncrement + 1;
	}

	public abstract void generateChunk(ChunkRegion worldIn, ChunkGenerator generator, Random rand, BlockPos pos);

	@Override
	public boolean canGenerate(ServerWorldAccess world) {
		return condition.getAsBoolean() && dimConfig.canSpawnHere(world.toServerWorld());
	}
	
	public Biome getBiome(WorldAccess world, BlockPos pos) {
		return world.getBiomeAccess().getBiome(pos);
	}
	
	protected boolean isNether(WorldAccess world) {
		return world.getDimension().isUltrawarm();
	}
	
}
