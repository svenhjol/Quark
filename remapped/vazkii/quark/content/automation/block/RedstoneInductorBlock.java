package vazkii.quark.content.automation.block;

import java.util.Random;

import javax.annotation.Nonnull;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.block.AbstractRedstoneGateBlock;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.RedstoneWireBlock;
import net.minecraft.block.ShapeContext;
import net.minecraft.client.color.block.BlockColorProvider;
import net.minecraft.client.color.item.ItemColorProvider;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.item.ItemStack;
import net.minecraft.particle.DustParticleEffect;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.BooleanProperty;
import net.minecraft.state.property.DirectionProperty;
import net.minecraft.state.property.IntProperty;
import net.minecraft.state.property.Properties;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.world.BlockView;
import net.minecraft.world.TickPriority;
import net.minecraft.world.World;
import net.minecraft.world.WorldAccess;
import net.minecraft.world.WorldView;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import vazkii.arl.interf.IBlockColorProvider;
import vazkii.quark.base.block.QuarkBlock;
import vazkii.quark.base.handler.RenderLayerHandler;
import vazkii.quark.base.handler.RenderLayerHandler.RenderTypeSkeleton;
import vazkii.quark.base.module.QuarkModule;

/**
 * @author WireSegal
 * Created at 10:37 AM on 8/26/19.
 */
public class RedstoneInductorBlock extends QuarkBlock implements IBlockColorProvider {
    protected static final VoxelShape SHAPE = Block.createCuboidShape(0.0D, 0.0D, 0.0D, 16.0D, 2.0D, 16.0D);

    public static final DirectionProperty FACING = Properties.HORIZONTAL_FACING;
    public static final BooleanProperty LOCKED = Properties.LOCKED;
    public static final IntProperty POWER = Properties.POWER;

    public RedstoneInductorBlock(String regname, QuarkModule module, ItemGroup creativeTab, Settings properties) {
        super(regname, module, creativeTab, properties);

        setDefaultState(getDefaultState()
                .with(FACING, Direction.NORTH)
                .with(LOCKED, false)
                .with(POWER, 0));
        
		RenderLayerHandler.setRenderType(this, RenderTypeSkeleton.CUTOUT);
    }

    @Override
    @SuppressWarnings("deprecation")
    public void scheduledTick(BlockState state, ServerWorld world, BlockPos pos, Random rand) {
        if (!isLocked(world, pos, state)) {
            int currentPower = state.get(POWER);
            int power = this.calculateInputStrength(world, pos, state);
            if (currentPower != power)
                world.setBlockState(pos, state.with(POWER, power));
        }
    }

    protected void updateState(World world, BlockPos pos, BlockState state) {
        if (!isLocked(world, pos, state)) {
            int currentPower = state.get(POWER);
            int power = this.calculateInputStrength(world, pos, state);
            if (currentPower != power && !world.getBlockTickScheduler().isTicking(pos, this)) {
                TickPriority priority = power > 0 ? TickPriority.VERY_HIGH : TickPriority.HIGH;
                world.getBlockTickScheduler().schedule(pos, this, 1, priority);
            }
        }
    }

    protected boolean isLocked(WorldView world, BlockPos pos, BlockState state) {
        return getPowerOnSides(world, pos, state) > 0;
    }

    protected int getPowerOnSides(WorldView worldIn, BlockPos pos, BlockState state) {
        Direction direction = state.get(FACING);
        Direction direction1 = direction.rotateYClockwise();
        Direction direction2 = direction.rotateYCounterclockwise();
        return Math.max(this.getPowerOnSide(worldIn, pos.offset(direction1), direction1), this.getPowerOnSide(worldIn, pos.offset(direction2), direction2));
    }

    protected int getPowerOnSide(WorldView worldIn, BlockPos pos, Direction side) {
        BlockState state = worldIn.getBlockState(pos);
        return AbstractRedstoneGateBlock.isRedstoneGate(state) ? worldIn.getStrongRedstonePower(pos, side) : 0;
    }

    @Override
    protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
        builder.add(FACING, LOCKED, POWER);
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

    @Override
    @SuppressWarnings("deprecation")
    public int getStrongRedstonePower(BlockState blockState, BlockView blockAccess, BlockPos pos, Direction side) {
        return blockState.getWeakRedstonePower(blockAccess, pos, side);
    }

