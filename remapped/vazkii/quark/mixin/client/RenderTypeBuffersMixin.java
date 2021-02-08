package vazkii.quark.mixin.client;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import it.unimi.dsi.fastutil.objects.Object2ObjectLinkedOpenHashMap;
import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.render.BufferBuilderStorage;
import net.minecraft.client.render.RenderLayer;
import vazkii.quark.content.tools.client.GlintRenderType;

@Mixin(BufferBuilderStorage.class)
public class RenderTypeBuffersMixin {

	@Inject(method = "assignBufferBuilder", at = @At("HEAD"))
	private static void addGlintTypes(Object2ObjectLinkedOpenHashMap<RenderLayer, BufferBuilder> mapBuildersIn, RenderLayer renderTypeIn, CallbackInfo callbackInfo) {
		GlintRenderType.addGlintTypes(mapBuildersIn);
	}
}
