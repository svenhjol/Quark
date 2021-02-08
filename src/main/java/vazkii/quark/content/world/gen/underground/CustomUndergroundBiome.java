package vazkii.quark.content.world.gen.underground;

import net.minecraft.block.BlockState;
import net.minecraft.util.math.BlockPos;
import vazkii.quark.base.util.WeightedSelector;
import vazkii.quark.content.world.gen.UndergroundBiomeGenerator.Context;

public class CustomUndergroundBiome extends BasicUndergroundBiome {

	private final WeightedSelector<BlockState> floor;
	private final WeightedSelector<BlockState> ceil;
	private final WeightedSelector<BlockState> wall;

	public CustomUndergroundBiome(WeightedSelector<BlockState> floor, WeightedSelector<BlockState> ceil, WeightedSelector<BlockState> wall, boolean mimicInside) {
		super(null, null, null, mimicInside);
		this.floor = floor;
		this.ceil = ceil;
		this.wall = wall;
	}

	private void placeState(Context context, BlockPos pos, BlockState setState) {
		if (setState != null) {
			int flags = 2;
			if (setState.getMaterial().isLiquid())
				flags |= 1;
			context.world.setBlockState(pos, setState, flags);
		}
	}
	
	@Override
	public void fillCeiling(Context context, BlockPos pos, BlockState state) {
		BlockState setState = ceil.select();
		placeState(context, pos, setState);
	}
	
	@Override
	public void fillWall(Context context, BlockPos pos, BlockState state) {
		BlockState setState = wall.select();
		placeState(context, pos, setState);
	}

	@Override
	public void fillFloor(Context context, BlockPos pos, BlockState state) {
		BlockState setState = floor.select();
		placeState(context, pos, setState);
	}
}
