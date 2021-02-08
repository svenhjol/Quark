package vazkii.quark.content.building.block;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.ShapeContext;
import net.minecraft.block.Waterloggable;
import net.minecraft.entity.ai.pathing.NavigationType;
import net.minecraft.fluid.FluidState;
import net.minecraft.fluid.Fluids;
import net.minecraft.fluid.WaterFluid;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.BooleanProperty;
import net.minecraft.state.property.Properties;
import net.minecraft.tag.FluidTags;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;
import net.minecraft.world.WorldAccess;
import vazkii.quark.base.block.QuarkBlock;
import vazkii.quark.base.module.QuarkModule;

public class ShallowDirtBlock extends QuarkBlock implements Waterloggable {

	private static final VoxelShape SHAPE = createCuboidShape(0, 0, 0, 16, 14, 16);
	
	public static final BooleanProperty WATERLOGGED = Properties.WATERLOGGED;
	
	public ShallowDirtBlock(QuarkModule module) {
		super("shallow_dirt", module, null, Settings.copy(Blocks.DIRT).nonOpaque());
		
		setDefaultState(getDefaultState().with(WATERLOGGED, false));
	}
	
	@Override
	protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
		builder.add(WATERLOGGED);
	}
	
	@Nonnull
	@Override
	public VoxelShape getOutlineShape(BlockState state, BlockView worldIn, BlockPos pos, ShapeContext context) {
		return SHAPE;
	}
	
	@Override
	public void neighborUpdate(BlockState state, World worldIn, BlockPos pos, Block blockIn, BlockPos fromPos, boolean isMoving) {
		super.neighborUpdate(state, worldIn, pos, blockIn, fromPos, isMoving);
		
		FluidState fluid = worldIn.getFluidState(fromPos);
		if(fluid.getFluid() instanceof WaterFluid.Flowing)
			worldIn.setBlockState(pos, state.with(WATERLOGGED, false));
	}
	
	@Override
	@Nullable
	public BlockState getPlacementState(ItemPlacementContext context) {
		BlockPos blockpos = context.getBlockPos();
		FluidState fluid = context.getWorld().getFluidState(blockpos);
		return getDefaultState().with(WATERLOGGED, fluid.getFluid() == Fluids.WATER);
	}
	

	@Nonnull
	@Override
	@SuppressWarnings("deprecation")
	public FluidState getFluidState(BlockState state) {
		return state.get(WATERLOGGED) ? Fluids.WATER.getStill(false) : super.getFluidState(state);
	}
	
	@Nonnull
	@Override
	@SuppressWarnings("deprecation")
	public BlockState getStateForNeighborUpdate(@Nonnull BlockState stateIn, Direction facing, BlockState facingState, WorldAccess worldIn, BlockPos currentPos, BlockPos facingPos) {
		if(stateIn.get(WATERLOGGED))
			worldIn.getFluidTickScheduler().schedule(currentPos, Fluids.WATER, Fluids.WATER.getTickRate(worldIn));

		return super.getStateForNeighborUpdate(stateIn, facing, facingState, worldIn, currentPos, facingPos);
	}

	
	@Override
	public boolean hasSidedTransparency(BlockState state) {
		return true;
	}
	
	@Override
	public boolean canPathfindThrough(@Nonnull BlockState state, @Nonnull BlockView worldIn, @Nonnull BlockPos pos, NavigationType type) {
		return type == NavigationType.WATER && worldIn.getFluidState(pos).isIn(FluidTags.WATER); 
	}

}
