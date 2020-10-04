package vazkii.quark.base.block;

import net.minecraft.block.BlockState;
import net.minecraft.item.ItemGroup;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.BlockView;
import vazkii.quark.base.module.Module;

public class QuarkFlammablePillarBlock extends QuarkPillarBlock {

	final int flammability;
	
	public QuarkFlammablePillarBlock(String regname, Module module, ItemGroup creativeTab, int flamability, Settings properties) {
		super(regname, module, creativeTab, properties);
		this.flammability = flamability;
	}
	
	@Override
	public boolean isFlammable(BlockState state, BlockView world, BlockPos pos, Direction face) {
		return true;
	}
	
	@Override
	public int getFlammability(BlockState state, BlockView world, BlockPos pos, Direction face) {
		return flammability;
	}

}
