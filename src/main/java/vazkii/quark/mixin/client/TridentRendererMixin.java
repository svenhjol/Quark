package vazkii.quark.mixin.client;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import com.mojang.blaze3d.matrix.MatrixStack;

import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.entity.TridentRenderer;
import net.minecraft.entity.projectile.TridentEntity;
import vazkii.quark.content.tools.module.ColorRunesModule;

@Mixin(TridentRenderer.class)
public class TridentRendererMixin {

	@Inject(method = "render", at = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/matrix/MatrixStack;push()V"))
	private void setColorRuneTargetStack(TridentEntity entityIn, float entityYaw, float partialTicks, MatrixStack matrixStackIn, IRenderTypeBuffer bufferIn, int packedLightIn, CallbackInfo callbackInfo) {
		ColorRunesModule.setTargetStack(entityIn.getArrowStack());
	}
	
}
