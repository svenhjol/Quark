package vazkii.quark.building.block;

import net.minecraft.block.BlockState;
import net.minecraft.item.ItemGroup;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.BlockView;
import net.minecraft.world.WorldView;
import vazkii.quark.base.block.QuarkBlock;
import vazkii.quark.base.module.Module;
import vazkii.quark.building.module.CompressedBlocksModule;

public class BurnForeverBlock extends QuarkBlock {
	
	final boolean flammable;

	public BurnForeverBlock(String regname, Module module, ItemGroup creativeTab, Settings properties, boolean flammable) {
		super(regname, module, creativeTab, properties);
		this.flammable = flammable;
	}

	@Override
	public boolean isFireSource(BlockState state, WorldView world, BlockPos pos, Direction side) {
		return side == Direction.UP && CompressedBlocksModule.burnsForever;
	}
	
	@Override
	public boolean isFlammable(BlockState state, BlockView world, BlockPos pos, Direction face) {
		return flammable;
	}
	
	@Override
	public int getFlammability(BlockState state, BlockView world, BlockPos pos, Direction face) {
		return 5;
	}
	
}
