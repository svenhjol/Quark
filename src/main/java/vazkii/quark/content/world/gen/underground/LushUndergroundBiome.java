package vazkii.quark.content.world.gen.underground;

import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.IGrowable;
import net.minecraft.block.VineBlock;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IWorld;
import vazkii.quark.base.handler.MiscUtil;
import vazkii.quark.content.world.gen.UndergroundBiomeGenerator.Context;

public class LushUndergroundBiome extends BasicUndergroundBiome {

	public LushUndergroundBiome() {
		super(Blocks.GRASS_BLOCK.getDefaultState(), null, null);
	}

	@Override
	public void finalFloorPass(Context context, BlockPos pos) {
		if(context.world.getBlockState(pos).getBlock() == Blocks.GRASS_BLOCK && context.random.nextFloat() < 0.6)
			context.world.setBlockState(pos.up(), Blocks.GRASS.getDefaultState(), 0);
	}

	@Override
	public void finalWallPass(Context context, BlockPos pos) {
		IWorld world = context.world;
		for(Direction facing : MiscUtil.HORIZONTALS) {
			BlockPos off = pos.offset(facing);
			BlockPos up = off.up();
			if(isCeiling(world, up, world.getBlockState(up)) && context.random.nextDouble() < 0.125) {
				BlockState stateAt = world.getBlockState(off); 
				boolean did = false;
				while(stateAt.getBlock().isAir(stateAt, world, off) && off.getY() > 0) {
					world.setBlockState(off, Blocks.VINE.getDefaultState().with(VineBlock.getPropertyFor(facing.getOpposite()), true), 2);
					off = off.down();
					stateAt = world.getBlockState(off);
					did = true;
				}

				if(did)
					return;
			}
		}
	}



}
