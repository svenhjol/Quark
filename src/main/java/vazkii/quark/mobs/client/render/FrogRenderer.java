package vazkii.quark.mobs.client.render;

import vazkii.quark.mobs.client.model.FrogModel;
import vazkii.quark.mobs.entity.FrogEntity;

import javax.annotation.Nonnull;
import net.minecraft.client.render.entity.EntityRenderDispatcher;
import net.minecraft.client.render.entity.MobEntityRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.client.util.math.Vector3f;
import net.minecraft.util.Identifier;

public class FrogRenderer extends MobEntityRenderer<FrogEntity, FrogModel> {

	private static final Identifier TEXTURE = new Identifier("quark", "textures/model/entity/frog.png");
	private static final Identifier TEXTURE_SWEATER = new Identifier("quark", "textures/model/entity/events/sweater_frog.png");
	private static final Identifier TEXTURE_FUNNY = new Identifier("quark", "textures/model/entity/events/funny_rat_frog.png");
	private static final Identifier TEXTURE_SWEATER_FUNNY = new Identifier("quark", "textures/model/entity/events/sweater_funny_rat_frog.png");
	private static final Identifier TEXTURE_SNAKE = new Identifier("quark", "textures/model/entity/events/snake_block_frog.png");
	private static final Identifier TEXTURE_SWEATER_SNAKE = new Identifier("quark", "textures/model/entity/events/sweater_snake_block_frog.png");
	private static final Identifier TEXTURE_KERMIT = new Identifier("quark", "textures/model/entity/events/kermit_frog.png");
	private static final Identifier TEXTURE_SWEATER_KERMIT = new Identifier("quark", "textures/model/entity/events/sweater_kermit_frog.png");
	private static final Identifier TEXTURE_VOID = new Identifier("quark", "textures/model/entity/events/void_frog.png");
	private static final Identifier TEXTURE_SWEATER_VOID = new Identifier("quark", "textures/model/entity/events/sweater_void_frog.png");

	public FrogRenderer(EntityRenderDispatcher manager) {
		super(manager, new FrogModel(), 0.2F);
	}

	@Override
	protected void applyRotations(@Nonnull FrogEntity frog, @Nonnull MatrixStack matrix, float ageInTicks, float rotationYaw, float partialTicks) {
		super.setupTransforms(frog, matrix, ageInTicks, rotationYaw, partialTicks);

		if (frog.isVoid()) {
			matrix.translate(0.0D, frog.getHeight(), 0.0D);
			matrix.multiply(Vector3f.POSITIVE_Z.getDegreesQuaternion(180.0F));
		}
	}

	@Nonnull
	@Override
	public Identifier getEntityTexture(@Nonnull FrogEntity entity) {
		if (entity.isVoid())
			return entity.hasSweater() ? TEXTURE_SWEATER_VOID : TEXTURE_VOID;

		if (entity.hasCustomName()) {
			String name = entity.getCustomName().getString().trim();
			if(name.equalsIgnoreCase("Alex") || name.equalsIgnoreCase("Rat") || name.equalsIgnoreCase("Funny Rat"))
				return entity.hasSweater() ? TEXTURE_SWEATER_FUNNY : TEXTURE_FUNNY;
			if(name.equalsIgnoreCase("Snake") || name.equalsIgnoreCase("SnakeBlock") || name.equalsIgnoreCase("Snake Block"))
				return entity.hasSweater() ? TEXTURE_SWEATER_SNAKE : TEXTURE_SNAKE;
			if(name.equalsIgnoreCase("Kermit"))
				return entity.hasSweater() ? TEXTURE_SWEATER_KERMIT : TEXTURE_KERMIT;
		}
		return entity.hasSweater() ? TEXTURE_SWEATER : TEXTURE;
	}

}
