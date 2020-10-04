package vazkii.quark.world.gen.underground;

import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.WorldAccess;
import vazkii.quark.world.gen.UndergroundBiomeGenerator.Context;
import vazkii.quark.world.module.underground.PermafrostUndergroundBiomeModule;

public class PermafrostUndergroundBiome extends BasicUndergroundBiome {
	
	public PermafrostUndergroundBiome() {
		super(Blocks.PACKED_ICE.getDefaultState(), PermafrostUndergroundBiomeModule.permafrost.getDefaultState(), PermafrostUndergroundBiomeModule.permafrost.getDefaultState(), true);
	}
	
	@Override
	public void fillFloor(Context context, BlockPos pos, BlockState state) {
		super.fillFloor(context, pos, state);

		WorldAccess world = context.world;
		if(context.random.nextDouble() < 0.015) {
			int height = 3 + context.random.nextInt(3);
			for(int i = 0; i < height; i++) {
				pos = pos.up();
				BlockState stateAt = world.getBlockState(pos);
				
				if(world.getBlockState(pos).getBlock().isAir(stateAt, world, pos))
					world.setBlockState(pos, floorState, 2);
				else break;
			}
		}
	}

}
