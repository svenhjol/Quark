package vazkii.quark.client.render.variant;

import net.minecraft.client.render.entity.EntityRenderDispatcher;
import net.minecraft.client.render.entity.RabbitEntityRenderer;
import net.minecraft.entity.passive.RabbitEntity;
import net.minecraft.util.Identifier;
import vazkii.quark.client.module.VariantAnimalTexturesModule;
import vazkii.quark.client.module.VariantAnimalTexturesModule.VariantTextureType;

public class VariantRabbitRenderer extends RabbitEntityRenderer {

	public VariantRabbitRenderer(EntityRenderDispatcher renderManagerIn) {
		super(renderManagerIn);
	}
	
	@Override
	public Identifier getTexture(RabbitEntity entity) {
		return VariantAnimalTexturesModule.getTextureOrShiny(entity, VariantTextureType.RABBIT, () -> super.getTexture(entity));
	}

}
