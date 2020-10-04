package vazkii.quark.automation.block;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.ShapeContext;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.ai.pathing.NavigationType;
import net.minecraft.item.ItemGroup;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.BooleanProperty;
import net.minecraft.state.property.Properties;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.util.shape.VoxelShapes;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;
import vazkii.quark.automation.tile.ChuteTileEntity;
import vazkii.quark.base.block.QuarkBlock;
import vazkii.quark.base.handler.RenderLayerHandler;
import vazkii.quark.base.handler.RenderLayerHandler.RenderTypeSkeleton;
import vazkii.quark.base.module.Module;

public class ChuteBlock extends QuarkBlock {

	private static final VoxelShape INPUT_SHAPE = Block.createCuboidShape(0.0D, 10.0D, 0.0D, 16.0D, 16.0D, 16.0D);
	private static final VoxelShape MIDDLE_SHAPE = Block.createCuboidShape(4.0D, 4.0D, 4.0D, 12.0D, 10.0D, 12.0D);
	private static final VoxelShape CHUTE_TOP_SHAPE = VoxelShapes.union(MIDDLE_SHAPE, INPUT_SHAPE);
	private static final VoxelShape DOWN_SHAPE = VoxelShapes.union(CHUTE_TOP_SHAPE, Block.createCuboidShape(6.0D, 0.0D, 6.0D, 10.0D, 4.0D, 10.0D));

	public static final BooleanProperty ENABLED = Properties.ENABLED;

	public ChuteBlock(String regname, Module module, ItemGroup creativeTab, Settings properties) {
		super(regname, module, creativeTab, properties);
		setDefaultState(getDefaultState().with(ENABLED, true));
		
		RenderLayerHandler.setRenderType(this, RenderTypeSkeleton.CUTOUT_MIPPED);
	}

	@Override
	public int getFlammability(BlockState state, BlockView world, BlockPos pos, Direction face) {
		return 20;
	}

	@Override
	public int getFireSpreadSpeed(BlockState state, BlockView world, BlockPos pos, Direction face) {
		return 5;
	}

	@Override
	@SuppressWarnings("deprecation")
	public void neighborUpdate(BlockState state, World worldIn, BlockPos pos, Block blockIn, BlockPos fromPos, boolean isMoving) {
		boolean flag = !worldIn.isReceivingRedstonePower(pos);

		if(flag != state.get(ENABLED))
			worldIn.setBlockState(pos, state.with(ENABLED, flag), 2 | 4);
	}

	@Override
	public boolean hasTileEntity(BlockState state) {
		return true;
	}

	@Nullable
	@Override
	public BlockEntity createTileEntity(BlockState state, BlockView world) {
		return new ChuteTileEntity();
	}

	@Nonnull
	@Override
	@SuppressWarnings("deprecation")
	public VoxelShape getOutlineShape(BlockState state, BlockView worldIn, BlockPos pos, ShapeContext context) {
		return DOWN_SHAPE;
	}

	@Override
	protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
		builder.add(ENABLED);
	}

	@Override
	@SuppressWarnings("deprecation")
	public boolean canPathfindThrough(@Nonnull BlockState state, @Nonnull BlockView worldIn, @Nonnull BlockPos pos, NavigationType type) {
		return false;
	}
}
