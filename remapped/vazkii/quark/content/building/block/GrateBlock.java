package vazkii.quark.content.building.block;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import it.unimi.dsi.fastutil.floats.Float2ObjectArrayMap;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Material;
import net.minecraft.block.ShapeContext;
import net.minecraft.block.Waterloggable;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.ExperienceOrbEntity;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.SpawnRestriction.Location;
import net.minecraft.entity.ai.pathing.NavigationType;
import net.minecraft.entity.ai.pathing.PathNodeType;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.passive.AnimalEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.fluid.FluidState;
import net.minecraft.fluid.Fluids;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.sound.BlockSoundGroup;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.BooleanProperty;
import net.minecraft.state.property.Properties;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.util.shape.VoxelShapes;
import net.minecraft.world.BlockView;
import vazkii.quark.base.block.QuarkBlock;
import vazkii.quark.base.handler.RenderLayerHandler;
import vazkii.quark.base.handler.RenderLayerHandler.RenderTypeSkeleton;
import vazkii.quark.base.module.QuarkModule;

public class GrateBlock extends QuarkBlock implements Waterloggable {

	private static final VoxelShape TRUE_SHAPE = createCuboidShape(0, 15, 0, 16, 16, 16);
	private static final VoxelShape SPAWN_BLOCK_SHAPE = createCuboidShape(0, 15, 0, 16, 32, 16);
	private static final Float2ObjectArrayMap<VoxelShape> WALK_BLOCK_CACHE = new Float2ObjectArrayMap<>();

	public static BooleanProperty WATERLOGGED = Properties.WATERLOGGED;

	public GrateBlock(QuarkModule module) {
		super("grate", module, ItemGroup.DECORATIONS, 
				Block.Properties.of(Material.METAL)
                .strength(5, 10)
                .sounds(BlockSoundGroup.METAL)
                .nonOpaque());

		setDefaultState(getDefaultState().with(WATERLOGGED, false));
		RenderLayerHandler.setRenderType(this, RenderTypeSkeleton.CUTOUT);
	}

	private static VoxelShape createNewBox(double stepHeight) {
		return createCuboidShape(0, 15, 0, 16, 17 + 16 * stepHeight, 16);
	}

	@Override
	public boolean hasDynamicBounds() {
		return true;
	}

	@Nonnull
	@Override
	@SuppressWarnings("deprecation")
	public VoxelShape getOutlineShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
		return TRUE_SHAPE;
	}
	
	private static VoxelShape getCachedShape(float stepHeight) {
		return WALK_BLOCK_CACHE.computeIfAbsent(stepHeight, GrateBlock::createNewBox);
	}

	@Nonnull
	@Override
	@SuppressWarnings("deprecation")
	public VoxelShape getCollisionShape(@Nonnull BlockState state, @Nonnull BlockView world, @Nonnull BlockPos pos, ShapeContext context) {
		Entity entity = context.getEntity();

		if(entity != null) {
			if (entity instanceof ItemEntity || entity instanceof ExperienceOrbEntity)
				return VoxelShapes.empty();

			boolean animal = entity instanceof AnimalEntity;
			boolean leashed = animal && ((AnimalEntity) entity).getHoldingEntity() != null;
			
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
	public PathNodeType getAiPathNodeType(BlockState state, BlockView world, BlockPos pos, @Nullable MobEntity entity) {
		if (entity instanceof AnimalEntity)
			return PathNodeType.DAMAGE_OTHER;
		return null;
	}

	@Nonnull
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
	@SuppressWarnings("deprecation")
	public boolean canPathfindThrough(@Nonnull BlockState state, @Nonnull BlockView world, @Nonnull BlockPos pos, NavigationType path) {
		return false;
	}

	@Override
	public boolean isTranslucent(BlockState state, @Nonnull BlockView world, @Nonnull BlockPos pos) {
		return !state.get(WATERLOGGED);
	}
	
	@Override
	public boolean canCreatureSpawn(BlockState state, BlockView world, BlockPos pos, Location type, EntityType<?> entityType) {
		return false;
	}
	
	@Override
	public boolean hasSidedTransparency(BlockState state) {
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
	public boolean collisionExtendsVertically(BlockState state, BlockView world, BlockPos pos, Entity collidingEntity) {
		return true;
	}

	@Override
	protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
		builder.add(WATERLOGGED);
	}
}
