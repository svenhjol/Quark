package vazkii.quark.content.building.block;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Material;
import net.minecraft.block.MaterialColor;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.sound.BlockSoundGroup;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.EnumProperty;
import net.minecraft.state.property.Properties;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import vazkii.quark.base.block.QuarkBlock;
import vazkii.quark.base.module.QuarkModule;

public class BambooMatBlock extends QuarkBlock {
	
	public static final EnumProperty<Direction> FACING = Properties.HOPPER_FACING;
	
	public BambooMatBlock(QuarkModule module) {
		super("bamboo_mat", module, ItemGroup.BUILDING_BLOCKS,
				Block.Properties.of(Material.WOOD, MaterialColor.YELLOW)
				.strength(0.5F)
				.sounds(BlockSoundGroup.WOOD));
		
		setDefaultState(getDefaultState().with(FACING, Direction.NORTH));
	}

	@Override
	public BlockState getPlacementState(ItemPlacementContext ctx) {
		Direction dir = ctx.getPlayerFacing();
		if(ctx.getPlayer().pitch > 70)
			dir = Direction.DOWN;
		
		if(dir != Direction.DOWN) {
			Direction opposite = dir.getOpposite();
			BlockPos target = ctx.getBlockPos().offset(opposite);
			BlockState state = ctx.getWorld().getBlockState(target);
			
			if(state.getBlock() != this || state.get(FACING) != opposite) {
				target = ctx.getBlockPos().offset(dir);
				state = ctx.getWorld().getBlockState(target);
				
				if(state.getBlock() == this && state.get(FACING) == dir)
					dir = opposite;
			}
		}
		
		return getDefaultState().with(FACING, dir);
	}
	
	@Override
	protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
		builder.add(FACING);
	}

}
