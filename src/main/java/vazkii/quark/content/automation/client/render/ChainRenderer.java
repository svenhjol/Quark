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

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.vertex.IVertexBuilder;

import io.netty.util.collection.IntObjectHashMap;
import io.netty.util.collection.IntObjectMap;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.item.minecart.AbstractMinecartEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.vector.Matrix4f;
import net.minecraft.world.LightType;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import vazkii.quark.content.automation.base.ChainHandler;

@OnlyIn(Dist.CLIENT)
public class ChainRenderer {
	private static final IntObjectMap<Entity> RENDER_MAP = new IntObjectHashMap<>();

	private static void renderLeash(EntityRenderer<Entity> renderer, Entity cart, float partialTicks, MatrixStack matrixStack, IRenderTypeBuffer renderBuffer, Entity holder) {
		Entity entity = holder;

		if(entity != null && holder != null) {
			boolean player = entity instanceof PlayerEntity;

			double yaw = MathHelper.lerp((partialTicks * 0.5F), entity.prevRotationYaw, entity.rotationYaw) * Math.PI / 180;
			double pitch = MathHelper.lerp((partialTicks * 0.5F), entity.prevRotationPitch, entity.rotationPitch) * Math.PI / 180;
			double rotX = Math.cos(yaw);
			double rotZ = Math.sin(yaw);
			double rotY = Math.sin(pitch);

			float xLocus = (float) MathHelper.lerp(partialTicks, prevX(entity), entity.getPosX());
			float yLocus = (float) (MathHelper.lerp(partialTicks, prevY(entity), entity.getPosY()));
			float zLocus = (float) MathHelper.lerp(partialTicks, prevZ(entity), entity.getPosZ());

			if (player) {
				xLocus += rotX;
				zLocus += rotZ;

				yLocus += 1.3;
			}

			float targetX = (float) MathHelper.lerp(partialTicks, prevX(cart), cart.getPosX());
			float targetY = (float) MathHelper.lerp(partialTicks, prevY(cart), cart.getPosY());
			float targetZ = (float) MathHelper.lerp(partialTicks, prevZ(cart), cart.getPosZ());
			if (player) {
				xLocus -= rotX;
				zLocus -= rotZ;
			}

			float offsetX = xLocus - targetX;
			float offsetY = yLocus - targetY;
			float offsetZ = zLocus - targetZ;

			IVertexBuilder vertexBuilder = renderBuffer.getBuffer(RenderType.getLeash());

			int lightAtEntity = getBlockLight(entity, partialTicks);
			int lightAtOther = getBlockLight(holder, partialTicks);
			int skyLightAtEntity = entity.world.getLightFor(LightType.SKY, new BlockPos(entity.getEyePosition(partialTicks)));
			int skyLightAtOther = entity.world.getLightFor(LightType.SKY, new BlockPos(holder.getEyePosition(partialTicks)));

			float mag = MathHelper.fastInvSqrt(offsetX * offsetX + offsetZ * offsetZ) * 0.025F / 2.0F;
			float zMag = offsetZ * mag;
			float xMag = offsetX * mag;

			matrixStack.push();
			matrixStack.translate(0, 0.1F, 0);

			Matrix4f matrix = matrixStack.getLast().getMatrix();
			renderSide(vertexBuilder, matrix, offsetX, offsetY, offsetZ, lightAtEntity, lightAtOther, skyLightAtEntity, skyLightAtOther, 0.025F, 0.025F, zMag, xMag);
			renderSide(vertexBuilder, matrix, offsetX, offsetY, offsetZ, lightAtEntity, lightAtOther, skyLightAtEntity, skyLightAtOther, 0.025F, 0.0F, zMag, xMag);
			matrixStack.pop();
		}
	}

	private static int getBlockLight(Entity entityIn, float partialTicks) {
		return entityIn.isBurning() ? 15 : entityIn.world.getLightFor(LightType.BLOCK, new BlockPos(entityIn.getEyePosition(partialTicks)));
	}

	public static void renderSide(IVertexBuilder vertexBuilder, Matrix4f matrix, float dX, float dY, float dZ, int lightAtEntity, int lightAtOther, int skyLightAtEntity, int skyLightAtOther, float width, float rotation, float xMag, float zMag) {
		for(int stepIdx = 0; stepIdx < 24; ++stepIdx) {
			float step = stepIdx / 23.0F;
			int brightness = (int)MathHelper.lerp(step, lightAtEntity, lightAtOther);
			int skyBrightness = (int)MathHelper.lerp(step, skyLightAtEntity, skyLightAtOther);
			int light = LightTexture.packLight(brightness, skyBrightness);
			addVertexPair(vertexBuilder, matrix, light, dX, dY, dZ, width, rotation, 24, stepIdx, false, xMag, zMag);
			addVertexPair(vertexBuilder, matrix, light, dX, dY, dZ, width, rotation, 24, stepIdx + 1, true, xMag, zMag);
		}

	}

	public static void addVertexPair(IVertexBuilder vertexBuilder, Matrix4f matrix, int light, float dX, float dY, float dZ, float width, float rotation, int steps, int stepIdx, boolean leading, float xMag, float zMag) {
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
			vertexBuilder.pos(matrix, x + xMag, y + width - rotation, z - zMag).color(r, g, b, 1.0F).lightmap(light).endVertex();
		}

		vertexBuilder.pos(matrix, x - xMag, y + rotation, z + zMag).color(r, g, b, 1.0F).lightmap(light).endVertex();
		if (leading) {
			vertexBuilder.pos(matrix, x + xMag, y + width - rotation, z - zMag).color(r, g, b, 1.0F).lightmap(light).endVertex();
		}

	}

	public static void renderChain(EntityRenderer<Entity> render, Entity entity, MatrixStack matrixStack, IRenderTypeBuffer renderBuffer, float partTicks) {
		if (ChainHandler.canBeLinked(entity)) {
			Entity holder = RENDER_MAP.get(entity.getEntityId());

			if (holder != null) {
				renderLeash(render, entity, partTicks, matrixStack, renderBuffer, holder);
			}
		}
	}

	private static double prevX(Entity entity) {
		if (entity instanceof AbstractMinecartEntity)
			return entity.lastTickPosX;
		return entity.prevPosX;
	}
	private static double prevY(Entity entity) {
		if (entity instanceof AbstractMinecartEntity)
			return entity.lastTickPosY;
		return entity.prevPosY;
	}
	private static double prevZ(Entity entity) {
		if (entity instanceof AbstractMinecartEntity)
			return entity.lastTickPosZ;
		return entity.prevPosZ;
	}
	private static double renderYawOffset(Entity entity) {
		if (entity instanceof LivingEntity)
			return ((LivingEntity) entity).renderYawOffset;
		return 0;
	}
	private static double prevRenderYawOffset(Entity entity) {
		if (entity instanceof LivingEntity)
			return ((LivingEntity) entity).prevRenderYawOffset;
		return 0;
	}

	public static void updateTick() {
		RENDER_MAP.clear();

		ClientWorld world = Minecraft.getInstance().world;
		if (world == null)
			return;

		for (Entity entity : world.getAllEntities()) {
			if (ChainHandler.canBeLinked(entity)) {
				Entity other = ChainHandler.getLinked(entity);
				if (other != null)
					RENDER_MAP.put(entity.getEntityId(), other);
			}
		}
	}
}
