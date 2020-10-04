package vazkii.quark.automation.block;

import java.util.EnumSet;
import java.util.Random;

import javax.annotation.Nonnull;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.RedstoneWireBlock;
import net.minecraft.block.ShapeContext;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.item.ItemStack;
import net.minecraft.particle.DustParticleEffect;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.DirectionProperty;
import net.minecraft.state.property.EnumProperty;
import net.minecraft.state.property.Properties;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.world.BlockView;
import net.minecraft.world.TickPriority;
import net.minecraft.world.World;
import net.minecraft.world.WorldView;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.event.ForgeEventFactory;
import vazkii.quark.automation.base.RandomizerPowerState;
import vazkii.quark.base.block.QuarkBlock;
import vazkii.quark.base.handler.RenderLayerHandler;
import vazkii.quark.base.handler.RenderLayerHandler.RenderTypeSkeleton;
import vazkii.quark.base.module.Module;

/**
 * @author WireSegal
 * Created at 9:57 AM on 8/26/19.
 */

public class RedstoneRandomizerBlock extends QuarkBlock {

    protected static final VoxelShape SHAPE = Block.createCuboidShape(0.0D, 0.0D, 0.0D, 16.0D, 2.0D, 16.0D);

    public static final DirectionProperty FACING = Properties.HORIZONTAL_FACING;
    public static final EnumProperty<RandomizerPowerState> POWERED = EnumProperty.of("powered", RandomizerPowerState.class);

    public RedstoneRandomizerBlock(String regname, Module module, ItemGroup creativeTab, Settings properties) {
        super(regname, module, creativeTab, properties);

        setDefaultState(getDefaultState()
                .with(FACING, Direction.NORTH)
                .with(POWERED, RandomizerPowerState.OFF));
        
		RenderLayerHandler.setRenderType(this, RenderTypeSkeleton.CUTOUT);
    }

    @Override
    @SuppressWarnings("deprecation")
    public void scheduledTick(BlockState state, ServerWorld world, BlockPos pos, Random rand) {
        boolean isPowered = isPowered(state);
        boolean willBePowered = shouldBePowered(world, pos, state);
        if(isPowered != willBePowered) {
            if (!willBePowered)
                state = state.with(POWERED, RandomizerPowerState.OFF);
            else
                state = state.with(POWERED, rand.nextBoolean() ? RandomizerPowerState.LEFT : RandomizerPowerState.RIGHT);

            world.setBlockState(pos, state);
        }
    }

    protected void updateState(World world, BlockPos pos, BlockState state) {
        boolean isPowered = isPowered(state);
        boolean willBePowered = shouldBePowered(world, pos, state);
        if (isPowered != willBePowered && !world.getBlockTickScheduler().isTicking(pos, this)) {
            TickPriority priority = isPowered ? TickPriority.VERY_HIGH : TickPriority.HIGH;

            world.getBlockTickScheduler().schedule(pos, this, 2, priority);
        }

    }

