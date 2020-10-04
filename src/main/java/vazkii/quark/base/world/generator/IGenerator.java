package vazkii.quark.base.world.generator;

import net.minecraft.util.math.BlockPos;
import net.minecraft.world.ChunkRegion;
import net.minecraft.world.WorldAccess;
import net.minecraft.world.gen.ChunkRandom;
import net.minecraft.world.gen.GenerationStep;
import net.minecraft.world.gen.StructureAccessor;
import net.minecraft.world.gen.chunk.ChunkGenerator;

/**
 * @author WireSegal
 * Created at 9:03 PM on 10/1/19.
 */
public interface IGenerator {
    int generate(int seedIncrement, long seed, GenerationStep.Feature stage, ChunkRegion worldIn, ChunkGenerator generator, StructureAccessor structureManager, ChunkRandom rand, BlockPos pos);

    boolean canGenerate(WorldAccess world);
}
