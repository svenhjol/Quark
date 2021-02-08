package vazkii.quark.base.world.generator;

import java.util.List;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.ChunkRegion;
import net.minecraft.world.ServerWorldAccess;
import net.minecraft.world.gen.ChunkRandom;
import net.minecraft.world.gen.GenerationStep;
import net.minecraft.world.gen.chunk.ChunkGenerator;

/**
 * @author WireSegal
 * Created at 9:02 PM on 10/1/19.
 */
public class CombinedGenerator implements IGenerator {

    private List<? extends IGenerator> children;

    public CombinedGenerator(List<? extends IGenerator> children) {
        this.children = children;
    }

    @Override
    public int generate(int seedIncrement, long seed, GenerationStep.Feature stage, ChunkRegion worldIn, ChunkGenerator generator, ChunkRandom rand, BlockPos pos) {
        for (IGenerator child : children) {
            if (child.canGenerate(worldIn))
                seedIncrement = child.generate(seedIncrement, seed, stage, worldIn, generator, rand, pos);
        }
        return seedIncrement;
    }

    @Override
    public boolean canGenerate(ServerWorldAccess world) {
        return children.stream().anyMatch((it) -> it.canGenerate(world));
    }
}
