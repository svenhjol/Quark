package vazkii.quark.base.world;

import java.util.Random;
import java.util.stream.Stream;

import com.google.common.collect.ImmutableSet;

import net.minecraft.util.math.BlockPos;
import net.minecraft.world.gen.decorator.Decorator;
import net.minecraft.world.gen.decorator.DecoratorContext;
import net.minecraft.world.gen.decorator.NopeDecoratorConfig;

public class ChunkCornerPlacement extends Decorator<NopeDecoratorConfig> {

	public ChunkCornerPlacement() {
		super(NopeDecoratorConfig.CODEC);
	}

	@Override // getPositions
	public Stream<BlockPos> func_241857_a(DecoratorContext wdc, Random random, NopeDecoratorConfig config, BlockPos pos) {
		return ImmutableSet.of(pos).stream();
	}

}
