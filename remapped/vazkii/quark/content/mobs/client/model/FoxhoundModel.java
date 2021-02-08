package vazkii.quark.content.mobs.client.model;

import net.minecraft.client.model.ModelPart;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.entity.model.EntityModel;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.MathHelper;
import vazkii.quark.content.mobs.entity.FoxhoundEntity;

/**
 * ModelFoxhound - McVinnyq
 * Created using Tabula 7.0.0
 */
public class FoxhoundModel extends EntityModel<FoxhoundEntity> {

	public final ModelPart head;
	public final ModelPart rightFrontLeg;
	public final ModelPart leftFrontLeg;
	public final ModelPart rightBackLeg;
	public final ModelPart leftBackLeg;
	public final ModelPart body;
	public final ModelPart snout;
	public final ModelPart rightEar;
	public final ModelPart leftEar;
	public final ModelPart tail;
	public final ModelPart fluff;
	
	private FoxhoundEntity entity;

	public FoxhoundModel() {
		this.textureWidth = 64;
		this.textureHeight = 64;
		this.leftBackLeg = new ModelPart(this, 36, 32);
		this.leftBackLeg.setPivot(3.0F, 12.0F, 9.5F);
		this.leftBackLeg.addCuboid(-1.5F, 0.0F, -1.5F, 3, 12, 3, 0.0F);
		this.rightFrontLeg = new ModelPart(this, 0, 32);
		this.rightFrontLeg.setPivot(-2.0F, 12.0F, 2.0F);
		this.rightFrontLeg.addCuboid(-1.5F, 0.0F, -1.5F, 3, 12, 3, 0.0F);
		this.rightEar = new ModelPart(this, 0, 47);
		this.rightEar.setPivot(0.0F, 0.0F, 0.0F);
		this.rightEar.addCuboid(-4.0F, -5.0F, -5.0F, 2, 2, 3, 0.0F);
		this.tail = new ModelPart(this, 36, 16);
		this.tail.setPivot(0.0F, 0.0F, 1.5F);
		this.tail.addCuboid(-2.0F, -4.0F, 0.0F, 4, 5, 10, 0.0F);
		this.setRotateAngle(tail, -1.3089969389957472F, 0.0F, 0.0F);
		this.body = new ModelPart(this, 0, 2);
		this.body.setPivot(0.0F, 17.0F, 12.0F);
		this.body.addCuboid(-4.0F, -12.0F, 0.0F, 8, 12, 6, 0.0F);
		this.setRotateAngle(body, 1.5707963267948966F, 0.0F, 0.0F);
		this.fluff = new ModelPart(this, 28, 0);
		this.fluff.setPivot(0.0F, -13.0F, 3.0F);
		this.fluff.addCuboid(-5.0F, 0.0F, -4.0F, 10, 8, 8, 0.05F);
		this.leftFrontLeg = new ModelPart(this, 12, 32);
		this.leftFrontLeg.setPivot(2.0F, 12.0F, 2.0F);
		this.leftFrontLeg.addCuboid(-1.5F, 0.0F, -1.5F, 3, 12, 3, 0.0F);
		this.rightBackLeg = new ModelPart(this, 24, 32);
		this.rightBackLeg.setPivot(-3.0F, 12.0F, 9.5F);
		this.rightBackLeg.addCuboid(-1.5F, 0.0F, -1.5F, 3, 12, 3, 0.0F);
		this.leftEar = new ModelPart(this, 10, 47);
		this.leftEar.setPivot(0.0F, 0.0F, 0.0F);
		this.leftEar.addCuboid(2.0F, -5.0F, -5.0F, 2, 2, 3, 0.0F);
		this.head = new ModelPart(this, 0, 20);
		this.head.setPivot(0.0F, 14.5F, 0.0F);
		this.head.addCuboid(-4.0F, -3.0F, -6.0F, 8, 6, 6, 0.0F);
		this.snout = new ModelPart(this, 29, 18);
		this.snout.setPivot(0.0F, 0.0F, 0.0F);
		this.snout.addCuboid(-2.0F, 1.0F, -10.0F, 4, 2, 4, 0.0F);
		this.head.addChild(this.rightEar);
		this.body.addChild(this.tail);
		this.body.addChild(this.fluff);
		this.head.addChild(this.leftEar);
		this.head.addChild(this.snout);
	}

