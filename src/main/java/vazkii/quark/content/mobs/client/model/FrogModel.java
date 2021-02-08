package vazkii.quark.content.mobs.client.model;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.vertex.IVertexBuilder;
import net.minecraft.client.renderer.entity.model.EntityModel;
import net.minecraft.client.renderer.model.ModelRenderer;
import net.minecraft.util.math.MathHelper;
import vazkii.quark.content.mobs.entity.FrogEntity;

import javax.annotation.Nonnull;

public class FrogModel extends EntityModel<FrogEntity> {

	private float frogSize;

	public final ModelRenderer headTop;
	public final ModelRenderer headBottom;
	public final ModelRenderer body;
	public final ModelRenderer rightArm;
	public final ModelRenderer leftArm;
	public final ModelRenderer rightEye;
	public final ModelRenderer leftEye;

	public FrogModel() {
		textureWidth = 64;
		textureHeight = 32;
		rightArm = new ModelRenderer(this, 33, 7);
		rightArm.mirror = true;
		rightArm.setRotationPoint(6.5F, 22.0F, 1.0F);
		rightArm.addBox(-1.0F, -1.0F, -5.0F, 3, 3, 6, 0.0F);
		leftArm = new ModelRenderer(this, 33, 7);
		leftArm.setRotationPoint(-6.5F, 22.0F, 1.0F);
		leftArm.addBox(-2.0F, -1.0F, -5.0F, 3, 3, 6, 0.0F);
		body = new ModelRenderer(this, 0, 7);
		body.setRotationPoint(0.0F, 20.0F, 0.0F);
		body.addBox(-5.5F, -3.0F, 0.0F, 11, 7, 11, 0.0F);
		headTop = new ModelRenderer(this, 0, 0);
		headTop.setRotationPoint(0.0F, 18.0F, 0.0F);
		headTop.addBox(-5.5F, -1.0F, -5.0F, 11, 2, 5, 0.0F);
		headBottom = new ModelRenderer(this, 32, 0);
		headBottom.setRotationPoint(0.0F, 18.0F, 0.0F);
		headBottom.addBox(-5.5F, 1.0F, -5.0F, 11, 2, 5, 0.0F);
		rightEye = new ModelRenderer(this, 0, 0);
		rightEye.mirror = true;
		rightEye.setRotationPoint(0.0F, 18.0F, 0.0F);
		rightEye.addBox(1.5F, -1.5F, -4.0F, 1, 1, 1, 0.0F);
		leftEye = new ModelRenderer(this, 0, 0);
		leftEye.setRotationPoint(0.0F, 18.0F, 0.0F);
		leftEye.addBox(-2.5F, -1.5F, -4.0F, 1, 1, 1, 0.0F);
	}


	@Override
	public void setLivingAnimations(FrogEntity frog, float limbSwing, float limbSwingAmount, float partialTickTime) {
		int rawTalkTime = frog.getTalkTime();

		headBottom.rotateAngleX = (float) Math.PI / 120;

		if (rawTalkTime != 0) {
			float talkTime = rawTalkTime - partialTickTime;

			int speed = 10;

			headBottom.rotateAngleX += Math.PI / 8 * (1 - MathHelper.cos(talkTime * (float) Math.PI * 2 / speed));
		}
	}

	@Override
	public void setRotationAngles(FrogEntity frog, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
		frogSize = frog.getSizeModifier();

		rightArm.rotateAngleX = MathHelper.cos(limbSwing * 2 / 3) * 1F * limbSwingAmount;
		leftArm.rotateAngleX = MathHelper.cos(limbSwing * 2 / 3) * 1F * limbSwingAmount;

		headTop.rotateAngleX = headPitch * (float) Math.PI / 180;
		rightEye.rotateAngleX = leftEye.rotateAngleX = headTop.rotateAngleX;
		headBottom.rotateAngleX += headPitch * (float) Math.PI / 180;

		if (frog.isVoid()) {
			headTop.rotateAngleX *= -1;
			rightEye.rotateAngleX *= -1;
			leftEye.rotateAngleX *= -1;
			headBottom.rotateAngleX *= -1;
		}
	}

	@Override
	public void render(MatrixStack matrix, @Nonnull IVertexBuilder vb, int packedLightIn, int packedOverlayIn, float red, float green, float blue, float alpha) {
		matrix.push();
		matrix.translate(0, 1.5 - frogSize * 1.5, 0);
		matrix.scale(frogSize, frogSize, frogSize);

		if (isChild) {
			matrix.push();
			matrix.translate(0, 0.6, 0);
			matrix.scale(0.625F, 0.625F, 0.625F);
		}

		headTop.render(matrix, vb, packedLightIn, packedOverlayIn, red, green, blue, alpha);
		headBottom.render(matrix, vb, packedLightIn, packedOverlayIn, red, green, blue, alpha);
		rightEye.render(matrix, vb, packedLightIn, packedOverlayIn, red, green, blue, alpha);
		leftEye.render(matrix, vb, packedLightIn, packedOverlayIn, red, green, blue, alpha);

		if (isChild) {
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
