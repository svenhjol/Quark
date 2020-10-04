package vazkii.quark.base.world;

import java.util.Random;
import java.util.stream.Stream;

import com.google.common.collect.ImmutableSet;

import net.minecraft.util.math.BlockPos;
import net.minecraft.world.WorldAccess;
import net.minecraft.world.gen.chunk.ChunkGenerator;
import net.minecraft.world.gen.decorator.Decorator;
import net.minecraft.world.gen.decorator.NopeDecoratorConfig;

public class ChunkCornerPlacement extends Decorator<NopeDecoratorConfig> {

	public ChunkCornerPlacement() {
		super(NopeDecoratorConfig.field_24891);
	}

	@Override
	public Stream<BlockPos> getPositions(WorldAccess worldIn, ChunkGenerator generatorIn, Random random, NopeDecoratorConfig configIn, BlockPos pos) {
		return ImmutableSet.of(pos).stream();
	}


}