    @Override
    @SuppressWarnings("deprecation")
    public int getWeakRedstonePower(BlockState blockState, BlockView blockAccess, BlockPos pos, Direction side) {
        return blockState.get(FACING) == side ? blockState.get(POWER) : 0;
    }

    @Override
    @SuppressWarnings("deprecation")
    public void neighborUpdate(BlockState state, World world, BlockPos pos, Block blockIn, BlockPos fromPos, boolean isMoving) {
        if (state.canPlaceAt(world, pos))
            this.updateState(world, pos, state);
        else
            RedstoneRandomizerBlock.breakAndDrop(this, state, world, pos);
    }

    protected int calculateInputStrength(World world, BlockPos pos, BlockState state) {
        Direction face = state.get(FACING);
        return Math.min(15, calculateInputStrength(world, pos, face) + calculateInputStrength(world, pos, face.rotateYCounterclockwise()) + calculateInputStrength(world, pos, face.rotateYClockwise()));
    }

    protected int calculateInputStrength(World world, BlockPos pos, Direction face) {
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
        BlockState state = this.getDefaultState().with(FACING, context.getPlayerFacing().getOpposite());
        return state.with(LOCKED, this.isLocked(context.getWorld(), context.getBlockPos(), state));
    }

    @Nonnull
    @Override
    @SuppressWarnings("deprecation")
    public BlockState getStateForNeighborUpdate(@Nonnull BlockState stateIn, Direction facing, BlockState facingState, WorldAccess worldIn, BlockPos currentPos, BlockPos facingPos) {
        return !worldIn.isClient() && facing.getAxis() != stateIn.get(FACING).getAxis() ? stateIn.with(LOCKED, this.isLocked(worldIn, currentPos, stateIn)) : super.getStateForNeighborUpdate(stateIn, facing, facingState, worldIn, currentPos, facingPos);
    }

    @Override
    public void onPlaced(World world, BlockPos pos, BlockState state, LivingEntity placer, ItemStack stack) {
        if (calculateInputStrength(world, pos, state) > 0)
            world.getBlockTickScheduler().schedule(pos, this, 1);
    }

    @Override
    @SuppressWarnings("deprecation")
    public void onBlockAdded(BlockState state, World world, BlockPos pos, BlockState oldState, boolean isMoving) {
        RedstoneRandomizerBlock.notifyNeighbors(this, world, pos, state);
    }

    @Override
    @SuppressWarnings("deprecation")
    public void onStateReplaced(BlockState state, @Nonnull World world, @Nonnull BlockPos pos, @Nonnull BlockState newState, boolean isMoving) {
        if (!isMoving && state.getBlock() != newState.getBlock()) {
            super.onStateReplaced(state, world, pos, newState, false);
            RedstoneRandomizerBlock.notifyNeighbors(this, world, pos, state);
        }
    }

//    @Override does this work?
//    @SuppressWarnings("deprecation")
//    public boolean isSolid(BlockState state) {
//        return true;
//    }

    @Environment(EnvType.CLIENT)
    @Override
    public void randomDisplayTick(BlockState stateIn, World worldIn, BlockPos pos, Random rand) {
        if (stateIn.get(POWER) != 0) {
            double x = (pos.getX() + 0.5D) + (rand.nextFloat() - 0.5D) * 0.2D;
            double y = (pos.getY() + 0.4D) + (rand.nextFloat() - 0.5D) * 0.2D;
            double z = (pos.getZ() + 0.5D) + (rand.nextFloat() - 0.5D) * 0.2D;
            float power = stateIn.get(POWER) / 15f;

            float r = power * 0.6F + 0.4F;
            float g = Math.max(0.0F, power * power * 0.7F - 0.5F);
            float b = Math.max(0.0F, power * power * 0.6F - 0.7F);
            worldIn.addParticle(new DustParticleEffect(r, g, b, 1f), x, y, z, 0.0D, 0.0D, 0.0D);
        }
    }

    @Override
    @Environment(EnvType.CLIENT)
    public BlockColorProvider getBlockColor() {
        return (state, world, pos, index) -> index == 1 ? RedstoneWireBlock.getWireColor(state.get(POWER)) : -1;
    }

    @Override
    @Environment(EnvType.CLIENT)
    public ItemColorProvider getItemColor() {
        return null;
    }
}
