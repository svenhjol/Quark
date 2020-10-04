package vazkii.quark.oddities.block;

import java.util.Collections;
import java.util.List;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import net.minecraft.block.Block;
import net.minecraft.block.BlockRenderType;
import net.minecraft.block.BlockState;
import net.minecraft.block.Material;
import net.minecraft.block.PistonHeadBlock;
import net.minecraft.block.ShapeContext;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.ai.pathing.NavigationType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.loot.context.LootContext;
import net.minecraft.loot.context.LootContextParameters;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.DirectionProperty;
import net.minecraft.util.ActionResult;
import net.minecraft.util.BlockMirror;
import net.minecraft.util.BlockRotation;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.util.shape.VoxelShapes;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;
import vazkii.quark.base.block.QuarkBlock;
import vazkii.quark.base.module.Module;
import vazkii.quark.oddities.tile.MagnetizedBlockTileEntity;

/**
 * @author WireSegal
 * Created at 3:05 PM on 2/26/20.
 */
public class MovingMagnetizedBlock extends QuarkBlock {
	public static final DirectionProperty FACING = PistonHeadBlock.FACING;

	public MovingMagnetizedBlock(Module module) {
		super("magnetized_block", module, null, Block.Properties.of(Material.PISTON).strength(-1.0F).dynamicBounds().dropsNothing().nonOpaque());
		this.setDefaultState(this.stateManager.getDefaultState().with(FACING, Direction.NORTH));
	}

	@Nonnull
	@Override
	public BlockRenderType getRenderType(BlockState state) {
		return BlockRenderType.INVISIBLE;
	}

	@Override
	public boolean hasTileEntity(BlockState state) {
		return true;
	}

	@Nullable
	@Override
	public BlockEntity createTileEntity(BlockState state, BlockView world) {
		return null;
	}

	@Override
	public void onStateReplaced(BlockState state, @Nonnull World worldIn, @Nonnull BlockPos pos, @Nonnull BlockState newState, boolean isMoving) {
		if (state.getBlock() != newState.getBlock()) {
			MagnetizedBlockTileEntity tile = getMagnetTileEntity(worldIn, pos);
			if (tile != null)
				tile.clearMagnetTileEntity();
		}
	}

	@Override 
	public boolean hasSidedTransparency(BlockState state) {
		return true;
	}

//	@Override
//	public boolean isNormalCube(BlockState state, @Nonnull IBlockReader worldIn, @Nonnull BlockPos pos) {
//		return false;
//	}
//
//	@Override
//	public boolean causesSuffocation(@Nonnull BlockState state, @Nonnull IBlockReader worldIn, @Nonnull BlockPos pos) {
//		return false;
//	}

	@Override
	public ActionResult onUse(BlockState state, World worldIn, BlockPos pos, PlayerEntity player, Hand handIn, BlockHitResult hit) {
		if (!worldIn.isClient && worldIn.getBlockEntity(pos) == null) {
			worldIn.removeBlock(pos, false);
			return ActionResult.SUCCESS;
		} else
			return ActionResult.PASS;
	}

	@Override
	@Nonnull
	public List<ItemStack> getDroppedStacks(@Nonnull BlockState state, @Nonnull LootContext.Builder builder) {
		MagnetizedBlockTileEntity tile = this.getMagnetTileEntity(builder.getWorld(), builder.get(LootContextParameters.POSITION));
		return tile == null ? Collections.emptyList() : tile.getMagnetState().getDroppedStacks(builder);
	}

	@Override
	@Nonnull
	public VoxelShape getOutlineShape(BlockState state, BlockView worldIn, BlockPos pos, ShapeContext context) {
		return VoxelShapes.empty();
	}

	@Override
	@Nonnull
	public VoxelShape getCollisionShape(@Nonnull BlockState state, @Nonnull BlockView worldIn, @Nonnull BlockPos pos, ShapeContext context) {
		MagnetizedBlockTileEntity tile = this.getMagnetTileEntity(worldIn, pos);
		return tile != null ? tile.getCollisionShape(worldIn, pos) : VoxelShapes.empty();
	}

	@Nullable
	private MagnetizedBlockTileEntity getMagnetTileEntity(BlockView world, BlockPos pos) {
		BlockEntity tile = world.getBlockEntity(pos);
		return tile instanceof MagnetizedBlockTileEntity ? (MagnetizedBlockTileEntity)tile : null;
	}

	@Override
	@Nonnull
	public ItemStack getPickStack(BlockView worldIn, BlockPos pos, BlockState state) {
		return ItemStack.EMPTY;
	}

	@Override
	@Nonnull
	public BlockState rotate(@Nonnull BlockState state, BlockRotation rot) {
		return state.with(FACING, rot.rotate(state.get(FACING)));
	}

	@Override
	@Nonnull
	public BlockState mirror(@Nonnull BlockState state, BlockMirror mirrorIn) {
		return state.rotate(mirrorIn.getRotation(state.get(FACING)));
	}

	@Override
	protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
		builder.add(FACING);
	}

	@Override
	public boolean canPathfindThrough(@Nonnull BlockState state, @Nonnull BlockView worldIn, @Nonnull BlockPos pos, NavigationType type) {
		return false;
	}
}
