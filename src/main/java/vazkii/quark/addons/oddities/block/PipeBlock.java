package vazkii.quark.addons.oddities.block;

import java.util.HashSet;
import java.util.Set;

import javax.annotation.Nonnull;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.IWaterLoggable;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.fluid.FluidState;
import net.minecraft.fluid.Fluids;
import net.minecraft.item.BlockItemUseContext;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.state.BooleanProperty;
import net.minecraft.state.StateContainer.Builder;
import net.minecraft.state.properties.BlockStateProperties;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Direction;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.shapes.ISelectionContext;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.util.math.shapes.VoxelShapes;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.IWorld;
import net.minecraft.world.World;
import net.minecraftforge.common.ToolType;
import vazkii.quark.addons.oddities.tile.PipeTileEntity;
import vazkii.quark.base.block.QuarkBlock;
import vazkii.quark.base.handler.RenderLayerHandler;
import vazkii.quark.base.handler.RenderLayerHandler.RenderTypeSkeleton;
import vazkii.quark.base.module.QuarkModule;

public class PipeBlock extends QuarkBlock implements IWaterLoggable {

	private static final VoxelShape CENTER_SHAPE = VoxelShapes.create(0.3125, 0.3125, 0.3125, 0.6875, 0.6875, 0.6875);

	private static final VoxelShape DOWN_SHAPE = VoxelShapes.create(0.3125, 0, 0.3125, 0.6875, 0.6875, 0.6875);
	private static final VoxelShape UP_SHAPE = VoxelShapes.create(0.3125, 0.3125, 0.3125, 0.6875, 1, 0.6875);
	private static final VoxelShape NORTH_SHAPE = VoxelShapes.create(0.3125, 0.3125, 0, 0.6875, 0.6875, 0.6875);
	private static final VoxelShape SOUTH_SHAPE = VoxelShapes.create(0.3125, 0.3125, 0.3125, 0.6875, 0.6875, 1);
	private static final VoxelShape WEST_SHAPE = VoxelShapes.create(0, 0.3125, 0.3125, 0.6875, 0.6875, 0.6875);
	private static final VoxelShape EAST_SHAPE = VoxelShapes.create(0.3125, 0.3125, 0.3125, 1, 0.6875, 0.6875);

	public static final BooleanProperty DOWN = BooleanProperty.create("down");
	public static final BooleanProperty UP = BooleanProperty.create("up");
	public static final BooleanProperty NORTH = BooleanProperty.create("north");
	public static final BooleanProperty SOUTH = BooleanProperty.create("south");
	public static final BooleanProperty WEST = BooleanProperty.create("west");
	public static final BooleanProperty EAST = BooleanProperty.create("east");
	public static final BooleanProperty WATERLOGGED = BlockStateProperties.WATERLOGGED;

	@SuppressWarnings("unchecked")
	private static final BooleanProperty[] CONNECTIONS = new BooleanProperty[] {
			DOWN, UP, NORTH, SOUTH, WEST, EAST
	};

	private static final VoxelShape[] SIDE_BOXES = new VoxelShape[] {
			DOWN_SHAPE, UP_SHAPE, NORTH_SHAPE, SOUTH_SHAPE, WEST_SHAPE, EAST_SHAPE
	};

	private static final VoxelShape[] shapeCache = new VoxelShape[64];

	public PipeBlock(QuarkModule module) {
		super("pipe", module, ItemGroup.REDSTONE, 
				Block.Properties.create(Material.GLASS)
				.hardnessAndResistance(3F, 10F)
				.sound(SoundType.GLASS)
				.notSolid());
		
		setDefaultState(getDefaultState()
				.with(DOWN, false).with(UP, false)
				.with(NORTH, false).with(SOUTH, false)
				.with(WEST, false).with(EAST, false)
				.with(WATERLOGGED, false));
		
		RenderLayerHandler.setRenderType(this, RenderTypeSkeleton.CUTOUT);
	}
	
	@Override
	public boolean isToolEffective(BlockState state, ToolType tool) {
		return tool == ToolType.PICKAXE;
	}
	
