package vazkii.quark.content.tools.entity;

import javax.annotation.Nonnull;

import net.minecraft.block.BlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.IPacket;
import net.minecraft.particles.ParticleTypes;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceContext;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;
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
			BlockPos pos = getPosition();
			BlockState state = world.getBlockState(pos);

			if(state.getBlock().isIn(SkullPikesModule.pikeTrophiesTag)) {
				BlockPos down = pos.down();
				BlockState downState = world.getBlockState(down);

				if(downState.getBlock().isIn(BlockTags.FENCES))
					good = true;
			}

			if(!good)
				setDead();

			ServerWorld sworld = (ServerWorld) world;
			if(Math.random() < 0.4)
				sworld.spawnParticle(Math.random() < 0.05 ? ParticleTypes.WARPED_SPORE : ParticleTypes.ASH, pos.getX() + 0.5, pos.getY() + 0.25, pos.getZ() + 0.5, 1, 0.25, 0.25, 0.25, 0);
		}
	}

	public boolean isVisible(Entity entityIn) {
		Vector3d vector3d = new Vector3d(getPosX(), getPosY() + 1, getPosZ());
		Vector3d vector3d1 = new Vector3d(entityIn.getPosX(), entityIn.getPosYEye(), entityIn.getPosZ());
		return world.rayTraceBlocks(new RayTraceContext(vector3d, vector3d1, RayTraceContext.BlockMode.COLLIDER, RayTraceContext.FluidMode.NONE, this)).getType() == RayTraceResult.Type.MISS;
	}

	@Override
	protected void registerData() {
		// NO-OP
	}

	@Override
	protected void readAdditional(CompoundNBT nbt) {
		// NO-OP
	}

	@Override
	protected void writeAdditional(CompoundNBT nbt) {
		// NO-OP
	}

	@Nonnull
	@Override
	public IPacket<?> createSpawnPacket() {
		return NetworkHooks.getEntitySpawningPacket(this);
	}

}
