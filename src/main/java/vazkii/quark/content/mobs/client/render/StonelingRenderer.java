package vazkii.quark.content.mobs.client.render;

import net.minecraft.client.renderer.entity.EntityRendererManager;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import vazkii.quark.content.mobs.client.layer.StonelingItemLayer;
import vazkii.quark.content.mobs.client.model.StonelingModel;
import vazkii.quark.content.mobs.entity.StonelingEntity;

import javax.annotation.Nonnull;

@OnlyIn(Dist.CLIENT)
public class StonelingRenderer extends MobRenderer<StonelingEntity, StonelingModel> {

	public StonelingRenderer(EntityRendererManager renderManager) {
		super(renderManager, new StonelingModel(), 0.3F);
		addLayer(new StonelingItemLayer(this));
	}
	
	@Override
	public ResourceLocation getEntityTexture(@Nonnull StonelingEntity entity) {
		return entity.getVariant().getTexture();
	}

}
