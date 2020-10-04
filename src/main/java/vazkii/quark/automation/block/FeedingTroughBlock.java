package vazkii.quark.automation.block;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.ShapeContext;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemGroup;
import net.minecraft.screen.NamedScreenHandlerFactory;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.sound.BlockSoundGroup;
import net.minecraft.sound.SoundEvents;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.BooleanProperty;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.ItemScatterer;
import net.minecraft.util.function.BooleanBiFunction;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.util.shape.VoxelShapes;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;
import net.minecraft.world.WorldView;
import vazkii.quark.automation.tile.FeedingTroughTileEntity;
import vazkii.quark.base.block.QuarkBlock;
import vazkii.quark.base.module.Module;

/**
 * @author WireSegal
 * Created at 9:39 AM on 9/20/19.
 */
public class FeedingTroughBlock extends QuarkBlock {

    private static final BlockSoundGroup WOOD_WITH_PLANT_STEP = new BlockSoundGroup(1.0F, 1.0F, SoundEvents.BLOCK_WOOD_BREAK, SoundEvents.BLOCK_GRASS_STEP, SoundEvents.BLOCK_WOOD_PLACE, SoundEvents.BLOCK_WOOD_HIT, SoundEvents.BLOCK_WOOD_FALL);

    public static BooleanProperty FULL = BooleanProperty.of("full");

    public static final VoxelShape CUBOID_SHAPE = createCuboidShape(0, 0, 0, 16, 8, 16);
    public static final VoxelShape EMPTY_SHAPE = VoxelShapes.combineAndSimplify(CUBOID_SHAPE,
            createCuboidShape(2, 2, 2, 14, 8, 14), BooleanBiFunction.ONLY_FIRST);

    public static final VoxelShape FULL_SHAPE = VoxelShapes.combineAndSimplify(CUBOID_SHAPE,
            createCuboidShape(2, 6, 2, 14, 8, 14), BooleanBiFunction.ONLY_FIRST);


    public FeedingTroughBlock(String regname, Module module, ItemGroup creativeTab, Settings properties) {
        super(regname, module, creativeTab, properties);
        setDefaultState(getDefaultState().with(FULL, false));
    }

    @Nonnull
    @Override
    @SuppressWarnings("deprecation")
    public VoxelShape getCollisionShape(@Nonnull BlockState state, @Nonnull BlockView world, @Nonnull BlockPos pos, ShapeContext context) {
        return EMPTY_SHAPE;
    }

    @Nonnull
    @Override
    @SuppressWarnings("deprecation")
    public VoxelShape getRayTraceShape(BlockState state, BlockView world, BlockPos pos) {
        return CUBOID_SHAPE;
    }

    @Nonnull
    @Override
    @SuppressWarnings("deprecation")
    public VoxelShape getOutlineShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
        return state.get(FULL) ? FULL_SHAPE : EMPTY_SHAPE;
    }

    @Override
    protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
        builder.add(FULL);
    }

    @Override
    public BlockSoundGroup getSoundType(BlockState state, WorldView world, BlockPos pos, @Nullable Entity entity) {
        if (state.get(FULL))
            return WOOD_WITH_PLANT_STEP;
        return super.getSoundType(state, world, pos, entity);
    }

    @Override
    public void onLandedUpon(World world, BlockPos pos, Entity entity, float distance) {
        if (world.getBlockState(pos).get(FULL))
            entity.handleFallDamage(distance, 0.2F);
        else
            super.onLandedUpon(world, pos, entity, distance);
    }

    @Override
    @SuppressWarnings("deprecation")
    public void onStateReplaced(BlockState state, @Nonnull World world, @Nonnull BlockPos pos, @Nonnull BlockState newState, boolean isMoving) {
        if (state.getBlock() != newState.getBlock()) {
            BlockEntity tile = world.getBlockEntity(pos);
            if (tile instanceof FeedingTroughTileEntity) {
                ItemScatterer.spawn(world, pos, (FeedingTroughTileEntity)tile);
                world.updateComparators(pos, this);
            }

            super.onStateReplaced(state, world, pos, newState, isMoving);
        }
    }

    @Override
    public boolean hasTileEntity(BlockState state) {
        return true;
    }

    @Override
    public BlockEntity createTileEntity(BlockState state, BlockView world) {
        return new FeedingTroughTileEntity();
    }

    @Override
    @SuppressWarnings("deprecation")
    public boolean hasComparatorOutput(BlockState state) {
        return true;
    }

    @Override
    @SuppressWarnings("deprecation")
    public int getComparatorOutput(BlockState state, World world, BlockPos pos) {
        return ScreenHandler.calculateComparatorOutput(world.getBlockEntity(pos));
    }

    
    @Override
    @SuppressWarnings("deprecation")
    public ActionResult onUse(BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, BlockHitResult trace) {
        if (world.isClient)
            return ActionResult.SUCCESS;
        else {
            NamedScreenHandlerFactory container = this.createScreenHandlerFactory(state, world, pos);
            if (container != null)
                player.openHandledScreen(container);

            return ActionResult.SUCCESS;
        }
    }

    @Override
    @SuppressWarnings("deprecation")
    public boolean onSyncedBlockEvent(BlockState state, World world, BlockPos pos, int id, int param) {
        super.onSyncedBlockEvent(state, world, pos, id, param);
        BlockEntity tile = world.getBlockEntity(pos);
        return tile != null && tile.onSyncedBlockEvent(id, param);
    }

    @Override
    @Nullable
    @SuppressWarnings("deprecation")
    public NamedScreenHandlerFactory createScreenHandlerFactory(BlockState state, World world, BlockPos pos) {
        BlockEntity tile = world.getBlockEntity(pos);
        return tile instanceof NamedScreenHandlerFactory ? (NamedScreenHandlerFactory)tile : null;
    }

}
