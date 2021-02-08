package vazkii.quark.content.tools.entity;

import javax.annotation.Nonnull;

import net.minecraft.block.BlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.Packet;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.tag.BlockTags;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.RaycastContext;
import net.minecraft.world.World;
import net.minecraftforge.fml.network.NetworkHooks;
import vazkii.quark.content.tools.module.SkullPikesModule;

public class SkullPikeEntity extends Entity {

	public SkullPikeEntity(EntityType<?> type, World world) {
		super(type, world);
	}

	@Override
	public void tick() {
		super.tick();

		if(world instanceof ServerWorld) {
			boolean good = false;
			BlockPos pos = getBlockPos();
			BlockState state = world.getBlockState(pos);

			if(state.getBlock().isIn(SkullPikesModule.pikeTrophiesTag)) {
				BlockPos down = pos.down();
				BlockState downState = world.getBlockState(down);

				if(downState.getBlock().isIn(BlockTags.FENCES))
					good = true;
			}

			if(!good)
				method_30076();

			ServerWorld sworld = (ServerWorld) world;
			if(Math.random() < 0.4)
				sworld.spawnParticles(Math.random() < 0.05 ? ParticleTypes.WARPED_SPORE : ParticleTypes.ASH, pos.getX() + 0.5, pos.getY() + 0.25, pos.getZ() + 0.5, 1, 0.25, 0.25, 0.25, 0);
		}
	}

	public boolean isVisible(Entity entityIn) {
		Vec3d vector3d = new Vec3d(getX(), getY() + 1, getZ());
		Vec3d vector3d1 = new Vec3d(entityIn.getX(), entityIn.getEyeY(), entityIn.getZ());
		return world.raycast(new RaycastContext(vector3d, vector3d1, RaycastContext.ShapeType.COLLIDER, RaycastContext.FluidHandling.NONE, this)).getType() == HitResult.Type.MISS;
	}

	@Override
	protected void initDataTracker() {
		// NO-OP
	}

	@Override
	protected void readCustomDataFromTag(CompoundTag nbt) {
		// NO-OP
	}

	@Override
	protected void writeCustomDataToTag(CompoundTag nbt) {
		// NO-OP
	}

	@Nonnull
	@Override
	public Packet<?> createSpawnPacket() {
		return NetworkHooks.getEntitySpawningPacket(this);
	}

}
