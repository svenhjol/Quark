package vazkii.quark.base.world;

import java.util.Random;
import java.util.stream.Stream;

import com.google.common.collect.ImmutableSet;

import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IWorld;
import net.minecraft.world.gen.ChunkGenerator;
import net.minecraft.world.gen.feature.WorldDecoratingHelper;
import net.minecraft.world.gen.placement.NoPlacementConfig;
import net.minecraft.world.gen.placement.Placement;

public class ChunkCornerPlacement extends Placement<NoPlacementConfig> {

	public ChunkCornerPlacement() {
		super(NoPlacementConfig.field_236555_a_);
	}

	@Override // getPositions
	public Stream<BlockPos> func_241857_a(WorldDecoratingHelper wdc, Random random, NoPlacementConfig config, BlockPos pos) {
		return ImmutableSet.of(pos).stream();
	}

}
