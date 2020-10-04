package vazkii.quark.building.block;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.ShapeContext;
import net.minecraft.block.SlabBlock;
import net.minecraft.block.Waterloggable;
import net.minecraft.client.color.block.BlockColorProvider;
import net.minecraft.client.color.item.ItemColorProvider;
import net.minecraft.entity.ai.pathing.NavigationType;
import net.minecraft.fluid.Fluid;
import net.minecraft.fluid.FluidState;
import net.minecraft.fluid.Fluids;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.item.ItemStack;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.BooleanProperty;
import net.minecraft.state.property.EnumProperty;
import net.minecraft.state.property.Properties;
import net.minecraft.tag.FluidTags;
import net.minecraft.util.StringIdentifiable;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Direction.Axis;
import net.minecraft.util.math.Direction.AxisDirection;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.util.shape.VoxelShapes;
import net.minecraft.world.BlockView;
import net.minecraft.world.WorldAccess;
import net.minecraft.world.WorldView;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import vazkii.arl.interf.IBlockColorProvider;
import vazkii.arl.interf.IItemColorProvider;
import vazkii.quark.base.block.QuarkBlock;
import vazkii.quark.base.block.QuarkSlabBlock;
import vazkii.quark.base.module.Module;

public class VerticalSlabBlock extends QuarkBlock implements Waterloggable, IBlockColorProvider {

	public static final EnumProperty<VerticalSlabType> TYPE = EnumProperty.of("type", VerticalSlabType.class);
	public static final BooleanProperty WATERLOGGED = Properties.WATERLOGGED;

	public final Block parent;

	public VerticalSlabBlock(Block parent, Module module) {
		super(parent.getRegistryName().getPath().replace("_slab", "_vertical_slab"), module, ItemGroup.BUILDING_BLOCKS, Block.Properties.copy(parent));
		this.parent = parent;
		
		if(!(parent instanceof SlabBlock))
			throw new IllegalArgumentException("Can't rotate a non-slab block into a vertical slab.");

		if(parent instanceof QuarkSlabBlock)
			setCondition(((QuarkSlabBlock) parent).parent::isEnabled);

		setDefaultState(getDefaultState().with(TYPE, VerticalSlabType.NORTH).with(WATERLOGGED, false));
	}
	
	@Override
	public boolean hasSidedTransparency(BlockState state) {
		return state.get(TYPE) != VerticalSlabType.DOUBLE;
	}

	@Override
	protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
		builder.add(TYPE, WATERLOGGED);
	}

	@Nonnull
	@Override
	public VoxelShape getOutlineShape(BlockState state, BlockView worldIn, BlockPos pos, ShapeContext context) {
		return state.get(TYPE).shape;
	}
	
	@Override
	public boolean isConduitFrame(BlockState state, WorldView world, BlockPos pos, BlockPos conduit) {
		return parent.isConduitFrame(state, world, pos, conduit);
	}

	@Override
	@Nullable
	public BlockState getPlacementState(ItemPlacementContext context) {
		BlockPos blockpos = context.getBlockPos();
		BlockState blockstate = context.getWorld().getBlockState(blockpos);
		if(blockstate.getBlock() == this) 
			return blockstate.with(TYPE, VerticalSlabType.DOUBLE).with(WATERLOGGED, false);
		
		FluidState fluid = context.getWorld().getFluidState(blockpos);
		BlockState retState = getDefaultState().with(WATERLOGGED, fluid.getFluid() == Fluids.WATER);
		Direction direction = getDirectionForPlacement(context);
		VerticalSlabType type = VerticalSlabType.fromDirection(direction);
		
		return retState.with(TYPE, type);
	}
	
	private Direction getDirectionForPlacement(ItemPlacementContext context) {
		Direction direction = context.getSide();
		if(direction.getAxis() != Axis.Y)
			return direction;
		
		BlockPos pos = context.getBlockPos();
		Vec3d vec = context.getHitPos().subtract(new Vec3d(pos.getX(), pos.getY(), pos.getZ())).subtract(0.5, 0, 0.5);
		double angle = Math.atan2(vec.x, vec.z) * -180.0 / Math.PI;
		return Direction.fromRotation(angle).getOpposite();
	}

	@Override
	public boolean canReplace(BlockState state, @Nonnull ItemPlacementContext useContext) {
		ItemStack itemstack = useContext.getStack();
		VerticalSlabType slabtype = state.get(TYPE);
		return slabtype != VerticalSlabType.DOUBLE && itemstack.getItem() == this.asItem()  &&
			(useContext.canReplaceExisting() && (useContext.getSide() == slabtype.direction && getDirectionForPlacement(useContext) == slabtype.direction)
					|| (!useContext.canReplaceExisting() && useContext.getSide().getAxis() != slabtype.direction.getAxis()));
	}

	@Nonnull
	@Override
	@SuppressWarnings("deprecation")
	public FluidState getFluidState(BlockState state) {
		return state.get(WATERLOGGED) ? Fluids.WATER.getStill(false) : super.getFluidState(state);
	}

	@Override
	public boolean tryFillWithFluid(@Nonnull WorldAccess worldIn, @Nonnull BlockPos pos, BlockState state, @Nonnull FluidState fluidStateIn) {
		return state.get(TYPE) != VerticalSlabType.DOUBLE && Waterloggable.super.tryFillWithFluid(worldIn, pos, state, fluidStateIn);
	}

	@Override
	public boolean canFillWithFluid(BlockView worldIn, BlockPos pos, BlockState state, Fluid fluidIn) {
		return state.get(TYPE) != VerticalSlabType.DOUBLE && Waterloggable.super.canFillWithFluid(worldIn, pos, state, fluidIn);
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
	public boolean canPathfindThrough(@Nonnull BlockState state, @Nonnull BlockView worldIn, @Nonnull BlockPos pos, NavigationType type) {
		return type == NavigationType.WATER && worldIn.getFluidState(pos).isIn(FluidTags.WATER); 
	}

	@Override
	@Environment(EnvType.CLIENT)
	public BlockColorProvider getBlockColor() {
		return parent instanceof IBlockColorProvider ? ((IBlockColorProvider) parent).getBlockColor() : null;
	}

	@Override
	@Environment(EnvType.CLIENT)
	public ItemColorProvider getItemColor() {
		return parent instanceof IItemColorProvider ? ((IItemColorProvider) parent).getItemColor() : null;
	}

	public enum VerticalSlabType implements StringIdentifiable {
		NORTH(Direction.NORTH),
		SOUTH(Direction.SOUTH),
		WEST(Direction.WEST),
		EAST(Direction.EAST),
		DOUBLE(null);

		private final String name;
		public final Direction direction;
		public final VoxelShape shape;

		VerticalSlabType(Direction direction) {
			this.name = direction == null ? "double" : direction.asString(); // name()
			this.direction = direction;

			if(direction == null)
				shape = VoxelShapes.fullCube();
			else {
				double min = 0;
				double max = 8;
				if(direction.getDirection() == AxisDirection.NEGATIVE) {
					min = 8;
					max = 16;
				}

				if(direction.getAxis() == Axis.X)
					shape = Block.createCuboidShape(min, 0, 0, max, 16, 16);
				else shape = Block.createCuboidShape(0, 0, min, 16, 16, max);
			}
		}

		@Override
		public String toString() {
			return name;
		}

		@Nonnull
		@Override
		public String asString() { // getName
			return name;
		}

		public static VerticalSlabType fromDirection(Direction direction) {
			for(VerticalSlabType type : VerticalSlabType.values())
				if(type.direction != null && direction == type.direction)
					return type;

			return null;
		}

	}

}
