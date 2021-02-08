package vazkii.quark.addons.oddities.tile;

import java.util.List;

import javax.annotation.Nonnull;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.piston.PistonBehavior;
import net.minecraft.entity.Entity;
import net.minecraft.entity.MovementType;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtHelper;
import net.minecraft.sound.BlockSoundGroup;
import net.minecraft.sound.SoundCategory;
import net.minecraft.state.property.Properties;
import net.minecraft.util.Tickable;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.util.shape.VoxelShapes;
import net.minecraft.world.BlockView;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import vazkii.quark.addons.oddities.magnetsystem.MagnetSystem;
import vazkii.quark.addons.oddities.module.MagnetsModule;
import vazkii.quark.api.IMagnetMoveAction;

public class MagnetizedBlockTileEntity extends BlockEntity implements Tickable {
    private BlockState magnetState;
    private CompoundTag subTile;
    private Direction magnetFacing;
    private static final ThreadLocal<Direction> MOVING_ENTITY = ThreadLocal.withInitial(() -> null);
    private float progress;
    private float lastProgress;
    private long lastTicked;

    public MagnetizedBlockTileEntity() {
        super(MagnetsModule.magnetizedBlockType);
    }

    public MagnetizedBlockTileEntity(BlockState magnetStateIn, CompoundTag subTileIn, Direction magnetFacingIn) {
        this();
        this.magnetState = magnetStateIn;
        this.subTile = subTileIn;
        this.magnetFacing = magnetFacingIn;
    }

    public Direction getFacing() {
        return this.magnetFacing;
    }

    public float getProgress(float ticks) {
        if (ticks > 1.0F) {
            ticks = 1.0F;
        }

        return MathHelper.lerp(ticks, this.lastProgress, this.progress);
    }

    @Environment(EnvType.CLIENT)
    public float getOffsetX(float ticks) {
        return this.magnetFacing.getOffsetX() * this.getExtendedProgress(this.getProgress(ticks));
    }

    @Environment(EnvType.CLIENT)
    public float getOffsetY(float ticks) {
        return this.magnetFacing.getOffsetY() * this.getExtendedProgress(this.getProgress(ticks));
    }

    @Environment(EnvType.CLIENT)
    public float getOffsetZ(float ticks) {
        return this.magnetFacing.getOffsetZ() * this.getExtendedProgress(this.getProgress(ticks));
    }

    private float getExtendedProgress(float partialTicks) {
        return partialTicks - 1.0F;
    }

    private void moveCollidedEntities(float progress) {
        if (this.world == null)
            return;

        Direction direction = this.magnetFacing;
        double movement = (progress - this.progress);
        VoxelShape collision = magnetState.getCollisionShape(this.world, this.getPos());
        if (!collision.isEmpty()) {
            List<Box> boundingBoxes = collision.getBoundingBoxes();
            Box containingBox = this.moveByPositionAndProgress(this.getEnclosingBox(boundingBoxes));
            List<Entity> entities = this.world.getOtherEntities(null, this.getMovementArea(containingBox, direction, movement).union(containingBox));
            if (!entities.isEmpty()) {
                boolean sticky = this.magnetState.getBlock().isStickyBlock(this.magnetState);

                for (Entity entity : entities) {
                    if (entity.getPistonBehavior() != PistonBehavior.IGNORE) {
                        if (sticky) {
                            Vec3d motion = entity.getVelocity();
                            double dX = motion.x;
                            double dY = motion.y;
                            double dZ = motion.z;
                            switch (direction.getAxis()) {
                                case X:
                                    dX = direction.getOffsetX();
                                    break;
                                case Y:
                                    dY = direction.getOffsetY();
                                    break;
                                case Z:
                                    dZ = direction.getOffsetZ();
                            }

                            entity.setVelocity(dX, dY, dZ);
                        }

                        double motion = 0.0D;

                        for (Box aList : boundingBoxes) {
                            Box movementArea = this.getMovementArea(this.moveByPositionAndProgress(aList), direction, movement);
                            Box entityBox = entity.getBoundingBox();
                            if (movementArea.intersects(entityBox)) {
                                motion = Math.max(motion, this.getMovement(movementArea, direction, entityBox));
                                if (motion >= movement) {
                                    break;
                                }
                            }
                        }

                        if (motion > 0) {
                            motion = Math.min(motion, movement) + 0.01D;
                            MOVING_ENTITY.set(direction);
                            entity.move(MovementType.PISTON, new Vec3d(motion * direction.getOffsetX(), motion * direction.getOffsetY(), motion * direction.getOffsetZ()));
                            MOVING_ENTITY.set(null);
                        }
                    }
                }

            }
        }
    }

