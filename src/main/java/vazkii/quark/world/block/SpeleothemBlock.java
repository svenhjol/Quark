package vazkii.quark.world.block;

import java.util.Locale;

import javax.annotation.Nonnull;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.ShapeContext;
import net.minecraft.block.Waterloggable;
import net.minecraft.entity.ai.pathing.NavigationType;
import net.minecraft.fluid.FluidState;
import net.minecraft.fluid.Fluids;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.state.StateManager.Builder;
import net.minecraft.state.property.BooleanProperty;
import net.minecraft.state.property.EnumProperty;
import net.minecraft.state.property.Properties;
import net.minecraft.tag.FluidTags;
import net.minecraft.util.StringIdentifiable;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;
import net.minecraft.world.WorldView;
import vazkii.quark.base.block.QuarkBlock;
import vazkii.quark.base.module.Module;

public class SpeleothemBlock extends QuarkBlock implements Waterloggable {

	public static final EnumProperty<SpeleothemSize> SIZE = EnumProperty.of("size", SpeleothemSize.class);
	public static final BooleanProperty WATERLOGGED = Properties.WATERLOGGED;
	
	public SpeleothemBlock(String name, Module module, Block parent, boolean nether) {
		super(name + "_speleothem", module, ItemGroup.DECORATIONS, 
				Block.Properties.copy(parent)
				.strength(nether ? 0.4F : 1.5F)
				.nonOpaque());
		
		setDefaultState(getDefaultState().with(SIZE, SpeleothemSize.BIG).with(WATERLOGGED, false));
	}

	@Override
	public boolean canPlaceAt(BlockState state, WorldView worldIn, BlockPos pos) {
		return getBearing(worldIn, pos) > 0;
	}
	
	@Override
	public BlockState getPlacementState(ItemPlacementContext context) {
		SpeleothemSize size = SpeleothemSize.values()[Math.max(0, getBearing(context.getWorld(), context.getBlockPos()) - 1)];
		return getDefaultState().with(SIZE, size).with(WATERLOGGED, context.getWorld().getFluidState(context.getBlockPos()).getFluid() == Fluids.WATER);
	}
	
	@Nonnull
	@Override
	@SuppressWarnings("deprecation")
	public FluidState getFluidState(BlockState state) {
		return state.get(WATERLOGGED) ? Fluids.WATER.getStill(false) : super.getFluidState(state);
	}
	
	public void neighborUpdate(BlockState state, World worldIn, BlockPos pos, Block blockIn, BlockPos fromPos, boolean isMoving) {
		int size = state.get(SIZE).strength;
		if(getBearing(worldIn, pos) < size + 1)
			worldIn.breakBlock(pos, false);
	}

	@Override
	public boolean canPathfindThrough(@Nonnull BlockState state, @Nonnull BlockView worldIn, @Nonnull BlockPos pos, NavigationType type) {
		return type == NavigationType.WATER && worldIn.getFluidState(pos).isIn(FluidTags.WATER); 
	}
	
	private int getBearing(WorldView world, BlockPos pos) {
		return Math.max(getStrength(world, pos.down()), getStrength(world, pos.up()));
	}
	
	private int getStrength(WorldView world, BlockPos pos) {
		BlockState state = world.getBlockState(pos);
		if(state.isOpaque())
			return 3;
		
		if(state.getEntries().containsKey(SIZE))
			return state.get(SIZE).strength;
		
		return 0;
	}
	
//	@Override does this work?
//	public boolean isSolid(BlockState state) {
//		return false;
//	}
	
	@Override
	public VoxelShape getOutlineShape(BlockState state, BlockView worldIn, BlockPos pos, ShapeContext context) {
		return state.get(SIZE).shape;
	}

	@Override
	protected void appendProperties(Builder<Block, BlockState> builder) {
		builder.add(SIZE, WATERLOGGED);
	}
	
	public enum SpeleothemSize implements StringIdentifiable {
		
		SMALL(0, 2),
		MEDIUM(1, 4),
		BIG(2, 8);
		
		SpeleothemSize(int strength, int width) {
			this.strength = strength;
			
			int pad = (16 - width) / 2;
			shape = Block.createCuboidShape(pad, 0, pad, 16 - pad, 16, 16 - pad);
		}
		
		public final int strength;
		public final VoxelShape shape;

		@Override
		public String asString() { // getName
			return name().toLowerCase(Locale.ROOT);
		}
		
	}

}
