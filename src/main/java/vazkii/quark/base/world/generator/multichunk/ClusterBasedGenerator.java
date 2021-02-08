package vazkii.quark.base.world.generator.multichunk;

import java.util.Random;
import java.util.function.BooleanSupplier;

import net.minecraft.util.math.BlockPos;
import net.minecraft.world.gen.ChunkGenerator;
import net.minecraft.world.gen.WorldGenRegion;
import vazkii.quark.base.world.config.ClusterSizeConfig;
import vazkii.quark.base.world.config.DimensionConfig;
import vazkii.quark.base.world.generator.Generator;

public abstract class ClusterBasedGenerator extends MultiChunkFeatureGenerator {

	public final ClusterShape.Provider shapeProvider;
	
	public ClusterBasedGenerator(DimensionConfig dimConfig, ClusterSizeConfig sizeConfig, long seedXor) {
		this(dimConfig, Generator.NO_COND, sizeConfig, seedXor);
	}
	
	public ClusterBasedGenerator(DimensionConfig dimConfig, BooleanSupplier condition, ClusterSizeConfig sizeConfig, long seedXor) {
		super(dimConfig, condition, seedXor);
		this.shapeProvider = new ClusterShape.Provider(sizeConfig, seedXor);
	}

	@Override
	public int getFeatureRadius() {
		return shapeProvider.getRadius();
	}

	@Override
	public void generateChunkPart(BlockPos src, ChunkGenerator generator, Random random, BlockPos chunkCorner, WorldGenRegion world) {
		final ClusterShape shape = shapeProvider.around(src);
		final IGenerationContext context = createContext(src, generator, random, chunkCorner, world);
		
		forEachChunkBlock(chunkCorner, shape.getLowerBound(), shape.getUpperBound(), (pos) -> {
			double noise = shape.noiseDiff(pos);
			if(noise > 0)
				context.consume(pos, noise);
		});
		
		if(context instanceof IFinishableContext)
			((IFinishableContext) context).finish();
	}
	
	public abstract IGenerationContext createContext(BlockPos src, ChunkGenerator generator, Random random, BlockPos chunkCorner, WorldGenRegion world);
	
	public static abstract interface IGenerationContext {
		public void consume(BlockPos pos, double noise);
	}
	
	public static abstract interface IFinishableContext extends IGenerationContext {
		public void finish();
	}

}
