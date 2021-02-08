package vazkii.quark.content.tools.client.render;

import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.EntityRenderDispatcher;
import net.minecraft.client.render.entity.EntityRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Identifier;
import vazkii.quark.content.tools.entity.SkullPikeEntity;

public class SkullPikeRenderer extends EntityRenderer<SkullPikeEntity> {

	public SkullPikeRenderer(EntityRenderDispatcher p_i46179_1_) {
		super(p_i46179_1_);
	}
	
	@Override
	public void render(SkullPikeEntity p_225623_1_, float p_225623_2_, float p_225623_3_, MatrixStack p_225623_4_, VertexConsumerProvider p_225623_5_, int p_225623_6_) {
	}

	@Override
	public Identifier getEntityTexture(SkullPikeEntity arg0) {
		return null;
	}

}
