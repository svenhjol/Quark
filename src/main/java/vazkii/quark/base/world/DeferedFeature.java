package vazkii.quark.base.world;

import java.util.Random;

import net.minecraft.util.math.BlockPos;
import net.minecraft.world.ServerWorldAccess;
import net.minecraft.world.gen.GenerationStep;
import net.minecraft.world.gen.StructureAccessor;
import net.minecraft.world.gen.chunk.ChunkGenerator;
import net.minecraft.world.gen.feature.DefaultFeatureConfig;
import net.minecraft.world.gen.feature.Feature;

public class DeferedFeature extends Feature<DefaultFeatureConfig> {

	private final GenerationStep.Feature stage;

	public DeferedFeature(GenerationStep.Feature stage) {
		super(DefaultFeatureConfig.CODEC);
		this.stage = stage;
	}

	@Override // place
	public boolean func_230362_a_(ServerWorldAccess seedReader, StructureAccessor structureManager, ChunkGenerator generator, Random rand, BlockPos pos, DefaultFeatureConfig config) {
		WorldGenHandler.generateChunk(seedReader, structureManager, generator, pos, stage);
		return true;
	}

}
