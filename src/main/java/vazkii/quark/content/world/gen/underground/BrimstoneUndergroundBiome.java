package vazkii.quark.content.world.gen.underground;

import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.util.math.BlockPos;
import vazkii.quark.content.world.gen.UndergroundBiomeGenerator.Context;
import vazkii.quark.content.world.module.underground.BrimstoneUndergroundBiomeModule;

public class BrimstoneUndergroundBiome extends BasicUndergroundBiome {

	public BrimstoneUndergroundBiome() {
		super(BrimstoneUndergroundBiomeModule.brimstone.getDefaultState(), BrimstoneUndergroundBiomeModule.brimstone.getDefaultState(), BrimstoneUndergroundBiomeModule.brimstone.getDefaultState(), true);
	}
	
	@Override
	public void fillCeiling(Context context, BlockPos pos, BlockState state) {
		if(context.random.nextBoolean())
			 context.world.setBlockState(pos, Blocks.COBBLESTONE.getDefaultState(), 2);
		else super.fillCeiling(context, pos, state);
	}
	
	@Override
	public void fillFloor(Context context, BlockPos pos, BlockState state) {
		if(!isBorder(context.world, pos) && context.random.nextDouble() < 0.25)
			context.world.setBlockState(pos, Blocks.LAVA.getDefaultState(), 2);
		else if(context.random.nextDouble() < 0.0625)
			context.world.setBlockState(pos, Blocks.OBSIDIAN.getDefaultState(), 2);
		else super.fillFloor(context, pos, state);
	}
	
}
