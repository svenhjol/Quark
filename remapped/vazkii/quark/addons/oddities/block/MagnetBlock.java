package vazkii.quark.addons.oddities.block;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.piston.PistonBehavior;
import net.minecraft.client.item.TooltipContext;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.state.StateManager.Builder;
import net.minecraft.state.property.BooleanProperty;
import net.minecraft.state.property.DirectionProperty;
import net.minecraft.state.property.Properties;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Text;
import net.minecraft.util.BlockMirror;
import net.minecraft.util.BlockRotation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import vazkii.arl.util.RegistryHelper;
import vazkii.quark.addons.oddities.magnetsystem.MagnetSystem;
import vazkii.quark.addons.oddities.module.MagnetsModule;
import vazkii.quark.addons.oddities.tile.MagnetTileEntity;
import vazkii.quark.addons.oddities.tile.MagnetizedBlockTileEntity;
import vazkii.quark.base.block.QuarkBlock;
import vazkii.quark.base.module.QuarkModule;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;

public class MagnetBlock extends QuarkBlock {

	public static final DirectionProperty FACING = Properties.FACING;
	public static final BooleanProperty POWERED = Properties.POWERED;

	public MagnetBlock(QuarkModule module) {
		super("magnet", module, ItemGroup.REDSTONE, Settings.copy(Blocks.IRON_BLOCK));
		setDefaultState(getDefaultState().with(FACING, Direction.DOWN).with(POWERED, false));
		RegistryHelper.setCreativeTab(this, ItemGroup.REDSTONE);
	}

	@Override
	@Environment(EnvType.CLIENT)
	public void appendTooltip(@Nonnull ItemStack stack, @Nullable BlockView worldIn, @Nonnull List<Text> tooltip, @Nonnull TooltipContext flagIn) {
		if (stack.getName().asString().equals("Q"))
			tooltip.add(new LiteralText("haha yes"));
	}

	@Override
	protected void appendProperties(Builder<Block, BlockState> builder) {
		builder.add(FACING, POWERED);
	}

	@Override
	@SuppressWarnings("deprecation")
	public void neighborUpdate(@Nonnull BlockState state, @Nonnull World worldIn, @Nonnull BlockPos pos, @Nonnull Block blockIn, @Nonnull BlockPos fromPos, boolean isMoving) {
		super.neighborUpdate(state, worldIn, pos, blockIn, fromPos, isMoving);
		
		boolean wasPowered = state.get(POWERED);
		boolean isPowered = isPowered(worldIn, pos, state.get(FACING));
		if(isPowered != wasPowered)
			worldIn.setBlockState(pos, state.with(POWERED, isPowered));
	}

	@Override
	public boolean onSyncedBlockEvent(BlockState state, World world, BlockPos pos, int action, int data) {
		boolean push = action == 0;
		Direction moveDir = state.get(FACING);
		Direction dir = push ? moveDir : moveDir.getOpposite();

		BlockPos targetPos = pos.offset(dir, data);
		BlockState targetState = world.getBlockState(targetPos);

		BlockEntity tile = world.getBlockEntity(pos);
		if (!(tile instanceof MagnetTileEntity))
			return false;

		BlockPos endPos = targetPos.offset(moveDir);
		PistonBehavior reaction = MagnetSystem.getPushAction((MagnetTileEntity) tile, targetPos, targetState, moveDir);
		if (reaction != PistonBehavior.IGNORE && reaction != PistonBehavior.DESTROY)
			return false;

		BlockEntity tilePresent = world.getBlockEntity(targetPos);
		CompoundTag tileData = new CompoundTag();
		if (tilePresent != null && !(tilePresent instanceof MagnetizedBlockTileEntity))
			tilePresent.toTag(tileData);

		MagnetizedBlockTileEntity movingTile = new MagnetizedBlockTileEntity(targetState, tileData, moveDir);

		if (!world.isClient && reaction == PistonBehavior.DESTROY) {
			BlockState blockstate = world.getBlockState(endPos);
			Block.dropStacks(blockstate, world, endPos, tilePresent);
		}

		if (tilePresent != null)
			tilePresent.markRemoved();

		world.setBlockState(endPos, MagnetsModule.magnetized_block.getDefaultState()
				.with(MovingMagnetizedBlock.FACING, moveDir), 68);
		world.setBlockEntity(endPos, movingTile);

		world.setBlockState(targetPos, Blocks.AIR.getDefaultState(), 66);

		return true;
	}

	private boolean isPowered(World worldIn, BlockPos pos, Direction facing) {
		Direction opp = facing.getOpposite();
		for(Direction direction : Direction.values())
			if(direction != facing && direction != opp && worldIn.isEmittingRedstonePower(pos.offset(direction), direction))
				return true;

		return false;
	}

	@Override
	public BlockState getPlacementState(ItemPlacementContext context) {
		Direction facing = context.getPlayerLookDirection().getOpposite();
		return getDefaultState().with(FACING, facing)
				.with(POWERED, isPowered(context.getWorld(), context.getBlockPos(), facing));
	}

	@Nonnull
	@Override
	public BlockState rotate(@Nonnull BlockState state, BlockRotation rot) {
		return state.with(FACING, rot.rotate(state.get(FACING)));
	}

	@Nonnull
	@Override
	public BlockState mirror(@Nonnull BlockState state, BlockMirror mirrorIn) {
		return state.rotate(mirrorIn.getRotation(state.get(FACING)));
	}

	@Override
	public boolean hasTileEntity(BlockState state) {
		return true;
	}

	@Override
	public BlockEntity createTileEntity(BlockState state, BlockView world) {
		return new MagnetTileEntity();
	}

}
