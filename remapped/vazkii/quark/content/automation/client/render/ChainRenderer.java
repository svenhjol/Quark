/**
 * This class was created by <WireSegal>. It's distributed as
 * part of the Quark Mod. Get the Source Code in github:
 * https://github.com/Vazkii/Quark
 * <p>
 * Quark is Open Source and distributed under the
 * CC-BY-NC-SA 3.0 License: https://creativecommons.org/licenses/by-nc-sa/3.0/deed.en_GB
 * <p>
 * File Created @ [Jul 17, 2019, 20:06 AM (EST)]
 */
package vazkii.quark.content.automation.client.render;

import io.netty.util.collection.IntObjectHashMap;
import io.netty.util.collection.IntObjectMap;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.LightmapTextureManager;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.EntityRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.vehicle.AbstractMinecartEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Matrix4f;
import net.minecraft.world.LightType;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import vazkii.quark.content.automation.base.ChainHandler;

@Environment(EnvType.CLIENT)
public class ChainRenderer {
	private static final IntObjectMap<Entity> RENDER_MAP = new IntObjectHashMap<>();

	private static void renderLeash(EntityRenderer<Entity> renderer, Entity cart, float partialTicks, MatrixStack matrixStack, VertexConsumerProvider renderBuffer, Entity holder) {
		Entity entity = holder;

		if(entity != null && holder != null) {
			boolean player = entity instanceof PlayerEntity;

			double yaw = MathHelper.lerp((partialTicks * 0.5F), entity.prevYaw, entity.yaw) * Math.PI / 180;
			double pitch = MathHelper.lerp((partialTicks * 0.5F), entity.prevPitch, entity.pitch) * Math.PI / 180;
			double rotX = Math.cos(yaw);
			double rotZ = Math.sin(yaw);
			double rotY = Math.sin(pitch);

			float xLocus = (float) MathHelper.lerp(partialTicks, prevX(entity), entity.getX());
			float yLocus = (float) (MathHelper.lerp(partialTicks, prevY(entity), entity.getY()));
			float zLocus = (float) MathHelper.lerp(partialTicks, prevZ(entity), entity.getZ());

			if (player) {
				xLocus += rotX;
				zLocus += rotZ;

				yLocus += 1.3;
			}

			float targetX = (float) MathHelper.lerp(partialTicks, prevX(cart), cart.getX());
			float targetY = (float) MathHelper.lerp(partialTicks, prevY(cart), cart.getY());
			float targetZ = (float) MathHelper.lerp(partialTicks, prevZ(cart), cart.getZ());
			if (player) {
				xLocus -= rotX;
				zLocus -= rotZ;
			}

			float offsetX = xLocus - targetX;
			float offsetY = yLocus - targetY;
			float offsetZ = zLocus - targetZ;

			VertexConsumer vertexBuilder = renderBuffer.getBuffer(RenderLayer.getLeash());

			int lightAtEntity = getBlockLight(entity, partialTicks);
			int lightAtOther = getBlockLight(holder, partialTicks);
			int skyLightAtEntity = entity.world.getLightLevel(LightType.SKY, new BlockPos(entity.getCameraPosVec(partialTicks)));
			int skyLightAtOther = entity.world.getLightLevel(LightType.SKY, new BlockPos(holder.getCameraPosVec(partialTicks)));

			float mag = MathHelper.fastInverseSqrt(offsetX * offsetX + offsetZ * offsetZ) * 0.025F / 2.0F;
			float zMag = offsetZ * mag;
			float xMag = offsetX * mag;

			matrixStack.push();
			matrixStack.translate(0, 0.1F, 0);

			Matrix4f matrix = matrixStack.peek().getModel();
			renderSide(vertexBuilder, matrix, offsetX, offsetY, offsetZ, lightAtEntity, lightAtOther, skyLightAtEntity, skyLightAtOther, 0.025F, 0.025F, zMag, xMag);
			renderSide(vertexBuilder, matrix, offsetX, offsetY, offsetZ, lightAtEntity, lightAtOther, skyLightAtEntity, skyLightAtOther, 0.025F, 0.0F, zMag, xMag);
			matrixStack.pop();
		}
	}

