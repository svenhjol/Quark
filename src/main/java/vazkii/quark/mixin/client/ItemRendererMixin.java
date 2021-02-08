package vazkii.quark.mixin.client;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.vertex.IVertexBuilder;

import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.ItemRenderer;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.color.ItemColors;
import net.minecraft.client.renderer.model.BakedQuad;
import net.minecraft.client.renderer.model.IBakedModel;
import net.minecraft.client.renderer.model.ItemCameraTransforms;
import net.minecraft.item.ItemStack;
import vazkii.quark.content.tools.module.ColorRunesModule;
import vazkii.quark.content.management.module.ItemSharingModule;

import java.util.List;

@Mixin(ItemRenderer.class)
public abstract class ItemRendererMixin {

	@Inject(method = "renderItem(Lnet/minecraft/item/ItemStack;Lnet/minecraft/client/renderer/model/ItemCameraTransforms$TransformType;ZLcom/mojang/blaze3d/matrix/MatrixStack;Lnet/minecraft/client/renderer/IRenderTypeBuffer;IILnet/minecraft/client/renderer/model/IBakedModel;)V", at = @At("HEAD"))
	private void setColorRuneTargetStack(ItemStack itemStackIn, ItemCameraTransforms.TransformType transformTypeIn, boolean leftHand, MatrixStack matrixStackIn, IRenderTypeBuffer bufferIn, int combinedLightIn, int combinedOverlayIn, IBakedModel modelIn, CallbackInfo callbackInfo) {
		ColorRunesModule.setTargetStack(itemStackIn);
	}

	@Redirect(method = "getArmorVertexBuilder", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/RenderType;getArmorGlint()Lnet/minecraft/client/renderer/RenderType;"))
	private static RenderType getArmorGlint() {
		return ColorRunesModule.getArmorGlint();
	}

	@Redirect(method = "getArmorVertexBuilder", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/RenderType;getArmorEntityGlint()Lnet/minecraft/client/renderer/RenderType;"))
	private static RenderType getArmorEntityGlint() {
		return ColorRunesModule.getArmorEntityGlint();
	}

	@Redirect(method = "getBuffer", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/RenderType;getGlint()Lnet/minecraft/client/renderer/RenderType;"))
	private static RenderType getGlint() {
		return ColorRunesModule.getGlint();
	}	

	@Redirect(method = "getBuffer", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/RenderType;getEntityGlint()Lnet/minecraft/client/renderer/RenderType;"))
	private static RenderType getEntityGlint() {
		return ColorRunesModule.getEntityGlint();
	}

	@Redirect(method = "getEntityGlintVertexBuilder", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/RenderType;getGlintDirect()Lnet/minecraft/client/renderer/RenderType;"))
	private static RenderType getGlintDirect() {
		return ColorRunesModule.getGlintDirect();
	}

	@Redirect(method = "getEntityGlintVertexBuilder", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/RenderType;getEntityGlintDirect()Lnet/minecraft/client/renderer/RenderType;"))
	private static RenderType getEntityGlintDirect() {
		return ColorRunesModule.getEntityGlintDirect();
	}

	@Accessor
	public abstract ItemColors getItemColors();

	@Inject(method = "renderQuads", at = @At(value = "HEAD"), cancellable = true)
	// [VanillaCopy] the entire method lmao
	// Quark: add the alpha value from ItemSharingModule
	public void renderQuads(MatrixStack ms, IVertexBuilder builder, List<BakedQuad> quads, ItemStack stack, int lightmap, int overlay, CallbackInfo ci) {
		if (ItemSharingModule.alphaValue != 1.0F) {
			boolean flag = !stack.isEmpty();
			MatrixStack.Entry entry = ms.getLast();
			
			for(BakedQuad bakedquad : quads) {
				int i = flag && bakedquad.hasTintIndex() ? getItemColors().getColor(stack, bakedquad.getTintIndex()) : -1;

				float r = (i >> 16 & 255) / 255.0F;
				float g = (i >> 8 & 255) / 255.0F;
				float b = (i & 255) / 255.0F;
				builder.addVertexData(entry, bakedquad, r, g, b, ItemSharingModule.alphaValue, lightmap, overlay, true);
			}
			ci.cancel();
		}
	}
}
