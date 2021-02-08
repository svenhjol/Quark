package vazkii.quark.base.world;

import java.util.Random;

import net.minecraft.util.math.BlockPos;
import net.minecraft.world.StructureWorldAccess;
import net.minecraft.world.gen.GenerationStep;
import net.minecraft.world.gen.chunk.ChunkGenerator;
import net.minecraft.world.gen.feature.DefaultFeatureConfig;
import net.minecraft.world.gen.feature.Feature;

public class DeferedFeature extends Feature<DefaultFeatureConfig> {

	private final GenerationStep.Feature stage;

	public DeferedFeature(GenerationStep.Feature stage) {
		super(DefaultFeatureConfig.CODEC);
		this.stage = stage;
	}

	@Override
	public boolean func_241855_a(StructureWorldAccess seedReader, ChunkGenerator generator, Random rand, BlockPos pos, DefaultFeatureConfig config) {
		WorldGenHandler.generateChunk(seedReader, generator, pos, stage);
		return true;
	}

}
