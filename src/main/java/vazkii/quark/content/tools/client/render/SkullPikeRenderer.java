package vazkii.quark.content.tools.client.render;

import com.mojang.blaze3d.matrix.MatrixStack;

import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererManager;
import net.minecraft.util.ResourceLocation;
import vazkii.quark.content.tools.entity.SkullPikeEntity;

public class SkullPikeRenderer extends EntityRenderer<SkullPikeEntity> {

	public SkullPikeRenderer(EntityRendererManager p_i46179_1_) {
		super(p_i46179_1_);
	}
	
	@Override
	public void render(SkullPikeEntity p_225623_1_, float p_225623_2_, float p_225623_3_, MatrixStack p_225623_4_, IRenderTypeBuffer p_225623_5_, int p_225623_6_) {
	}

	@Override
	public ResourceLocation getEntityTexture(SkullPikeEntity arg0) {
		return null;
	}

}
