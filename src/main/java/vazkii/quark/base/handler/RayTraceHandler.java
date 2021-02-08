package vazkii.quark.base.handler;

import org.apache.commons.lang3.tuple.Pair;

import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.attributes.Attributes;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RayTraceContext;
import net.minecraft.util.math.RayTraceContext.BlockMode;
import net.minecraft.util.math.RayTraceContext.FluidMode;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.World;
import net.minecraftforge.common.ForgeMod;
import net.minecraftforge.registries.ForgeRegistries;

public class RayTraceHandler {

	public static RayTraceResult rayTrace(Entity entity, World world, PlayerEntity player, BlockMode blockMode, FluidMode fluidMode) {
		return rayTrace(entity, world, player, blockMode, fluidMode, getEntityRange(player));
	}
	
	public static RayTraceResult rayTrace(Entity entity, World world, Entity player, BlockMode blockMode, FluidMode fluidMode, double range) {
		 Pair<Vector3d, Vector3d> params = getEntityParams(player);
		
		return rayTrace(entity, world, params.getLeft(), params.getRight(), blockMode, fluidMode, range);
	}
	
	public static RayTraceResult rayTrace(Entity entity, World world, Vector3d startPos, Vector3d ray, BlockMode blockMode, FluidMode fluidMode, double range) {
		return rayTrace(entity, world, startPos, ray.scale(range), blockMode, fluidMode);
	}

	public static RayTraceResult rayTrace(Entity entity, World world, Vector3d startPos, Vector3d ray, BlockMode blockMode, FluidMode fluidMode) {
		Vector3d end = startPos.add(ray);
		RayTraceContext context = new RayTraceContext(startPos, end, blockMode, fluidMode, entity);
		return world.rayTraceBlocks(context);
	}
	
	public static double getEntityRange(LivingEntity player) {
		return player.getAttribute(ForgeMod.REACH_DISTANCE.get()).getValue();
	}
	
	public static Pair<Vector3d, Vector3d> getEntityParams(Entity player) {
		float scale = 1.0F;
		float pitch = player.prevRotationPitch + (player.rotationPitch - player.prevRotationPitch) * scale;
		float yaw = player.prevRotationYaw + (player.rotationYaw - player.prevRotationYaw) * scale;
		Vector3d pos = player.getPositionVec();
		double posX = player.prevPosX + (pos.x - player.prevPosX) * scale;
		double posY = player.prevPosY + (pos.y - player.prevPosY) * scale;
		if (player instanceof PlayerEntity)
			posY += ((PlayerEntity) player).getEyeHeight();
		double posZ = player.prevPosZ + (pos.z - player.prevPosZ) * scale;
		Vector3d rayPos = new Vector3d(posX, posY, posZ);

		float zYaw = -MathHelper.cos(yaw * (float) Math.PI / 180);
		float xYaw = MathHelper.sin(yaw * (float) Math.PI / 180);
		float pitchMod = -MathHelper.cos(pitch * (float) Math.PI / 180);
		float azimuth = -MathHelper.sin(pitch * (float) Math.PI / 180);
		float xLen = xYaw * pitchMod;
		float yLen = zYaw * pitchMod;
		Vector3d ray = new Vector3d(xLen, azimuth, yLen);
		
		return Pair.of(rayPos, ray);
	}
	
}
