package vazkii.quark.content.building.block;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.ShapeContext;
import net.minecraft.block.Waterloggable;
import net.minecraft.fluid.FluidState;
import net.minecraft.fluid.Fluids;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.BooleanProperty;
import net.minecraft.state.property.Properties;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.world.BlockView;
import vazkii.quark.base.block.QuarkBlock;
import vazkii.quark.base.module.QuarkModule;

public class WoodPostBlock extends QuarkBlock implements Waterloggable {

	private static final VoxelShape SHAPE = Block.createCuboidShape(6F, 0F, 6F, 10F, 16F, 10F);

	public static final BooleanProperty WATERLOGGED = Properties.WATERLOGGED;

	public WoodPostBlock(QuarkModule module, Block parent) {
		super(parent.getRegistryName().getPath().replace("_fence", "_post"), module, ItemGroup.BUILDING_BLOCKS, Settings.copy(parent));
		setDefaultState(stateManager.getDefaultState().with(WATERLOGGED, false));
	}

	@Override
	public VoxelShape getOutlineShape(BlockState state, BlockView worldIn, BlockPos pos, ShapeContext context) {
		return SHAPE;
	}

	@Override
	public boolean isTranslucent(BlockState state, BlockView reader, BlockPos pos) {
		return !state.get(WATERLOGGED);
	}

	@Override
	@SuppressWarnings("deprecation")
	public FluidState getFluidState(BlockState state) {
		return state.get(WATERLOGGED) ? Fluids.WATER.getStill(false) : super.getFluidState(state);
	}
	
	@Override
	public BlockState getPlacementState(ItemPlacementContext context) {
		return getDefaultState().with(WATERLOGGED, context.getWorld().getFluidState(context.getBlockPos()).getFluid() == Fluids.WATER);
	}

	@Override
	protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
		builder.add(WATERLOGGED);
	}

}
