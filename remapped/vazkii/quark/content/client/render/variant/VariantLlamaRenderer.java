package vazkii.quark.content.client.render.variant;

import net.minecraft.client.render.entity.EntityRenderDispatcher;
import net.minecraft.client.render.entity.LlamaEntityRenderer;
import net.minecraft.entity.passive.LlamaEntity;
import net.minecraft.util.Identifier;
import vazkii.quark.content.client.module.VariantAnimalTexturesModule;
import vazkii.quark.content.client.module.VariantAnimalTexturesModule.VariantTextureType;

public class VariantLlamaRenderer extends LlamaEntityRenderer {

	public VariantLlamaRenderer(EntityRenderDispatcher renderManagerIn) {
		super(renderManagerIn);
	}

	@Override
	public Identifier getTexture(LlamaEntity entity) {
		return VariantAnimalTexturesModule.getTextureOrShiny(entity, VariantTextureType.LLAMA, () -> super.getTexture(entity));
	}
	
}