    @Override
    protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
        builder.add(FACING, POWERED);
    }

    @Nonnull
    @Override
    @SuppressWarnings("deprecation")
    public VoxelShape getOutlineShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
        return SHAPE;
    }

    @Override
    @SuppressWarnings("deprecation")
    public boolean canPlaceAt(BlockState state, WorldView world, BlockPos pos) {
        return hasTopRim(world, pos.down());
    }

    protected boolean isPowered(BlockState state) {
        return state.get(POWERED) != RandomizerPowerState.OFF;
    }

    @Override
    @SuppressWarnings("deprecation")
    public int getStrongRedstonePower(BlockState blockState, BlockView blockAccess, BlockPos pos, Direction side) {
        return blockState.getWeakRedstonePower(blockAccess, pos, side);
    }

    @Override
    @SuppressWarnings("deprecation")
    public int getWeakRedstonePower(BlockState blockState, BlockView blockAccess, BlockPos pos, Direction side) {
        RandomizerPowerState powerState = blockState.get(POWERED);
        switch (powerState) {
            case RIGHT:
                return blockState.get(FACING).rotateYClockwise() == side ? 15 : 0;
            case LEFT:
                return blockState.get(FACING).rotateYCounterclockwise() == side ? 15 : 0;
            default:
                return 0;
        }
    }

    @Override
    @SuppressWarnings("deprecation")
    public void neighborUpdate(BlockState state, World world, BlockPos pos, Block blockIn, BlockPos fromPos, boolean isMoving) {
        if (state.canPlaceAt(world, pos))
            this.updateState(world, pos, state);
        else
            breakAndDrop(this, state, world, pos);
    }

    public static void breakAndDrop(Block block, BlockState state, World world, BlockPos pos) {
        dropStacks(state, world, pos, null);
        world.removeBlock(pos, false);

        for(Direction direction : Direction.values())
            world.updateNeighborsAlways(pos.offset(direction), block);
    }

    protected boolean shouldBePowered(World world, BlockPos pos, BlockState state) {
        return this.calculateInputStrength(world, pos, state) > 0;
    }

    protected int calculateInputStrength(World world, BlockPos pos, BlockState state) {
        Direction face = state.get(FACING);
        BlockPos checkPos = pos.offset(face);
        int strength = world.getEmittedRedstonePower(checkPos, face);
        if (strength >= 15) {
            return strength;
        } else {
            BlockState checkState = world.getBlockState(checkPos);
            return Math.max(strength, checkState.getBlock() == Blocks.REDSTONE_WIRE ? checkState.get(RedstoneWireBlock.POWER) : 0);
        }
    }

    @Override
    @SuppressWarnings("deprecation")
    public boolean emitsRedstonePower(BlockState state) {
        return true;
    }

    @Override
    public BlockState getPlacementState(ItemPlacementContext context) {
        return this.getDefaultState().with(FACING, context.getPlayerFacing().getOpposite());
    }

    @Override
    public void onPlaced(World world, BlockPos pos, BlockState state, LivingEntity placer, ItemStack stack) {
        if (this.shouldBePowered(world, pos, state)) {
            world.getBlockTickScheduler().schedule(pos, this, 1);
        }
    }

    @Override
    @SuppressWarnings("deprecation")
    public void onBlockAdded(BlockState state, World world, BlockPos pos, BlockState oldState, boolean isMoving) {
        notifyNeighbors(this, world, pos, state);
    }

    @Override
    @SuppressWarnings("deprecation")
    public void onStateReplaced(BlockState state, @Nonnull World world, @Nonnull BlockPos pos, @Nonnull BlockState newState, boolean isMoving) {
        if (!isMoving && state.getBlock() != newState.getBlock()) {
            super.onStateReplaced(state, world, pos, newState, false);
            notifyNeighbors(this, world, pos, state);
        }
    }

    public static void notifyNeighbors(Block block, World world, BlockPos pos, BlockState state) {
        Direction face = state.get(FACING);
        BlockPos neighborPos = pos.offset(face.getOpposite());
        if (ForgeEventFactory.onNeighborNotify(world, pos, world.getBlockState(pos), EnumSet.of(face.getOpposite()), false).isCanceled())
            return;
        world.updateNeighbor(neighborPos, block, pos);
        world.updateNeighborsExcept(neighborPos, block, face);
    }

    @Environment(EnvType.CLIENT)
    @Override
    public void randomDisplayTick(BlockState stateIn, World worldIn, BlockPos pos, Random rand) {
        if (stateIn.get(POWERED) != RandomizerPowerState.OFF) {
            double x = (double)((double)pos.getX() + 0.5D) + (double)(rand.nextFloat() - 0.5D) * 0.2D;
            double y = (double)((double)pos.getY() + 0.4D) + (double)(rand.nextFloat() - 0.5D) * 0.2D;
            double z = (double)((double)pos.getZ() + 0.5D) + (double)(rand.nextFloat() - 0.5D) * 0.2D;

            worldIn.addParticle(DustParticleEffect.RED, x, y, z, 0.0D, 0.0D, 0.0D);
        }
    }

}
