package vazkii.quark.mobs.client.model;

import java.util.function.BiConsumer;
import net.minecraft.client.model.ModelPart;
import net.minecraft.client.model.ModelPart.Cuboid;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.entity.model.EntityModel;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.client.util.math.Vector3f;
import vazkii.arl.util.ClientTicker;
import vazkii.quark.mobs.entity.ToretoiseEntity;

public class ToretoiseModel extends EntityModel<ToretoiseEntity> {
	
	private ToretoiseEntity entity;
    private float animFrames;
	
    public ModelPart body;
    public ModelPart head;
    public ModelPart rightFrontLeg;
    public ModelPart leftFrontLeg;
    public ModelPart rightBackLeg;
    public ModelPart leftBackLeg;
    public ModelPart mouth;
    
    public ModelPart CoalOre1;
    public ModelPart CoalOre2;
    public ModelPart CoalOre3;
    public ModelPart CoalOre4;
    public ModelPart IronOre1;
    public ModelPart IronOre2;
    public ModelPart IronOre3;
    public ModelPart LapisOre1;
    public ModelPart LapisOre2;
    public ModelPart LapisOre3;
    public ModelPart LapisOre4;
    public ModelPart RedstoneOre1;
    public ModelPart RedstoneOre2;
    public ModelPart RedstoneOre3;
    public ModelPart RedstoneOre4;
    public ModelPart RedstoneOre5;

