package vazkii.quark.mixin.client;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import net.minecraft.client.color.item.ItemColors;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.item.ItemRenderer;
import net.minecraft.client.render.model.BakedModel;
import net.minecraft.client.render.model.BakedQuad;
import net.minecraft.client.render.model.json.ModelTransformation;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.ItemStack;
import vazkii.quark.content.tools.module.ColorRunesModule;
import vazkii.quark.content.management.module.ItemSharingModule;

import java.util.List;

@Mixin(ItemRenderer.class)
public abstract class ItemRendererMixin {

	@Inject(method = "renderItem(Lnet/minecraft/item/ItemStack;Lnet/minecraft/client/render/model/json/ModelTransformation$Mode;ZLnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider;IILnet/minecraft/client/render/model/BakedModel;)V", at = @At("HEAD"))
	private void setColorRuneTargetStack(ItemStack itemStackIn, ModelTransformation.Mode transformTypeIn, boolean leftHand, MatrixStack matrixStackIn, VertexConsumerProvider bufferIn, int combinedLightIn, int combinedOverlayIn, BakedModel modelIn, CallbackInfo callbackInfo) {
		ColorRunesModule.setTargetStack(itemStackIn);
	}

	@Redirect(method = "getArmorGlintConsumer", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/render/RenderLayer;getArmorGlint()Lnet/minecraft/client/render/RenderLayer;"))
	private static RenderLayer getArmorGlint() {
		return ColorRunesModule.getArmorGlint();
	}

	@Redirect(method = "getArmorGlintConsumer", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/render/RenderLayer;getArmorEntityGlint()Lnet/minecraft/client/render/RenderLayer;"))
	private static RenderLayer getArmorEntityGlint() {
		return ColorRunesModule.getArmorEntityGlint();
	}

	@Redirect(method = "getItemGlintConsumer", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/render/RenderLayer;getGlint()Lnet/minecraft/client/render/RenderLayer;"))
	private static RenderLayer getGlint() {
		return ColorRunesModule.getGlint();
	}	

	@Redirect(method = "getItemGlintConsumer", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/render/RenderLayer;getEntityGlint()Lnet/minecraft/client/render/RenderLayer;"))
	private static RenderLayer getEntityGlint() {
		return ColorRunesModule.getEntityGlint();
	}

	@Redirect(method = "getDirectItemGlintConsumer", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/render/RenderLayer;getDirectGlint()Lnet/minecraft/client/render/RenderLayer;"))
	private static RenderLayer getGlintDirect() {
		return ColorRunesModule.getGlintDirect();
	}

	@Redirect(method = "getDirectItemGlintConsumer", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/render/RenderLayer;getDirectEntityGlint()Lnet/minecraft/client/render/RenderLayer;"))
	private static RenderLayer getEntityGlintDirect() {
		return ColorRunesModule.getEntityGlintDirect();
	}

	@Accessor
	public abstract ItemColors getColorMap();

	@Inject(method = "renderBakedItemQuads", at = @At(value = "HEAD"), cancellable = true)
	// [VanillaCopy] the entire method lmao
	// Quark: add the alpha value from ItemSharingModule
	public void renderQuads(MatrixStack ms, VertexConsumer builder, List<BakedQuad> quads, ItemStack stack, int lightmap, int overlay, CallbackInfo ci) {
		if (ItemSharingModule.alphaValue != 1.0F) {
			boolean flag = !stack.isEmpty();
			MatrixStack.Entry entry = ms.peek();
			
			for(BakedQuad bakedquad : quads) {
				int i = flag && bakedquad.hasColor() ? getColorMap().getColorMultiplier(stack, bakedquad.getColorIndex()) : -1;

				float r = (i >> 16 & 255) / 255.0F;
				float g = (i >> 8 & 255) / 255.0F;
				float b = (i & 255) / 255.0F;
				builder.addVertexData(entry, bakedquad, r, g, b, ItemSharingModule.alphaValue, lightmap, overlay, true);
			}
			ci.cancel();
		}
	}
}
