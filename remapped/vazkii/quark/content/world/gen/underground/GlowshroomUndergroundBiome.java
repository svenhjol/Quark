package vazkii.quark.content.world.gen.underground;

import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.util.math.BlockPos;
import vazkii.quark.content.world.gen.UndergroundBiomeGenerator.Context;
import vazkii.quark.content.world.module.underground.GlowshroomUndergroundBiomeModule;

public class GlowshroomUndergroundBiome extends BasicUndergroundBiome {

	public GlowshroomUndergroundBiome() {
		super(GlowshroomUndergroundBiomeModule.glowcelium.getDefaultState(), Blocks.DIRT.getDefaultState(), Blocks.DIRT.getDefaultState());
	}

	@Override
	public void fillFloor(Context context, BlockPos pos, BlockState state) {
		super.fillFloor(context, pos, state);

		if(context.random.nextDouble() < GlowshroomUndergroundBiomeModule.glowshroomSpawnChance)
			context.world.setBlockState(pos.up(), GlowshroomUndergroundBiomeModule.glowshroom.getDefaultState(), 2);
	}

}
