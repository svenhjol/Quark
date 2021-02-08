package vazkii.quark.content.building.block;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.block.material.MaterialColor;
import net.minecraft.item.BlockItemUseContext;
import net.minecraft.item.ItemGroup;
import net.minecraft.state.EnumProperty;
import net.minecraft.state.StateContainer;
import net.minecraft.state.properties.BlockStateProperties;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import vazkii.quark.base.block.QuarkBlock;
import vazkii.quark.base.module.QuarkModule;

public class BambooMatBlock extends QuarkBlock {
	
	public static final EnumProperty<Direction> FACING = BlockStateProperties.FACING_EXCEPT_UP;
	
	public BambooMatBlock(QuarkModule module) {
		super("bamboo_mat", module, ItemGroup.BUILDING_BLOCKS,
				Block.Properties.create(Material.WOOD, MaterialColor.YELLOW)
				.hardnessAndResistance(0.5F)
				.sound(SoundType.WOOD));
		
		setDefaultState(getDefaultState().with(FACING, Direction.NORTH));
	}

	@Override
	public BlockState getStateForPlacement(BlockItemUseContext ctx) {
		Direction dir = ctx.getPlacementHorizontalFacing();
		if(ctx.getPlayer().rotationPitch > 70)
			dir = Direction.DOWN;
		
		if(dir != Direction.DOWN) {
			Direction opposite = dir.getOpposite();
			BlockPos target = ctx.getPos().offset(opposite);
			BlockState state = ctx.getWorld().getBlockState(target);
			
			if(state.getBlock() != this || state.get(FACING) != opposite) {
				target = ctx.getPos().offset(dir);
				state = ctx.getWorld().getBlockState(target);
				
				if(state.getBlock() == this && state.get(FACING) == dir)
					dir = opposite;
			}
		}
		
		return getDefaultState().with(FACING, dir);
	}
	
	@Override
	protected void fillStateContainer(StateContainer.Builder<Block, BlockState> builder) {
		builder.add(FACING);
	}

}