	@Override
	public void setLivingAnimations(FoxhoundEntity hound, float limbSwing, float limbSwingAmount, float partialTickTime) {
		this.entity = hound;
		if (hound.isSitting() || hound.getAngerTime() > 0)
			this.tail.pitch = -0.6544984695F;
		else
			this.tail.pitch = -1.3089969389957472F + MathHelper.cos(limbSwing * 0.6662F) * limbSwingAmount;

		this.head.yaw = hound.getBegAnimationProgress(partialTickTime) - hound.getShakeAnimationProgress(partialTickTime, 0.0F);
		this.head.pitch = 0;
		this.body.yaw = hound.getShakeAnimationProgress(partialTickTime, -0.16F);
		this.tail.yaw = hound.getShakeAnimationProgress(partialTickTime, -0.2F);

		if (hound.isSleeping()) {
			this.head.setPivot(1.0F, 20.5F, 0.0F);
			this.setRotateAngle(head, 0.0F, 0.7853981633974483F, -0.04363323129985824F);

			this.body.setPivot(0.0F, 20.0F, 12.0F);
			this.setRotateAngle(body, 1.5707963267948966F, 0.0F, 1.5707963267948966F);
			this.tail.setPivot(0.0F, -1.0F, 1.0F);
			this.setRotateAngle(tail, 2.5497515042385164F, -0.22759093446006054F, 0.0F);
			this.rightFrontLeg.setPivot(0.0F, 18.0F, 2.0F);
			this.leftFrontLeg.setPivot(2.0F, 21.0F, 1.0F);
			this.rightBackLeg.setPivot(0.0F, 22.0F, 11.0F);
			this.leftBackLeg.setPivot(3.0F, 20.0F, 10.0F);

			this.setRotateAngle(rightFrontLeg, 0.2181661564992912F, 0.4363323129985824F, 1.3089969389957472F);
			this.setRotateAngle(leftFrontLeg, 0.0F, 0.0F, 1.3962634015954636F);
			this.setRotateAngle(rightBackLeg, -1.0471975511965976F, -0.08726646259971647F, 1.48352986419518F);
			this.setRotateAngle(leftBackLeg, -0.7853981633974483F, 0.0F, 1.2217304763960306F);
		} else if (hound.isSitting()) {
			this.head.setPivot(0.0F, 12.0F, 2.0F);
			this.body.setPivot(0.0F, 23.0F, 7.0F);
			this.setRotateAngle(body, 0.7853981633974483F, this.body.yaw, 0F);
			this.tail.setPivot(0.0F, 0.0F, -2.0F);
			this.setRotateAngle(tail, -0.5235987755982988F, -0.7243116395776468F, 0F);
			this.rightFrontLeg.setPivot(-2.0F, 12.0F, 1.25F);
			this.leftFrontLeg.setPivot(2.0F, 12.0F, 1.25F);
			this.rightBackLeg.setPivot(-3.0F, 21.0F, 10.0F);
			this.leftBackLeg.setPivot(3.0F, 21.0F, 10.0F);

			this.setRotateAngle(rightFrontLeg, 0F, 0F, 0F);
			this.setRotateAngle(leftFrontLeg, 0F, 0F, 0F);
			this.setRotateAngle(rightBackLeg, -1.3089969389957472F, 0.39269908169872414F, 0.0F);
			this.setRotateAngle(leftBackLeg, -1.3089969389957472F, -0.39269908169872414F, 0.0F);
		} else {
			this.head.setPivot(0.0F, 14.5F, 0.0F);
			this.body.setPivot(0.0F, 17.0F, 12.0F);
			this.setRotateAngle(body, 1.5707963267948966F, this.body.yaw, 0F);
			this.tail.setPivot(0.0F, 0.0F, 1.5F);
			this.rightFrontLeg.setPivot(-2.0F, 12.0F, 2.0F);
			this.leftFrontLeg.setPivot(2.0F, 12.0F, 2.0F);
			this.rightBackLeg.setPivot(-3.0F, 12.0F, 9.5F);
			this.leftBackLeg.setPivot(3.0F, 12.0F, 9.5F);
			this.setRotateAngle(rightFrontLeg, MathHelper.cos(limbSwing * 0.6662F) * 1.4F * limbSwingAmount, 0, 0);
			this.setRotateAngle(leftFrontLeg, MathHelper.cos(limbSwing * 0.6662F + (float) Math.PI) * 1.4F * limbSwingAmount, 0, 0);
			this.setRotateAngle(rightBackLeg, MathHelper.cos(limbSwing * 0.6662F + (float) Math.PI) * 1.4F * limbSwingAmount, 0, 0);
			this.setRotateAngle(leftBackLeg, MathHelper.cos(limbSwing * 0.6662F) * 1.4F * limbSwingAmount, 0, 0);
		}
	}

	@Override
	public void setRotationAngles(FoxhoundEntity entity, float p_225597_2_, float p_225597_3_, float p_225597_4_, float yaw, float pitch) {
		if (!entity.isSleeping()) {
			head.yaw += yaw * 0.017453292F;
			head.pitch += pitch * 0.017453292F;
		} else
			head.yaw += MathHelper.cos(entity.age / 30) / 20;    	
	}

	@Override
	public void render(MatrixStack matrix, VertexConsumer vb, int p_225598_3_, int p_225598_4_, float p_225598_5_, float p_225598_6_, float p_225598_7_, float p_225598_8_) {
		matrix.push();
		matrix.translate(0, 0, entity.isSitting() ? -0.25F : -0.35F);

		matrix.push();
		
		if (child)
			matrix.translate(0.0F, 5.0F / 16F, 0F);

		head.render(matrix, vb, p_225598_3_, p_225598_4_, p_225598_5_, p_225598_6_, p_225598_7_, p_225598_8_);
		
		matrix.pop();
		
		matrix.push();
		if (child) {
			matrix.translate(0.0F, 12.0F / 16F, 0F);
			matrix.scale(0.5F, 0.5F, 0.5F);
		}
		
		leftBackLeg.render(matrix, vb, p_225598_3_, p_225598_4_, p_225598_5_, p_225598_6_, p_225598_7_, p_225598_8_);
		rightFrontLeg.render(matrix, vb, p_225598_3_, p_225598_4_, p_225598_5_, p_225598_6_, p_225598_7_, p_225598_8_);
		body.render(matrix, vb, p_225598_3_, p_225598_4_, p_225598_5_, p_225598_6_, p_225598_7_, p_225598_8_);
		leftFrontLeg.render(matrix, vb, p_225598_3_, p_225598_4_, p_225598_5_, p_225598_6_, p_225598_7_, p_225598_8_);
		rightBackLeg.render(matrix, vb, p_225598_3_, p_225598_4_, p_225598_5_, p_225598_6_, p_225598_7_, p_225598_8_);
		matrix.pop();
		matrix.pop();
	}

	public void setRotateAngle(ModelPart modelRenderer, float x, float y, float z) {
		modelRenderer.pitch = x;
		modelRenderer.yaw = y;
		modelRenderer.roll = z;
	}
}
