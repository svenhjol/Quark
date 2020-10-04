package vazkii.quark.client.render.variant;

import net.minecraft.client.render.entity.CowEntityRenderer;
import net.minecraft.client.render.entity.EntityRenderDispatcher;
import net.minecraft.entity.passive.CowEntity;
import net.minecraft.util.Identifier;
import vazkii.quark.client.module.VariantAnimalTexturesModule;
import vazkii.quark.client.module.VariantAnimalTexturesModule.VariantTextureType;

public class VariantCowRenderer extends CowEntityRenderer {

	public VariantCowRenderer(EntityRenderDispatcher renderManagerIn) {
		super(renderManagerIn);
	}
	
	@Override
	public Identifier getTexture(CowEntity entity) {
		return VariantAnimalTexturesModule.getTextureOrShiny(entity, VariantTextureType.COW, VariantAnimalTexturesModule.enableCow);
	}
	
}
