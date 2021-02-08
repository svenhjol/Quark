package vazkii.quark.content.world.gen.underground;

import net.minecraft.block.BlockState;
import net.minecraft.util.math.BlockPos;
import vazkii.quark.content.world.gen.UndergroundBiomeGenerator.Context;

public class BasicUndergroundBiome extends UndergroundBiome {

	public BlockState floorState, ceilingState, wallState;
	public final boolean mimicInside;
	
	public BasicUndergroundBiome(BlockState floorState, BlockState ceilingState, BlockState wallState) {
		this(floorState, ceilingState, wallState, false);
	}
	
	public BasicUndergroundBiome(BlockState floorState, BlockState ceilingState, BlockState wallState, boolean mimicInside) {
		this.floorState = floorState;
		this.ceilingState = ceilingState;
		this.wallState = wallState;
		this.mimicInside = mimicInside;
	}
	
	@Override
	public void fillFloor(Context context, BlockPos pos, BlockState state) {
		if(floorState != null)
			context.world.setBlockState(pos, floorState, 2);
	}

	@Override
	public void fillCeiling(Context context, BlockPos pos, BlockState state) {	
		if(ceilingState != null)
			context.world.setBlockState(pos, ceilingState, 2);
	}

	@Override
	public void fillWall(Context context, BlockPos pos, BlockState state) {
		if(wallState != null)
			context.world.setBlockState(pos, wallState, 2);
	}

	@Override
	public void fillInside(Context context, BlockPos pos, BlockState state) {
		if(mimicInside)
			fillWall(context, pos, state);
	} 

}
