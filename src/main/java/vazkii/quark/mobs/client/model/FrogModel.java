package vazkii.quark.mobs.client.model;

import net.minecraft.client.model.ModelPart;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.entity.model.EntityModel;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.MathHelper;
import vazkii.quark.mobs.entity.FrogEntity;

import javax.annotation.Nonnull;

public class FrogModel extends EntityModel<FrogEntity> {

	private float frogSize;

	public final ModelPart headTop;
	public final ModelPart headBottom;
	public final ModelPart body;
	public final ModelPart rightArm;
	public final ModelPart leftArm;
	public final ModelPart rightEye;
	public final ModelPart leftEye;

	public FrogModel() {
		textureWidth = 64;
		textureHeight = 32;
		rightArm = new ModelPart(this, 33, 7);
		rightArm.mirror = true;
		rightArm.setPivot(6.5F, 22.0F, 1.0F);
		rightArm.addCuboid(-1.0F, -1.0F, -5.0F, 3, 3, 6, 0.0F);
		leftArm = new ModelPart(this, 33, 7);
		leftArm.setPivot(-6.5F, 22.0F, 1.0F);
		leftArm.addCuboid(-2.0F, -1.0F, -5.0F, 3, 3, 6, 0.0F);
		body = new ModelPart(this, 0, 7);
		body.setPivot(0.0F, 20.0F, 0.0F);
		body.addCuboid(-5.5F, -3.0F, 0.0F, 11, 7, 11, 0.0F);
		headTop = new ModelPart(this, 0, 0);
		headTop.setPivot(0.0F, 18.0F, 0.0F);
		headTop.addCuboid(-5.5F, -1.0F, -5.0F, 11, 2, 5, 0.0F);
		headBottom = new ModelPart(this, 32, 0);
		headBottom.setPivot(0.0F, 18.0F, 0.0F);
		headBottom.addCuboid(-5.5F, 1.0F, -5.0F, 11, 2, 5, 0.0F);
		rightEye = new ModelPart(this, 0, 0);
		rightEye.mirror = true;
		rightEye.setPivot(0.0F, 18.0F, 0.0F);
		rightEye.addCuboid(1.5F, -1.5F, -4.0F, 1, 1, 1, 0.0F);
		leftEye = new ModelPart(this, 0, 0);
		leftEye.setPivot(0.0F, 18.0F, 0.0F);
		leftEye.addCuboid(-2.5F, -1.5F, -4.0F, 1, 1, 1, 0.0F);
	}


	@Override
	public void setLivingAnimations(FrogEntity frog, float limbSwing, float limbSwingAmount, float partialTickTime) {
		int rawTalkTime = frog.getTalkTime();

		headBottom.pitch = (float) Math.PI / 120;

		if (rawTalkTime != 0) {
			float talkTime = rawTalkTime - partialTickTime;

			int speed = 10;

			headBottom.pitch += Math.PI / 8 * (1 - MathHelper.cos(talkTime * (float) Math.PI * 2 / speed));
		}
	}

	@Override
	public void setRotationAngles(FrogEntity frog, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
		frogSize = frog.getSizeModifier();

		rightArm.pitch = MathHelper.cos(limbSwing * 2 / 3) * 1F * limbSwingAmount;
		leftArm.pitch = MathHelper.cos(limbSwing * 2 / 3) * 1F * limbSwingAmount;

		headTop.pitch = headPitch * (float) Math.PI / 180;
		rightEye.pitch = leftEye.pitch = headTop.pitch;
		headBottom.pitch += headPitch * (float) Math.PI / 180;

		if (frog.isVoid()) {
			headTop.pitch *= -1;
			rightEye.pitch *= -1;
			leftEye.pitch *= -1;
			headBottom.pitch *= -1;
		}
	}

	@Override
	public void render(MatrixStack matrix, @Nonnull VertexConsumer vb, int packedLightIn, int packedOverlayIn, float red, float green, float blue, float alpha) {
		matrix.push();
		matrix.translate(0, 1.5 - frogSize * 1.5, 0);
		matrix.scale(frogSize, frogSize, frogSize);

		if (child) {
			matrix.push();
			matrix.translate(0, 0.6, 0);
			matrix.scale(0.625F, 0.625F, 0.625F);
		}

		headTop.render(matrix, vb, packedLightIn, packedOverlayIn, red, green, blue, alpha);
		headBottom.render(matrix, vb, packedLightIn, packedOverlayIn, red, green, blue, alpha);
		rightEye.render(matrix, vb, packedLightIn, packedOverlayIn, red, green, blue, alpha);
		leftEye.render(matrix, vb, packedLightIn, packedOverlayIn, red, green, blue, alpha);

		if (child) {
			matrix.pop();
			matrix.scale(0.5F, 0.5F, 0.5F);
			matrix.translate(0, 1.5, 0);
		}

		rightArm.render(matrix, vb, packedLightIn, packedOverlayIn, red, green, blue, alpha);
		leftArm.render(matrix, vb, packedLightIn, packedOverlayIn, red, green, blue, alpha);
		body.render(matrix, vb, packedLightIn, packedOverlayIn, red, green, blue, alpha);

		matrix.pop();
	}

}