    private Box getEnclosingBox(List<Box> boxes) {
        double minX = 0.0D;
        double minY = 0.0D;
        double minZ = 0.0D;
        double maxX = 1.0D;
        double maxY = 1.0D;
        double maxZ = 1.0D;

        for(Box bb : boxes) {
            minX = Math.min(bb.minX, minX);
            minY = Math.min(bb.minY, minY);
            minZ = Math.min(bb.minZ, minZ);
            maxX = Math.max(bb.maxX, maxX);
            maxY = Math.max(bb.maxY, maxY);
            maxZ = Math.max(bb.maxZ, maxZ);
        }

        return new Box(minX, minY, minZ, maxX, maxY, maxZ);
    }

    private double getMovement(Box bb1, Direction facing, Box bb2) {
        switch(facing.getAxis()) {
            case X:
                return getDeltaX(bb1, facing, bb2);
            case Z:
                return getDeltaZ(bb1, facing, bb2);
            default:
                return getDeltaY(bb1, facing, bb2);
        }
    }

    private Box moveByPositionAndProgress(Box bb) {
        double progress = this.getExtendedProgress(this.progress);
        return bb.offset(this.pos.getX() + progress * this.magnetFacing.getOffsetX(), this.pos.getY() + progress * this.magnetFacing.getOffsetY(), this.pos.getZ() + progress * this.magnetFacing.getOffsetZ());
    }

    private Box getMovementArea(Box bb, Direction dir, double movement) {
        double d0 = movement * dir.getDirection().offset();
        double d1 = Math.min(d0, 0.0D);
        double d2 = Math.max(d0, 0.0D);
        switch(dir) {
            case WEST:
                return new Box(bb.minX + d1, bb.minY, bb.minZ, bb.minX + d2, bb.maxY, bb.maxZ);
            case EAST:
                return new Box(bb.maxX + d1, bb.minY, bb.minZ, bb.maxX + d2, bb.maxY, bb.maxZ);
            case DOWN:
                return new Box(bb.minX, bb.minY + d1, bb.minZ, bb.maxX, bb.minY + d2, bb.maxZ);
            case NORTH:
                return new Box(bb.minX, bb.minY, bb.minZ + d1, bb.maxX, bb.maxY, bb.minZ + d2);
            case SOUTH:
                return new Box(bb.minX, bb.minY, bb.maxZ + d1, bb.maxX, bb.maxY, bb.maxZ + d2);
            default:
                return new Box(bb.minX, bb.maxY + d1, bb.minZ, bb.maxX, bb.maxY + d2, bb.maxZ);
        }
    }

    private static double getDeltaX(Box bb1, Direction facing, Box bb2) {
        return facing.getDirection() == Direction.AxisDirection.POSITIVE ? bb1.maxX - bb2.minX : bb2.maxX - bb1.minX;
    }

    private static double getDeltaY(Box bb1, Direction facing, Box bb2) {
        return facing.getDirection() == Direction.AxisDirection.POSITIVE ? bb1.maxY - bb2.minY : bb2.maxY - bb1.minY;
    }

    private static double getDeltaZ(Box bb1, Direction facing, Box bb2) {
        return facing.getDirection() == Direction.AxisDirection.POSITIVE ? bb1.maxZ - bb2.minZ : bb2.maxZ - bb1.minZ;
    }

    public BlockState getMagnetState() {
        return this.magnetState;
    }


    private IMagnetMoveAction getMoveAction() {
        Block block = magnetState.getBlock();
        if(block instanceof IMagnetMoveAction)
            return (IMagnetMoveAction) block;

        return MagnetSystem.getMoveAction(block);
    }

    public void finalizeContents(BlockState blockState) {
        if (world == null || world.isClient)
            return;

        BlockSoundGroup soundType = blockState.getSoundGroup();
        world.playSound(null, pos, soundType.getPlaceSound(), SoundCategory.BLOCKS, (soundType.getVolume() + 1) * 0.05F, soundType.getPitch() * 0.8F);

        BlockEntity newTile = getSubTile();
        if (newTile != null)
            world.setBlockEntity(pos, newTile);

        IMagnetMoveAction action = getMoveAction();
        if(action != null)
            action.onMagnetMoved(world, pos, magnetFacing, blockState, newTile);
    }
    
