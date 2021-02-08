package vazkii.quark.base.handler;

import org.apache.commons.lang3.tuple.Pair;

import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.RaycastContext;
import net.minecraft.world.RaycastContext.FluidHandling;
import net.minecraft.world.RaycastContext.ShapeType;
import net.minecraft.world.World;
import net.minecraftforge.common.ForgeMod;
import net.minecraftforge.registries.ForgeRegistries;

public class RayTraceHandler {

	public static HitResult rayTrace(Entity entity, World world, PlayerEntity player, ShapeType blockMode, FluidHandling fluidMode) {
		return rayTrace(entity, world, player, blockMode, fluidMode, getEntityRange(player));
	}
	
	public static HitResult rayTrace(Entity entity, World world, Entity player, ShapeType blockMode, FluidHandling fluidMode, double range) {
		 Pair<Vec3d, Vec3d> params = getEntityParams(player);
		
		return rayTrace(entity, world, params.getLeft(), params.getRight(), blockMode, fluidMode, range);
	}
	
	public static HitResult rayTrace(Entity entity, World world, Vec3d startPos, Vec3d ray, ShapeType blockMode, FluidHandling fluidMode, double range) {
		return rayTrace(entity, world, startPos, ray.multiply(range), blockMode, fluidMode);
	}

	public static HitResult rayTrace(Entity entity, World world, Vec3d startPos, Vec3d ray, ShapeType blockMode, FluidHandling fluidMode) {
		Vec3d end = startPos.add(ray);
		RaycastContext context = new RaycastContext(startPos, end, blockMode, fluidMode, entity);
		return world.raycast(context);
	}
	
	public static double getEntityRange(LivingEntity player) {
		return player.getAttributeInstance(ForgeMod.REACH_DISTANCE.get()).getValue();
	}
	
	public static Pair<Vec3d, Vec3d> getEntityParams(Entity player) {
		float scale = 1.0F;
		float pitch = player.prevPitch + (player.pitch - player.prevPitch) * scale;
		float yaw = player.prevYaw + (player.yaw - player.prevYaw) * scale;
		Vec3d pos = player.getPos();
		double posX = player.prevX + (pos.x - player.prevX) * scale;
		double posY = player.prevY + (pos.y - player.prevY) * scale;
		if (player instanceof PlayerEntity)
			posY += ((PlayerEntity) player).getStandingEyeHeight();
		double posZ = player.prevZ + (pos.z - player.prevZ) * scale;
		Vec3d rayPos = new Vec3d(posX, posY, posZ);

		float zYaw = -MathHelper.cos(yaw * (float) Math.PI / 180);
		float xYaw = MathHelper.sin(yaw * (float) Math.PI / 180);
		float pitchMod = -MathHelper.cos(pitch * (float) Math.PI / 180);
		float azimuth = -MathHelper.sin(pitch * (float) Math.PI / 180);
		float xLen = xYaw * pitchMod;
		float yLen = zYaw * pitchMod;
		Vec3d ray = new Vec3d(xLen, azimuth, yLen);
		
		return Pair.of(rayPos, ray);
	}
	
}
