package vazkii.quark.base.world;

import java.util.Random;

import net.minecraft.util.math.BlockPos;
import net.minecraft.world.ISeedReader;
import net.minecraft.world.gen.ChunkGenerator;
import net.minecraft.world.gen.GenerationStage;
import net.minecraft.world.gen.feature.Feature;
import net.minecraft.world.gen.feature.NoFeatureConfig;
import net.minecraft.world.gen.feature.structure.StructureManager;

public class DeferedFeature extends Feature<NoFeatureConfig> {

	private final GenerationStage.Decoration stage;

	public DeferedFeature(GenerationStage.Decoration stage) {
		super(NoFeatureConfig.field_236558_a_);
		this.stage = stage;
	}

	@Override
	public boolean func_241855_a(ISeedReader seedReader, ChunkGenerator generator, Random rand, BlockPos pos, NoFeatureConfig config) {
		WorldGenHandler.generateChunk(seedReader, generator, pos, stage);
		return true;
	}

}