    public BlockEntity getSubTile() {
        if (subTile != null && !subTile.isEmpty()) {
            CompoundTag tileData = subTile.copy();
            tileData.putInt("x", this.pos.getX());
            tileData.putInt("y", this.pos.getY());
            tileData.putInt("z", this.pos.getZ());
            return BlockEntity.createFromTag(magnetState, subTile);
        }
        
        return null;
    }

    public void clearMagnetTileEntity() {
        if (this.lastProgress < 1.0F && this.world != null) {
            this.progress = 1.0F;
            this.lastProgress = this.progress;

            this.world.removeBlockEntity(this.pos);
            this.markRemoved();
            if (this.world.getBlockState(this.pos).getBlock() == MagnetsModule.magnetized_block) {
                BlockState blockstate = Block.postProcessState(this.magnetState, this.world, this.pos);

                this.world.setBlockState(this.pos, blockstate, 3);
                this.world.updateNeighbor(this.pos, blockstate.getBlock(), this.pos);

                finalizeContents(blockstate);
            }
        }

    }

    @Override
    @SuppressWarnings("deprecation")
    public void tick() {
        if (this.world == null)
            return;
        this.lastTicked = this.world.getTime();
        this.lastProgress = this.progress;
        if (this.lastProgress >= 1.0F) {
            this.world.removeBlockEntity(this.pos);
            this.markRemoved();
            if (this.magnetState != null && this.world.getBlockState(this.pos).getBlock() == MagnetsModule.magnetized_block) {
                BlockState blockstate = Block.postProcessState(this.magnetState, this.world, this.pos);
                if (blockstate.isAir()) {
                    this.world.setBlockState(this.pos, this.magnetState, 84);
                    Block.replace(this.magnetState, blockstate, this.world, this.pos, 3);
                } else {
                    if (blockstate.getEntries().containsKey(Properties.WATERLOGGED) && blockstate.get(Properties.WATERLOGGED)) {
                        blockstate = blockstate.with(Properties.WATERLOGGED, Boolean.FALSE);
                    }

                    this.world.setBlockState(this.pos, blockstate, 67);
                    this.world.updateNeighbor(this.pos, blockstate.getBlock(), this.pos);

                    finalizeContents(blockstate);
                }
            }

        } else {
            float newProgress = this.progress + 0.5F;
            this.moveCollidedEntities(newProgress);
            this.progress = newProgress;
            if (this.progress >= 1.0F) {
                this.progress = 1.0F;
            }

        }
    }
    
    
    @Override
    public void fromTag(BlockState p_230337_1_, CompoundTag compound) {
    	super.fromTag(p_230337_1_, compound);
    	
        this.magnetState = NbtHelper.toBlockState(compound.getCompound("blockState"));
        this.magnetFacing = Direction.byId(compound.getInt("facing"));
        this.progress = compound.getFloat("progress");
        this.lastProgress = this.progress;
        this.subTile = compound.getCompound("subTile");
    }

    @Override
    @Nonnull
    public CompoundTag toInitialChunkDataTag() {
        return writeNBTData(super.toTag(new CompoundTag()), false);
    }

    @Nonnull
    @Override
    public CompoundTag toTag(CompoundTag compound) {
        return writeNBTData(super.toTag(new CompoundTag()), true);
    }

    private CompoundTag writeNBTData(CompoundTag compound, boolean includeSubTile) {
        compound.put("blockState", NbtHelper.fromBlockState(this.magnetState));
        if (includeSubTile)
            compound.put("subTile", subTile);
        compound.putInt("facing", this.magnetFacing.getId());
        compound.putFloat("progress", this.lastProgress);
        return compound;
    }

    public VoxelShape getCollisionShape(BlockView world, BlockPos pos) {
        Direction direction = MOVING_ENTITY.get();
        if (this.progress < 1.0D && direction == this.magnetFacing) {
            return VoxelShapes.empty();
        } else {

            float progress = this.getExtendedProgress(this.progress);
            double dX = this.magnetFacing.getOffsetX() * progress;
            double dY = this.magnetFacing.getOffsetY() * progress;
            double dZ = this.magnetFacing.getOffsetZ() * progress;
            return magnetState.getCollisionShape(world, pos).offset(dX, dY, dZ);
        }
    }

    public long getLastTicked() {
        return this.lastTicked;
    }
}
