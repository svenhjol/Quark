package vazkii.quark.content.world.gen.underground;

import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.util.math.BlockPos;
import vazkii.quark.content.world.gen.UndergroundBiomeGenerator.Context;
import vazkii.quark.content.world.module.underground.ElderPrismarineUndergroundBiomeModule;

public class ElderPrismarineUndergroundBiome extends BasicUndergroundBiome {

	private BlockState lanternState;
	
	public ElderPrismarineUndergroundBiome() {
		super(ElderPrismarineUndergroundBiomeModule.elder_prismarine.getDefaultState(), ElderPrismarineUndergroundBiomeModule.elder_prismarine.getDefaultState(), ElderPrismarineUndergroundBiomeModule.elder_prismarine.getDefaultState());
		lanternState = ElderPrismarineUndergroundBiomeModule.elder_sea_lantern.getDefaultState();
	}
	
	@Override
	public void fillWall(Context context, BlockPos pos, BlockState state) {
		super.fillWall(context, pos, state);
		
		if(context.random.nextDouble() < ElderPrismarineUndergroundBiomeModule.lanternChance)
			context.world.setBlockState(pos, lanternState, 2);
	}
	
	@Override
	public void fillFloor(Context context, BlockPos pos, BlockState state) {
		if(context.random.nextDouble() < ElderPrismarineUndergroundBiomeModule.waterChance && !isBorder(context.world, pos))
			context.world.setBlockState(pos, Blocks.WATER.getDefaultState(), 2);
		else super.fillFloor(context, pos, state);
	}

}
