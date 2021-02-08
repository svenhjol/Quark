package vazkii.quark.content.mobs.client.render;

import net.minecraft.client.renderer.entity.EntityRendererManager;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.util.ResourceLocation;
import vazkii.quark.content.mobs.client.model.CrabModel;
import vazkii.quark.content.mobs.entity.CrabEntity;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class CrabRenderer extends MobRenderer<CrabEntity, CrabModel> {

	private static final ResourceLocation[] TEXTURES = new ResourceLocation[] {
			new ResourceLocation("quark", "textures/model/entity/crab/red.png"),
			new ResourceLocation("quark", "textures/model/entity/crab/blue.png"),
			new ResourceLocation("quark", "textures/model/entity/crab/green.png")
	};

	public CrabRenderer(EntityRendererManager render) {
		super(render, new CrabModel(), 0.4F);
	}

	@Nullable
	@Override
	public ResourceLocation getEntityTexture(@Nonnull CrabEntity entity) {
		return TEXTURES[Math.min(TEXTURES.length - 1, entity.getVariant())];
	}
}
