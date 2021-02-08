package vazkii.quark.content.world.gen.underground;

import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.util.math.BlockPos;
import vazkii.quark.content.world.gen.UndergroundBiomeGenerator.Context;

public class SlimeUndergroundBiome extends BasicUndergroundBiome {
	
	public SlimeUndergroundBiome() {
		super(Blocks.WATER.getDefaultState(), null, null);
	}
	
	@Override
	public void fillCeiling(Context context, BlockPos pos, BlockState state) {
		BlockState setState = Blocks.GREEN_TERRACOTTA.getDefaultState();
		switch(context.random.nextInt(7)) {
		case 3: case 4: case 5:
			setState = Blocks.LIME_TERRACOTTA.getDefaultState();
			break;
		case 6:
			setState = Blocks.LIGHT_BLUE_TERRACOTTA.getDefaultState();
		}
		
		context.world.setBlockState(pos, setState, 2);
	}
	
	@Override
	public void fillWall(Context context, BlockPos pos, BlockState state) {
		fillCeiling(context, pos, state);
	}

	@Override
	public void fillFloor(Context context, BlockPos pos, BlockState state) {
		if(context.random.nextDouble() < 0.085)
			context.world.setBlockState(pos, Blocks.SLIME_BLOCK.getDefaultState(), 2);
		else
			context.world.setBlockState(pos, floorState, 3);
	}
	

}