	@Override
	public ActionResultType onBlockActivated(BlockState state, World worldIn, BlockPos pos, PlayerEntity player, Hand handIn, BlockRayTraceResult hit) {
		ItemStack stack = player.getHeldItem(handIn);
		
		// fix pipes if they're ruined
		if(stack.getItem() == Items.STICK) {
			Set<BlockPos> found = new HashSet<>();
			boolean fixedAny = false;
			
			Set<BlockPos> candidates = new HashSet<>();
			Set<BlockPos> newCandidates = new HashSet<>();
			
			candidates.add(pos);
			do {
				for(BlockPos cand : candidates) {
					for(Direction d : Direction.values()) {
						BlockPos offPos = cand.offset(d);
						BlockState offState = worldIn.getBlockState(offPos);
						if(offState.getBlock() == this && !candidates.contains(offPos) && !found.contains(offPos))
							newCandidates.add(offPos);
					}
					
					BlockState curr = worldIn.getBlockState(cand);
					BlockState target = getTargetState(worldIn, cand, curr.get(WATERLOGGED));
					if(!target.equals(curr)) {
						fixedAny = true;
						worldIn.setBlockState(cand, target, 2 | 4);
					}
				}
				
				found.addAll(candidates);
				candidates = newCandidates;
				newCandidates = new HashSet<>();
			} while(!candidates.isEmpty());
			
			if(fixedAny)
				return ActionResultType.SUCCESS;
		}
		
		return super.onBlockActivated(state, worldIn, pos, player, handIn, hit);
	}
	
	@Nonnull
	@Override
	@SuppressWarnings("deprecation")
	public FluidState getFluidState(BlockState state) {
		return state.get(WATERLOGGED) ? Fluids.WATER.getStillFluidState(false) : super.getFluidState(state);
	}
	
	@Override
	public void neighborChanged(BlockState state, World worldIn, BlockPos pos, Block blockIn, BlockPos fromPos, boolean isMoving) {
		BlockState targetState = getTargetState(worldIn, pos, state.get(WATERLOGGED));
		if(!targetState.equals(state))
			worldIn.setBlockState(pos, targetState, 2 | 4);
	}
	
	@Override
	public BlockState getStateForPlacement(BlockItemUseContext context) {
		return getTargetState(context.getWorld(), context.getPos(), context.getWorld().getFluidState(context.getPos()).getFluid() == Fluids.WATER);
	}
	
	private BlockState getTargetState(World worldIn, BlockPos pos, boolean waterlog) {
		BlockState newState = getDefaultState();
		newState = newState.with(WATERLOGGED, waterlog);
		
		for(Direction facing : Direction.values()) {
			BooleanProperty prop = CONNECTIONS[facing.ordinal()];
			PipeTileEntity.ConnectionType type = PipeTileEntity.getConnectionTo(worldIn, pos, facing);

			newState = newState.with(prop, type.isSolid);
		}
		
		return newState;
	}
	
	@Override
	public VoxelShape getShape(BlockState state, IBlockReader worldIn, BlockPos pos, ISelectionContext context) {
		int index = 0;
		for(Direction dir : Direction.values()) {
			int ord = dir.ordinal();
			if(state.get(CONNECTIONS[ord]))
				index += (1 << ord);
		}
		
		VoxelShape cached = shapeCache[index];
		if(cached == null) {
			VoxelShape currShape = CENTER_SHAPE;
			
			for(Direction dir : Direction.values()) {
				boolean connected = isConnected(state, dir);
				if(connected)
					currShape = VoxelShapes.or(currShape, SIDE_BOXES[dir.ordinal()]);
			}
			
			shapeCache[index] = currShape;
			cached = currShape;
		}
		
		return cached;
	}

	public static boolean isConnected(BlockState state, Direction side) {
		BooleanProperty prop = CONNECTIONS[side.ordinal()];
		return state.get(prop);
	}

	@Override
	protected void fillStateContainer(Builder<Block, BlockState> builder) {
		builder.add(UP, DOWN, NORTH, SOUTH, WEST, EAST, WATERLOGGED);
	}

	@Override
	public boolean hasComparatorInputOverride(BlockState state) {
		return true;
	}

	@Override
	public int getComparatorInputOverride(BlockState blockState, World worldIn, BlockPos pos) {
		TileEntity tile = worldIn.getTileEntity(pos);
		if(tile instanceof PipeTileEntity)
			return ((PipeTileEntity) tile).getComparatorOutput();
		return 0;
	}
	
	@Override
	public void onPlayerDestroy(IWorld worldIn, BlockPos pos, BlockState state) {
		TileEntity tileentity = worldIn.getTileEntity(pos);

		if(tileentity instanceof PipeTileEntity)
			((PipeTileEntity) tileentity).dropAllItems();

		super.onPlayerDestroy(worldIn, pos, state);
	}

	@Override
	public boolean hasTileEntity(BlockState state) {
		return true;
	}
	
	@Override
	public TileEntity createTileEntity(BlockState state, IBlockReader world) {
		return new PipeTileEntity();	
	}
	
}
