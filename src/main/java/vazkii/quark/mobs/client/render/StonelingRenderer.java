package vazkii.quark.mobs.client.render;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.render.entity.EntityRenderDispatcher;
import net.minecraft.client.render.entity.MobEntityRenderer;
import net.minecraft.util.Identifier;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import vazkii.quark.mobs.client.layer.StonelingItemLayer;
import vazkii.quark.mobs.client.model.StonelingModel;
import vazkii.quark.mobs.entity.StonelingEntity;

import javax.annotation.Nonnull;

@Environment(EnvType.CLIENT)
public class StonelingRenderer extends MobEntityRenderer<StonelingEntity, StonelingModel> {

	public StonelingRenderer(EntityRenderDispatcher renderManager) {
		super(renderManager, new StonelingModel(), 0.3F);
		addFeature(new StonelingItemLayer(this));
	}
	
	@Override
	public Identifier getEntityTexture(@Nonnull StonelingEntity entity) {
		return entity.getVariant().getTexture();
	}

}
