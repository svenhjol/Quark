package vazkii.quark.content.tools.client.render;

import javax.annotation.Nonnull;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.systems.RenderSystem;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererManager;
import net.minecraft.client.renderer.model.ItemCameraTransforms.TransformType;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.vector.Vector3f;
import vazkii.quark.content.tools.entity.PickarangEntity;

public class PickarangRenderer extends EntityRenderer<PickarangEntity> {

	public PickarangRenderer(EntityRendererManager renderManager) {
		super(renderManager);
	}
	
	@Override
	public void render(PickarangEntity entity, float yaw, float partialTicks, MatrixStack matrix, IRenderTypeBuffer buffer, int light) {
		matrix.push();
		matrix.translate(0, 0.2, 0);
		matrix.rotate(Vector3f.XP.rotationDegrees(90F));
		
		Minecraft mc = Minecraft.getInstance();
		float time = entity.ticksExisted + (mc.isGamePaused() ? 0 : partialTicks);
		matrix.rotate(Vector3f.ZP.rotationDegrees(time * 20F));

		mc.getItemRenderer().renderItem(entity.getStack(), TransformType.FIXED, light, OverlayTexture.NO_OVERLAY, matrix, buffer);
		
		matrix.pop();
	}

	@Override
	public ResourceLocation getEntityTexture(@Nonnull PickarangEntity entity) {
		return null;
	}

}
