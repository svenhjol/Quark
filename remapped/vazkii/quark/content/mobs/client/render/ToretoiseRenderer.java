package vazkii.quark.content.mobs.client.render;

import net.minecraft.client.render.entity.EntityRenderDispatcher;
import net.minecraft.client.render.entity.MobEntityRenderer;
import net.minecraft.util.Identifier;
import vazkii.quark.base.Quark;
import vazkii.quark.content.mobs.client.layer.ToretoiseOreLayer;
import vazkii.quark.content.mobs.client.model.ToretoiseModel;
import vazkii.quark.content.mobs.entity.ToretoiseEntity;

public class ToretoiseRenderer extends MobEntityRenderer<ToretoiseEntity, ToretoiseModel>{

	private static final Identifier BASE_TEXTURE = new Identifier(Quark.MOD_ID, "textures/model/entity/toretoise/base.png");
	
	public ToretoiseRenderer(EntityRenderDispatcher m) {
		super(m, new ToretoiseModel(), 1F);
		addFeature(new ToretoiseOreLayer(this));
	}

	@Override
	public Identifier getEntityTexture(ToretoiseEntity entity) {
		return BASE_TEXTURE;
	}

}