	private static int getBlockLight(Entity entityIn, float partialTicks) {
		return entityIn.isOnFire() ? 15 : entityIn.world.getLightLevel(LightType.BLOCK, new BlockPos(entityIn.getCameraPosVec(partialTicks)));
	}

	public static void renderSide(VertexConsumer vertexBuilder, Matrix4f matrix, float dX, float dY, float dZ, int lightAtEntity, int lightAtOther, int skyLightAtEntity, int skyLightAtOther, float width, float rotation, float xMag, float zMag) {
		for(int stepIdx = 0; stepIdx < 24; ++stepIdx) {
			float step = stepIdx / 23.0F;
			int brightness = (int)MathHelper.lerp(step, lightAtEntity, lightAtOther);
			int skyBrightness = (int)MathHelper.lerp(step, skyLightAtEntity, skyLightAtOther);
			int light = LightmapTextureManager.pack(brightness, skyBrightness);
			addVertexPair(vertexBuilder, matrix, light, dX, dY, dZ, width, rotation, 24, stepIdx, false, xMag, zMag);
			addVertexPair(vertexBuilder, matrix, light, dX, dY, dZ, width, rotation, 24, stepIdx + 1, true, xMag, zMag);
		}

	}

	public static void addVertexPair(VertexConsumer vertexBuilder, Matrix4f matrix, int light, float dX, float dY, float dZ, float width, float rotation, int steps, int stepIdx, boolean leading, float xMag, float zMag) {
		float r = 0.3F;
		float g = 0.3F;
		float b = 0.3F;
		if (stepIdx % 2 == 0) {
			r *= 0.7F;
			g *= 0.7F;
			b *= 0.7F;
		}

		float step = (float)stepIdx / steps;
		float x = dX * step;
		float y = dY * (step * step + step) * 0.5F; //((float)steps - stepIdx) / (steps * 0.75F) + 0.125F;
		float z = dZ * step;
		if (!leading) {
			vertexBuilder.vertex(matrix, x + xMag, y + width - rotation, z - zMag).color(r, g, b, 1.0F).light(light).next();
		}

		vertexBuilder.vertex(matrix, x - xMag, y + rotation, z + zMag).color(r, g, b, 1.0F).light(light).next();
		if (leading) {
			vertexBuilder.vertex(matrix, x + xMag, y + width - rotation, z - zMag).color(r, g, b, 1.0F).light(light).next();
		}

	}

	public static void renderChain(EntityRenderer<Entity> render, Entity entity, MatrixStack matrixStack, VertexConsumerProvider renderBuffer, float partTicks) {
		if (ChainHandler.canBeLinked(entity)) {
			Entity holder = RENDER_MAP.get(entity.getEntityId());

			if (holder != null) {
				renderLeash(render, entity, partTicks, matrixStack, renderBuffer, holder);
			}
		}
	}

	private static double prevX(Entity entity) {
		if (entity instanceof AbstractMinecartEntity)
			return entity.lastRenderX;
		return entity.prevX;
	}
	private static double prevY(Entity entity) {
		if (entity instanceof AbstractMinecartEntity)
			return entity.lastRenderY;
		return entity.prevY;
	}
	private static double prevZ(Entity entity) {
		if (entity instanceof AbstractMinecartEntity)
			return entity.lastRenderZ;
		return entity.prevZ;
	}
	private static double renderYawOffset(Entity entity) {
		if (entity instanceof LivingEntity)
			return ((LivingEntity) entity).bodyYaw;
		return 0;
	}
	private static double prevRenderYawOffset(Entity entity) {
		if (entity instanceof LivingEntity)
			return ((LivingEntity) entity).prevBodyYaw;
		return 0;
	}

	public static void updateTick() {
		RENDER_MAP.clear();

		ClientWorld world = MinecraftClient.getInstance().world;
		if (world == null)
			return;

		for (Entity entity : world.getEntities()) {
			if (ChainHandler.canBeLinked(entity)) {
				Entity other = ChainHandler.getLinked(entity);
				if (other != null)
					RENDER_MAP.put(entity.getEntityId(), other);
			}
		}
	}
}
