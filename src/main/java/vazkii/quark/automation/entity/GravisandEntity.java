package vazkii.quark.automation.entity;

import javax.annotation.Nonnull;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.ConcretePowderBlock;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.FallingBlockEntity;
import net.minecraft.entity.MovementType;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.data.TrackedDataHandlerRegistry;
import net.minecraft.item.AutomaticItemPlacementContext;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.Packet;
import net.minecraft.tag.FluidTags;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.GameRules;
import net.minecraft.world.RayTraceContext;
import net.minecraft.world.World;
import net.minecraftforge.fml.network.NetworkHooks;
import vazkii.quark.automation.module.GravisandModule;

public class GravisandEntity extends FallingBlockEntity {

	private static final TrackedData<Float> DIRECTION = DataTracker.registerData(GravisandEntity.class, TrackedDataHandlerRegistry.FLOAT);

	private static final String TAG_DIRECTION = "fallDirection";

	private final BlockState fallTile = GravisandModule.gravisand.getDefaultState();

	public GravisandEntity(EntityType<? extends GravisandEntity> type, World world) {
		super(type, world);
	}

	public GravisandEntity(World world, double x, double y, double z, float direction) {
		this(GravisandModule.gravisandType, world);
		this.inanimate = true;
		this.updatePosition(x, y + (double)((1.0F - this.getHeight()) / 2.0F), z);
		this.setVelocity(Vec3d.ZERO);
		this.prevX = x;
		this.prevY = y;
		this.prevZ = z;
		this.setFallingBlockPos(new BlockPos(getPos()));
		dataTracker.set(DIRECTION, direction);
	}


	@Override
	protected void initDataTracker() {
		super.initDataTracker();

		dataTracker.startTracking(DIRECTION, 0F);
	}

	// Mostly vanilla copy but supporting directional falling
	@Override
	public void tick() {
		Vec3d pos = getPos();
		if (this.fallTile.isAir(world, new BlockPos(getPos())) || pos.y > 300 || pos.y < -50) {
			this.remove();
		} else {
			this.prevX = pos.x;
			this.prevY = pos.y;
			this.prevZ = pos.z;
			Block block = this.fallTile.getBlock();
			if (this.timeFalling++ == 0) {
				BlockPos blockpos = new BlockPos(getPos());
				if (this.world.getBlockState(blockpos).getBlock() == block) {
					this.world.removeBlock(blockpos, false);
				} else if (!this.world.isClient) {
					this.remove();
					return;
				}
			}

			if (!this.hasNoGravity()) {
				this.setVelocity(this.getVelocity().add(0.0D, 0.04D * getFallDirection(), 0.0D));
			}

			this.move(MovementType.SELF, this.getVelocity());
			if (!this.world.isClient) {
				BlockPos fallTarget = new BlockPos(getPos());
				boolean flag = this.fallTile.getBlock() instanceof ConcretePowderBlock;
				boolean flag1 = flag && this.world.getFluidState(fallTarget).isIn(FluidTags.WATER);
				double d0 = this.getVelocity().lengthSquared();
				if (flag && d0 > 1.0D) {
					BlockHitResult blockraytraceresult = this.world.rayTrace(new RayTraceContext(new Vec3d(this.prevX, this.prevY, this.prevZ), pos, RayTraceContext.ShapeType.COLLIDER, RayTraceContext.FluidHandling.SOURCE_ONLY, this));
					if (blockraytraceresult.getType() != HitResult.Type.MISS && this.world.getFluidState(blockraytraceresult.getBlockPos()).isIn(FluidTags.WATER)) {
						fallTarget = blockraytraceresult.getBlockPos();
						flag1 = true;
					}
				}

				if (!verticalCollision && !flag1) {
					if (!this.world.isClient && (this.timeFalling > 100 && (fallTarget.getY() < 1 || fallTarget.getY() > 256) || this.timeFalling > 600)) {
						if (this.dropItem && this.world.getGameRules().getBoolean(GameRules.DO_ENTITY_DROPS)) {
							this.dropItem(block);
						}

						this.remove();
					}
				} else {
					BlockState blockstate = this.world.getBlockState(fallTarget);
					this.setVelocity(this.getVelocity().multiply(0.7D, -0.5D, 0.7D));
					if (blockstate.getBlock() != Blocks.MOVING_PISTON) {
						this.remove();
						Direction facing = getFallDirection() < 0 ? Direction.DOWN : Direction.UP;
						boolean flag2 = blockstate.canReplace(new AutomaticItemPlacementContext(this.world, fallTarget, facing, ItemStack.EMPTY, facing.getOpposite()));
						boolean flag3 = this.fallTile.canPlaceAt(this.world, fallTarget);
						if (flag2 && flag3) {
							this.world.setBlockState(fallTarget, this.fallTile, 3);
						} else if (this.dropItem && this.world.getGameRules().getBoolean(GameRules.DO_ENTITY_DROPS)) {
							this.dropItem(block);
						}
					} else if (this.dropItem && this.world.getGameRules().getBoolean(GameRules.DO_ENTITY_DROPS)) {
						this.dropItem(block);
					}
				}
			}
		}

		this.setVelocity(this.getVelocity().multiply(0.98D));
	}

	@Override
	public boolean handleFallDamage(float distance, float damageMultiplier) {
		return false;
	}

	private float getFallDirection() {
		return dataTracker.get(DIRECTION);
	}

	@Override
	protected void writeCustomDataToTag(CompoundTag compound) {
		super.writeCustomDataToTag(compound);

		compound.putFloat(TAG_DIRECTION, getFallDirection());
	}

	@Override
	protected void readCustomDataFromTag(CompoundTag compound) {
		super.readCustomDataFromTag(compound);

		dataTracker.set(DIRECTION, compound.getFloat(TAG_DIRECTION));
	}

	@Nonnull
	@Override
	public Packet<?> createSpawnPacket() {
		return NetworkHooks.getEntitySpawningPacket(this);
	}

	@Nonnull
	@Override
	public BlockState getBlockState() {
		return fallTile;
	}

}
