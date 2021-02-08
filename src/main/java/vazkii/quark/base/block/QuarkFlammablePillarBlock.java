package vazkii.quark.base.block;

import net.minecraft.block.BlockState;
import net.minecraft.item.ItemGroup;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockReader;
import vazkii.quark.base.module.QuarkModule;

public class QuarkFlammablePillarBlock extends QuarkPillarBlock {

	final int flammability;
	
	public QuarkFlammablePillarBlock(String regname, QuarkModule module, ItemGroup creativeTab, int flamability, Properties properties) {
		super(regname, module, creativeTab, properties);
		this.flammability = flamability;
	}
	
	@Override
	public boolean isFlammable(BlockState state, IBlockReader world, BlockPos pos, Direction face) {
		return true;
	}
	
	@Override
	public int getFlammability(BlockState state, IBlockReader world, BlockPos pos, Direction face) {
		return flammability;
	}

}
