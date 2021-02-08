package vazkii.quark.mixin.client;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.EntityRenderDispatcher;
import net.minecraft.client.render.entity.EntityRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;
import vazkii.quark.content.automation.client.render.ChainRenderer;

@Mixin(EntityRenderDispatcher.class)
public class EntityRendererManagerMixin {

	@Shadow
	public native <T extends Entity> EntityRenderer<? super T> getRenderer(T entityIn);

	@Inject(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/render/entity/EntityRenderer;render(Lnet/minecraft/entity/Entity;FFLnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider;I)V", shift = At.Shift.AFTER))
	@SuppressWarnings("unchecked")
	private <E extends Entity> void renderChain(E entityIn, double xIn, double yIn, double zIn, float rotationYawIn, float partialTicks, MatrixStack matrixStackIn, VertexConsumerProvider bufferIn, int packedLightIn, CallbackInfo ci) {
		ChainRenderer.renderChain((EntityRenderer<Entity>) getRenderer(entityIn), entityIn, matrixStackIn, bufferIn, partialTicks);
	}
}
