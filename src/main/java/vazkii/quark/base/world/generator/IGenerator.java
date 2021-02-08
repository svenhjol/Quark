package vazkii.quark.base.world.generator;

import net.minecraft.util.SharedSeedRandom;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IServerWorld;
import net.minecraft.world.gen.ChunkGenerator;
import net.minecraft.world.gen.GenerationStage;
import net.minecraft.world.gen.WorldGenRegion;

/**
 * @author WireSegal
 * Created at 9:03 PM on 10/1/19.
 */
public interface IGenerator {
    int generate(int seedIncrement, long seed, GenerationStage.Decoration stage, WorldGenRegion worldIn, ChunkGenerator generator, SharedSeedRandom rand, BlockPos pos);

    boolean canGenerate(IServerWorld world);
}
