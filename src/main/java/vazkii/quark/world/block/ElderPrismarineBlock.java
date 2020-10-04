package vazkii.quark.world.block;

import net.minecraft.block.BlockState;
import net.minecraft.item.ItemGroup;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.WorldView;
import vazkii.quark.base.block.QuarkBlock;
import vazkii.quark.base.module.Module;

public class ElderPrismarineBlock extends QuarkBlock {

	public ElderPrismarineBlock(String regname, Module module, ItemGroup creativeTab, Settings properties) {
		super(regname, module, creativeTab, properties);
	}
	
	@Override
	public boolean isConduitFrame(BlockState state, WorldView world, BlockPos pos, BlockPos conduit) {
		return true;
	}

}
