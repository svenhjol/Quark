package vazkii.quark.content.building.block;

import net.minecraft.block.BlockState;
import net.minecraft.item.ItemGroup;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.IWorldReader;
import vazkii.quark.base.block.QuarkBlock;
import vazkii.quark.base.module.QuarkModule;
import vazkii.quark.content.building.module.CompressedBlocksModule;

public class BurnForeverBlock extends QuarkBlock {
	
	final boolean flammable;

	public BurnForeverBlock(String regname, QuarkModule module, ItemGroup creativeTab, Properties properties, boolean flammable) {
		super(regname, module, creativeTab, properties);
		this.flammable = flammable;
	}

	@Override
	public boolean isFireSource(BlockState state, IWorldReader world, BlockPos pos, Direction side) {
		return side == Direction.UP && CompressedBlocksModule.burnsForever;
	}
	
	@Override
	public boolean isFlammable(BlockState state, IBlockReader world, BlockPos pos, Direction face) {
		return flammable;
	}
	
	@Override
	public int getFlammability(BlockState state, IBlockReader world, BlockPos pos, Direction face) {
		return 5;
	}
	
}
