package vazkii.quark.mixin.client;

import net.minecraft.block.entity.PistonBlockEntity;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.block.entity.PistonBlockEntityRenderer;
import net.minecraft.client.util.math.MatrixStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import vazkii.quark.content.automation.client.render.QuarkPistonTileEntityRenderer;

@Mixin(PistonBlockEntityRenderer.class)
public class PistonTileEntityRendererMixin {

	@Inject(method = "render", at = @At("HEAD"), cancellable = true)
	private void renderPistonBlock(PistonBlockEntity tileEntityIn, float partialTicks, MatrixStack matrixStackIn, VertexConsumerProvider bufferIn, int combinedLightIn, int combinedOverlayIn, CallbackInfo callbackInfo) {
		if(QuarkPistonTileEntityRenderer.renderPistonBlock(tileEntityIn, partialTicks, matrixStackIn, bufferIn, combinedLightIn, combinedOverlayIn))
			callbackInfo.cancel();
	}
}