    public ToretoiseModel() {
        textureWidth = 100;
        textureHeight = 100;
        mouth = new ModelPart(this, 66, 38);
        mouth.setPivot(0.0F, 1.0F, -1.0F);
        mouth.addCuboid(-4.5F, -2.5F, -8.0F, 9, 4, 8, 0.0F);
        
        leftFrontLeg = new ModelPart(this, 34, 38);
        leftFrontLeg.mirror = true;
        leftFrontLeg.setPivot(10.0F, 16.0F, -12.0F);
        leftFrontLeg.addCuboid(-4.0F, -2.0F, -4.0F, 8, 10, 8, 0.0F);
        setRotateAngle(leftFrontLeg, 0.0F, -0.7853981633974483F, 0.0F);
        
        rightBackLeg = new ModelPart(this, 34, 38);
        rightBackLeg.setPivot(-10.0F, 16.0F, 12.0F);
        rightBackLeg.addCuboid(-4.0F, -2.0F, -4.0F, 8, 10, 8, 0.0F);
        setRotateAngle(rightBackLeg, 0.0F, 0.7853981633974483F, 0.0F);
        
        body = new ModelPart(this, 0, 0);
        body.setPivot(0.0F, 8.0F, 0.0F);
        body.addCuboid(-11.0F, 0.0F, -13.0F, 22, 12, 26, 0.0F);
        
        head = new ModelPart(this, 0, 38);
        head.setPivot(0.0F, 16.0F, -13.0F);
        head.addCuboid(-4.0F, -4.0F, -8.0F, 8, 5, 8, 0.0F);
        
        rightFrontLeg = new ModelPart(this, 34, 38);
        rightFrontLeg.setPivot(-10.0F, 16.0F, -12.0F);
        rightFrontLeg.addCuboid(-4.0F, -2.0F, -4.0F, 8, 10, 8, 0.0F);
        setRotateAngle(rightFrontLeg, 0.0F, 0.7853981633974483F, 0.0F);
        
        leftBackLeg = new ModelPart(this, 34, 38);
        leftBackLeg.mirror = true;
        leftBackLeg.setPivot(10.0F, 16.0F, 12.0F);
        leftBackLeg.addCuboid(-4.0F, -2.0F, -4.0F, 8, 10, 8, 0.0F);
        
        setRotateAngle(leftBackLeg, 0.0F, -0.7853981633974483F, 0.0F);
        head.addChild(mouth);
        
        CoalOre1 = new ModelPart(this, 36, 56);
        CoalOre1.addCuboid(0.0F, -7.0F, -6.0F, 3, 3, 3, 0.0F);
        CoalOre1.setPivot(0.0F, 0.0F, 0.0F);
        CoalOre2 = new ModelPart(this, 42, 56);
        CoalOre2.addCuboid(7.0F, -2.0F, -10.0F, 6, 6, 6, 0.0F);
        CoalOre2.setPivot(0.0F, 0.0F, 0.0F);
        CoalOre3 = new ModelPart(this, 66, 50);
        CoalOre3.addCuboid(-2.0F, -7.0F, -4.0F, 7, 7, 7, 0.0F);
        CoalOre3.setPivot(0.0F, 0.0F, 0.0F);
        CoalOre4 = new ModelPart(this, 60, 64);
        CoalOre4.addCuboid(-15.0F, 0.0F, 1.0F, 4, 6, 6, 0.0F);
        CoalOre4.setPivot(0.0F, 0.0F, 0.0F);
        
        IronOre1 = new ModelPart(this, 36, 89);
        IronOre1.addCuboid(1.0F, -3.0F, 1.0F, 8, 3, 8, 0.0F);
        IronOre1.setPivot(0.0F, 0.0F, 0.0F);
        IronOre2 = new ModelPart(this, 32, 81);
        IronOre2.addCuboid(-7.0F, -2.0F, -11.0F, 6, 2, 6, 0.0F);
        IronOre2.setPivot(0.0F, 0.0F, 0.0F);
        IronOre3 = new ModelPart(this, 30, 76);
        IronOre3.addCuboid(-9.0F, -1.0F, 6.0F, 4, 1, 4, 0.0F);
        IronOre3.setPivot(0.0F, 0.0F, 0.0F);
        
        LapisOre1 = new ModelPart(this, 0, 51);
        LapisOre1.addCuboid(-5.0F, -8.0F, 0.0F, 8, 8, 0, 0.0F);
        LapisOre1.setPivot(0.0F, 0.0F, 0.0F);
        LapisOre2 = new ModelPart(this, 0, 53);
        LapisOre2.addCuboid(-1.0F, -8.0F, -4.0F, 0, 8, 8, 0.0F);
        LapisOre2.setPivot(0.0F, 0.0F, 0.0F);
        LapisOre3 = new ModelPart(this, 18, 51);
        LapisOre3.addCuboid(-10.0F, -8.0F, 8.0F, 8, 8, 0, 0.0F);
        LapisOre3.setPivot(0.0F, 0.0F, 0.0F);
        LapisOre4 = new ModelPart(this, 18, 53);
        LapisOre4.addCuboid(-6.0F, -8.0F, 4.0F, 0, 8, 8, 0.0F);
        LapisOre4.setPivot(0.0F, 0.0F, 0.0F);
        
        RedstoneOre1 = new ModelPart(this, 0, 83);
        RedstoneOre1.addCuboid(-8.0F, -12.0F, -6.0F, 5, 12, 5, 0.0F);
        RedstoneOre1.setPivot(0.0F, 0.0F, 0.0F);
        RedstoneOre2 = new ModelPart(this, 0, 74);
        RedstoneOre2.addCuboid(6.0F, -6.0F, -1.0F, 3, 6, 3, 0.0F);
        RedstoneOre2.setPivot(0.0F, 0.0F, 0.0F);
        RedstoneOre3 = new ModelPart(this, 12, 76);
        RedstoneOre3.addCuboid(-7.0F, -4.0F, 2.0F, 2, 4, 2, 0.0F);
        RedstoneOre3.setPivot(0.0F, 0.0F, 0.0F);
        RedstoneOre4 = new ModelPart(this, 20, 87);
        RedstoneOre4.addCuboid(1.0F, -9.0F, -9.0F, 4, 9, 4, 0.0F);
        RedstoneOre4.setPivot(0.0F, 0.0F, 0.0F);
        RedstoneOre5 = new ModelPart(this, 15, 77);
        RedstoneOre5.addCuboid(-1.0F, -5.0F, 5.0F, 5, 5, 5, 0.0F);
        RedstoneOre5.setPivot(0.0F, 0.0F, 0.0F);
        
        body.addChild(CoalOre2);
        body.addChild(CoalOre3);
        body.addChild(CoalOre4);
        body.addChild(IronOre1);
        body.addChild(IronOre2);
        body.addChild(IronOre3);
        body.addChild(LapisOre1);
        body.addChild(LapisOre2);
        body.addChild(LapisOre3);
        body.addChild(LapisOre4);
        body.addChild(RedstoneOre1);
        body.addChild(RedstoneOre2);
        body.addChild(RedstoneOre3);
        body.addChild(RedstoneOre4);
        body.addChild(RedstoneOre5);
        head.addChild(CoalOre1);
    }
    
