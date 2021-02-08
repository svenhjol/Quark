package vazkii.quark.content.building.block;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import it.unimi.dsi.fastutil.floats.Float2ObjectArrayMap;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.IWaterLoggable;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.MobEntity;
import net.minecraft.entity.EntitySpawnPlacementRegistry.PlacementType;
import net.minecraft.entity.item.ExperienceOrbEntity;
import net.minecraft.entity.item.ItemEntity;
import net.minecraft.entity.passive.AnimalEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.fluid.FluidState;
import net.minecraft.fluid.Fluids;
import net.minecraft.item.BlockItemUseContext;
import net.minecraft.item.ItemGroup;
import net.minecraft.pathfinding.PathNodeType;
import net.minecraft.pathfinding.PathType;
import net.minecraft.state.BooleanProperty;
import net.minecraft.state.StateContainer;
import net.minecraft.state.properties.BlockStateProperties;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.shapes.ISelectionContext;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.util.math.shapes.VoxelShapes;
import net.minecraft.world.IBlockReader;
import vazkii.quark.base.block.QuarkBlock;
import vazkii.quark.base.handler.RenderLayerHandler;
import vazkii.quark.base.handler.RenderLayerHandler.RenderTypeSkeleton;
import vazkii.quark.base.module.QuarkModule;

public class GrateBlock extends QuarkBlock implements IWaterLoggable {

	private static final VoxelShape TRUE_SHAPE = makeCuboidShape(0, 15, 0, 16, 16, 16);
	private static final VoxelShape SPAWN_BLOCK_SHAPE = makeCuboidShape(0, 15, 0, 16, 32, 16);
	private static final Float2ObjectArrayMap<VoxelShape> WALK_BLOCK_CACHE = new Float2ObjectArrayMap<>();

	public static BooleanProperty WATERLOGGED = BlockStateProperties.WATERLOGGED;

	public GrateBlock(QuarkModule module) {
		super("grate", module, ItemGroup.DECORATIONS, 
				Block.Properties.create(Material.IRON)
                .hardnessAndResistance(5, 10)
                .sound(SoundType.METAL)
                .notSolid());

		setDefaultState(getDefaultState().with(WATERLOGGED, false));
		RenderLayerHandler.setRenderType(this, RenderTypeSkeleton.CUTOUT);
	}

	private static VoxelShape createNewBox(double stepHeight) {
		return makeCuboidShape(0, 15, 0, 16, 17 + 16 * stepHeight, 16);
	}

	@Override
	public boolean isVariableOpacity() {
		return true;
	}

	@Nonnull
	@Override
	@SuppressWarnings("deprecation")
	public VoxelShape getShape(BlockState state, IBlockReader world, BlockPos pos, ISelectionContext context) {
		return TRUE_SHAPE;
	}
	
	private static VoxelShape getCachedShape(float stepHeight) {
		return WALK_BLOCK_CACHE.computeIfAbsent(stepHeight, GrateBlock::createNewBox);
	}

	@Nonnull
	@Override
	@SuppressWarnings("deprecation")
	public VoxelShape getCollisionShape(@Nonnull BlockState state, @Nonnull IBlockReader world, @Nonnull BlockPos pos, ISelectionContext context) {
		Entity entity = context.getEntity();

		if(entity != null) {
			if (entity instanceof ItemEntity || entity instanceof ExperienceOrbEntity)
				return VoxelShapes.empty();

			boolean animal = entity instanceof AnimalEntity;
			boolean leashed = animal && ((AnimalEntity) entity).getLeashHolder() != null;
			
			if (animal && !leashed)
				return getCachedShape(entity.stepHeight);

			if(entity instanceof MobEntity && !leashed)
				return SPAWN_BLOCK_SHAPE;

			return TRUE_SHAPE;
		}

		return TRUE_SHAPE;
	}

	@Nullable
	@Override
	public PathNodeType getAiPathNodeType(BlockState state, IBlockReader world, BlockPos pos, @Nullable MobEntity entity) {
		if (entity instanceof AnimalEntity)
			return PathNodeType.DAMAGE_OTHER;
		return null;
	}

	@Nonnull
	@Override
	@SuppressWarnings("deprecation")
	public FluidState getFluidState(BlockState state) {
		return state.get(WATERLOGGED) ? Fluids.WATER.getStillFluidState(false) : super.getFluidState(state);
	}
	
	@Override
	public BlockState getStateForPlacement(BlockItemUseContext context) {
		return getDefaultState().with(WATERLOGGED, context.getWorld().getFluidState(context.getPos()).getFluid() == Fluids.WATER);
	}

	@Override
	@SuppressWarnings("deprecation")
	public boolean allowsMovement(@Nonnull BlockState state, @Nonnull IBlockReader world, @Nonnull BlockPos pos, PathType path) {
		return false;
	}

	@Override
	public boolean propagatesSkylightDown(BlockState state, @Nonnull IBlockReader world, @Nonnull BlockPos pos) {
		return !state.get(WATERLOGGED);
	}
	
	@Override
	public boolean canCreatureSpawn(BlockState state, IBlockReader world, BlockPos pos, PlacementType type, EntityType<?> entityType) {
		return false;
	}
	
	@Override
	public boolean isTransparent(BlockState state) {
		return true;
	}
	
//	@Override
//	@SuppressWarnings("deprecation")
//	public boolean causesSuffocation(@Nonnull BlockState state, @Nonnull IBlockReader world, @Nonnull BlockPos pos) {
//		return false;
//	}
//
//	@Override
//	@SuppressWarnings("deprecation")
//	public boolean isNormalCube(BlockState state, @Nonnull IBlockReader world, @Nonnull BlockPos pos) {
//		return false;
//	}

	@Override
	public boolean collisionExtendsVertically(BlockState state, IBlockReader world, BlockPos pos, Entity collidingEntity) {
		return true;
	}

	@Override
	protected void fillStateContainer(StateContainer.Builder<Block, BlockState> builder) {
		builder.add(WATERLOGGED);
	}
}
