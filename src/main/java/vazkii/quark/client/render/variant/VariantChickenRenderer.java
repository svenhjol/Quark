package vazkii.quark.client.render.variant;

import net.minecraft.client.render.entity.ChickenEntityRenderer;
import net.minecraft.client.render.entity.EntityRenderDispatcher;
import net.minecraft.entity.passive.ChickenEntity;
import net.minecraft.util.Identifier;
import vazkii.quark.client.module.VariantAnimalTexturesModule;
import vazkii.quark.client.module.VariantAnimalTexturesModule.VariantTextureType;

public class VariantChickenRenderer extends ChickenEntityRenderer {

	public VariantChickenRenderer(EntityRenderDispatcher renderManagerIn) {
		super(renderManagerIn);
	}
	
	@Override
	public Identifier getTexture(ChickenEntity entity) {
		return VariantAnimalTexturesModule.getTextureOrShiny(entity, VariantTextureType.CHICKEN, VariantAnimalTexturesModule.enableChicken);
	}
	
}