	@Override
	public void setRotationAngles(ToretoiseEntity entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
		this.entity = entity;
		animFrames = limbSwing;
	}

    @Override
	public void render(MatrixStack matrix, VertexConsumer vb, int p_225598_3_, int p_225598_4_, float p_225598_5_, float p_225598_6_, float p_225598_7_, float p_225598_8_) {
        matrix.push();
        int bufferTime = 10;
    	if(entity.angeryTicks > 0 && entity.angeryTicks < ToretoiseEntity.ANGERY_TIME - bufferTime) {
    		double angeryTime = (entity.angeryTicks - ClientTicker.partialTicks) / (ToretoiseEntity.ANGERY_TIME - bufferTime) * Math.PI;
    		angeryTime = Math.sin(angeryTime) * -20;
    		
    		matrix.translate(0, 1., 1);
    		matrix.multiply(Vector3f.POSITIVE_X.getDegreesQuaternion((float) angeryTime));
    		matrix.translate(0, -1, -1);
    	}
    	
        float animSpeed = 30;
        float animPause = 12;
        
        float actualFrames = animFrames * 10;
        
        float doubleAnimSpeed = animSpeed * 2;
        float animBuff = animSpeed - animPause;
    	
        float scale = 0.02F;
        float bodyTrans = (float) (Math.sin(actualFrames / doubleAnimSpeed * Math.PI) + 1F) * scale;

        float rideMultiplier = 0;
        
        if(entity.rideTime > 0)
        	rideMultiplier = (float) Math.min(30, entity.rideTime - 1 + ClientTicker.partialTicks) / 30.0F;  
        
        bodyTrans *= (1F - rideMultiplier); 
        
        matrix.translate(0, bodyTrans, 0);
        matrix.multiply(Vector3f.POSITIVE_Z.getRadialQuaternion((bodyTrans - scale) * 0.5F));
        
        body.render(matrix, vb, p_225598_3_, p_225598_4_, p_225598_5_, p_225598_6_, p_225598_7_, p_225598_8_);
        
        matrix.push();
        matrix.translate(0, bodyTrans, rideMultiplier * 0.3);
        head.pitch = bodyTrans * 2;
        head.render(matrix, vb, p_225598_3_, p_225598_4_, p_225598_5_, p_225598_6_, p_225598_7_, p_225598_8_);
        matrix.pop();

        float finalRideMultiplier = rideMultiplier;
        BiConsumer<ModelPart, Float> draw = (renderer, frames) -> {
        	float time = Math.min(animBuff, frames % doubleAnimSpeed);
            float trans = ((float) (Math.sin(time / animBuff * Math.PI) + 1.0) / -2F) * 0.12F + 0.06F;
            
            float rotTime = (frames % doubleAnimSpeed);
            float rot = ((float) Math.sin(rotTime / doubleAnimSpeed * Math.PI) + 1F) * -0.25F;
        	
            trans *= (1F - finalRideMultiplier);
            rot *= (1F - finalRideMultiplier);
            trans += finalRideMultiplier * -0.2;

            matrix.push();
            
            Cuboid box = renderer.getRandomCuboid(entity.getRandom());
            double spread = (1F / 16F) * -1.8 * finalRideMultiplier;
            double x = (renderer.pivotX + box.minX);
            double z = (renderer.pivotZ + box.minZ);
            x *= (spread / Math.abs(x));
            z *= (spread / Math.abs(z));
            matrix.translate(x, 0, z);
            
            matrix.translate(0, trans, 0);
            float yRot = renderer.yaw;
            renderer.pitch = rot;
            renderer.yaw *= (1F - finalRideMultiplier);
            renderer.render(matrix, vb, p_225598_3_, p_225598_4_, p_225598_5_, p_225598_6_, p_225598_7_, p_225598_8_);
            renderer.yaw = yRot;
            matrix.pop();
        };
        
        draw.accept(leftFrontLeg, actualFrames);
        draw.accept(rightFrontLeg, actualFrames + animSpeed);
        draw.accept(leftBackLeg, actualFrames + animSpeed * 0.5F);
        draw.accept(rightBackLeg, actualFrames + animSpeed * 1.5F);
        matrix.pop();
    }
    
    public void setRotateAngle(ModelPart modelRenderer, float x, float y, float z) {
        modelRenderer.pitch = x;
        modelRenderer.yaw = y;
        modelRenderer.roll = z;
    }
	
}
