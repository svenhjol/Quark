package vazkii.quark.content.world.gen.underground;

import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IWorld;
import vazkii.quark.content.world.gen.UndergroundBiomeGenerator.Context;
import vazkii.quark.content.world.module.underground.PermafrostUndergroundBiomeModule;

public class PermafrostUndergroundBiome extends BasicUndergroundBiome {
	
	public PermafrostUndergroundBiome() {
		super(Blocks.PACKED_ICE.getDefaultState(), PermafrostUndergroundBiomeModule.permafrost.getDefaultState(), PermafrostUndergroundBiomeModule.permafrost.getDefaultState(), true);
	}
	
	@Override
	public void fillFloor(Context context, BlockPos pos, BlockState state) {
		super.fillFloor(context, pos, state);

		IWorld world = context.world;
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
