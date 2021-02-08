package vazkii.quark.mixin.client;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.mojang.blaze3d.matrix.MatrixStack;

import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.tileentity.PistonTileEntityRenderer;
import net.minecraft.tileentity.PistonTileEntity;
import vazkii.quark.content.automation.client.render.QuarkPistonTileEntityRenderer;

@Mixin(PistonTileEntityRenderer.class)
public class PistonTileEntityRendererMixin {

	@Inject(method = "render", at = @At("HEAD"), cancellable = true)
	private void renderPistonBlock(PistonTileEntity tileEntityIn, float partialTicks, MatrixStack matrixStackIn, IRenderTypeBuffer bufferIn, int combinedLightIn, int combinedOverlayIn, CallbackInfo callbackInfo) {
		if(QuarkPistonTileEntityRenderer.renderPistonBlock(tileEntityIn, partialTicks, matrixStackIn, bufferIn, combinedLightIn, combinedOverlayIn))
			callbackInfo.cancel();
	}
}
