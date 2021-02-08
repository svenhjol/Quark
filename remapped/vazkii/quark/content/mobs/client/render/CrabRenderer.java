package vazkii.quark.content.mobs.client.render;

import vazkii.quark.content.mobs.client.model.CrabModel;
import vazkii.quark.content.mobs.entity.CrabEntity;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import net.minecraft.client.render.entity.EntityRenderDispatcher;
import net.minecraft.client.render.entity.MobEntityRenderer;
import net.minecraft.util.Identifier;

public class CrabRenderer extends MobEntityRenderer<CrabEntity, CrabModel> {

	private static final Identifier[] TEXTURES = new Identifier[] {
			new Identifier("quark", "textures/model/entity/crab/red.png"),
			new Identifier("quark", "textures/model/entity/crab/blue.png"),
			new Identifier("quark", "textures/model/entity/crab/green.png")
	};

	public CrabRenderer(EntityRenderDispatcher render) {
		super(render, new CrabModel(), 0.4F);
	}

	@Nullable
	@Override
	public Identifier getEntityTexture(@Nonnull CrabEntity entity) {
		return TEXTURES[Math.min(TEXTURES.length - 1, entity.getVariant())];
	}
}
