package vazkii.quark.tools.client.render;

import javax.annotation.Nonnull;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.OverlayTexture;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.EntityRenderDispatcher;
import net.minecraft.client.render.entity.EntityRenderer;
import net.minecraft.client.render.model.json.ModelTransformation.Mode;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.client.util.math.Vector3f;
import net.minecraft.util.Identifier;
import com.mojang.blaze3d.systems.RenderSystem;
import vazkii.quark.tools.entity.PickarangEntity;

public class PickarangRenderer extends EntityRenderer<PickarangEntity> {

	public PickarangRenderer(EntityRenderDispatcher renderManager) {
		super(renderManager);
	}
	
	@Override
	public void render(PickarangEntity entity, float yaw, float partialTicks, MatrixStack matrix, VertexConsumerProvider buffer, int light) {
		matrix.push();
		matrix.translate(0, 0.2, 0);
		matrix.multiply(Vector3f.POSITIVE_X.getDegreesQuaternion(90F));
		
		MinecraftClient mc = MinecraftClient.getInstance();
		float time = entity.age + (mc.isPaused() ? 0 : partialTicks);
		matrix.multiply(Vector3f.POSITIVE_Z.getDegreesQuaternion(time * 20F));

		RenderSystem.enableBlend();
		mc.getItemRenderer().renderItem(entity.getStack(), Mode.FIXED, light, OverlayTexture.DEFAULT_UV, matrix, buffer);
		
		matrix.pop();
	}

	@Override
	public Identifier getEntityTexture(@Nonnull PickarangEntity entity) {
		return null;
	}

}
