/**
 * This class was created by <Vazkii>. It's distributed as
 * part of the Quark Mod. Get the Source Code in github:
 * https://github.com/Vazkii/Quark
 *
 * Quark is Open Source and distributed under the
 * CC-BY-NC-SA 3.0 License: https://creativecommons.org/licenses/by-nc-sa/3.0/deed.en_GB
 *
 * File Created @ [26/03/2016, 21:37:50 (GMT)]
 */
package vazkii.quark.content.tweaks.client.emote;

import java.util.Map;
import java.util.WeakHashMap;

import net.minecraft.client.renderer.entity.model.BipedModel;
import net.minecraft.client.renderer.entity.model.PlayerModel;
import net.minecraft.client.renderer.model.ModelRenderer;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import vazkii.aurelienribon.tweenengine.TweenAccessor;

@OnlyIn(Dist.CLIENT)
public class ModelAccessor implements TweenAccessor<BipedModel<?>> {

	public static final ModelAccessor INSTANCE = new ModelAccessor();

	private static final int ROT_X = 0;
	private static final int ROT_Y = 1;
	private static final int ROT_Z = 2;
	
	protected static final int MODEL_PROPS = 3;
	protected static final int BODY_PARTS = 7;
	protected static final int STATE_COUNT = MODEL_PROPS * BODY_PARTS;
	
	public static final int HEAD = 0;
	public static final int BODY = MODEL_PROPS;
	public static final int RIGHT_ARM = 2 * MODEL_PROPS;
	public static final int LEFT_ARM = 3 * MODEL_PROPS;
	public static final int RIGHT_LEG = 4 * MODEL_PROPS;
	public static final int LEFT_LEG = 5 * MODEL_PROPS;
	public static final int MODEL = 6 * MODEL_PROPS;

	public static final int HEAD_X = HEAD + ROT_X;
	public static final int HEAD_Y = HEAD + ROT_Y;
	public static final int HEAD_Z = HEAD + ROT_Z;
	public static final int BODY_X = BODY + ROT_X;
	public static final int BODY_Y = BODY + ROT_Y;
	public static final int BODY_Z = BODY + ROT_Z;
	public static final int RIGHT_ARM_X = RIGHT_ARM + ROT_X;
	public static final int RIGHT_ARM_Y = RIGHT_ARM + ROT_Y;
	public static final int RIGHT_ARM_Z = RIGHT_ARM + ROT_Z;
	public static final int LEFT_ARM_X = LEFT_ARM + ROT_X;
	public static final int LEFT_ARM_Y = LEFT_ARM + ROT_Y;
	public static final int LEFT_ARM_Z = LEFT_ARM + ROT_Z;
	public static final int RIGHT_LEG_X = RIGHT_LEG + ROT_X;
	public static final int RIGHT_LEG_Y = RIGHT_LEG + ROT_Y;
	public static final int RIGHT_LEG_Z = RIGHT_LEG + ROT_Z;
	public static final int LEFT_LEG_X = LEFT_LEG + ROT_X;
	public static final int LEFT_LEG_Y = LEFT_LEG + ROT_Y;
	public static final int LEFT_LEG_Z = LEFT_LEG + ROT_Z;

	public static final int MODEL_X = MODEL + ROT_X;
	public static final int MODEL_Y = MODEL + ROT_Y;
	public static final int MODEL_Z = MODEL + ROT_Z;

	private final Map<BipedModel<?>, float[]> MODEL_VALUES = new WeakHashMap<>();

	public void resetModel(BipedModel<?> model) {
		MODEL_VALUES.remove(model);
	}

	@Override
	public int getValues(BipedModel<?> target, int tweenType, float[] returnValues) {
		int axis = tweenType % MODEL_PROPS;
		int bodyPart = tweenType - axis;

		if (bodyPart == MODEL) {
			if (!MODEL_VALUES.containsKey(target)) {
				returnValues[0] = 0;
				return 1;
			}

			float[] values = MODEL_VALUES.get(target);
			returnValues[0] = values[axis];
			return 1;
		}

		ModelRenderer model = getBodyPart(target, bodyPart);
		if(model == null)
			return 0;

		switch(axis) {
			case ROT_X:
				returnValues[0] = model.rotateAngleX; break;
			case ROT_Y:
				returnValues[0] = model.rotateAngleY; break;
			case ROT_Z:
				returnValues[0] = model.rotateAngleZ; break;
		}

		return 1;
	}

	private ModelRenderer getBodyPart(BipedModel<?> model, int part) {
		switch(part) {
			case HEAD : return model.bipedHead;
			case BODY : return model.bipedBody;
			case RIGHT_ARM : return model.bipedRightArm;
			case LEFT_ARM : return model.bipedLeftArm;
			case RIGHT_LEG : return model.bipedRightLeg;
			case LEFT_LEG : return model.bipedLeftLeg;
		}
		return null;
	}

	@Override
	public void setValues(BipedModel<?> target, int tweenType, float[] newValues) {
		int axis = tweenType % MODEL_PROPS;
		int bodyPart = tweenType - axis;

		if (bodyPart == MODEL) {
			float[] values = MODEL_VALUES.get(target);
			if (values == null)
				MODEL_VALUES.put(target, values = new float[MODEL_PROPS]);

			values[axis] = newValues[0];

			return;
		}

		ModelRenderer model = getBodyPart(target, bodyPart);
		messWithModel(target, model, axis, newValues[0]);
	}

	private void messWithModel(BipedModel<?> biped, ModelRenderer part, int axis, float val) {
		setPartAxis(part, axis, val);
		if(biped instanceof PlayerModel)
			messWithPlayerModel((PlayerModel<?>) biped, part, axis, val);
	}

	private void messWithPlayerModel(PlayerModel<?> biped, ModelRenderer part, int axis, float val) {
		if(part == biped.bipedHead) {
			setPartAxis(biped.bipedHeadwear, axis, val);
		} else if(part == biped.bipedLeftArm)
			setPartAxis(biped.bipedLeftArmwear, axis, val);
		else if(part == biped.bipedRightArm)
			setPartAxis(biped.bipedRightArmwear, axis, val);
		else if(part == biped.bipedLeftLeg)
			setPartAxis(biped.bipedLeftLegwear, axis, val);
		else if(part == biped.bipedRightLeg)
			setPartAxis(biped.bipedRightLegwear, axis, val);
		else if(part == biped.bipedBody)
			setPartAxis(biped.bipedBodyWear, axis, val);
	}

	private void setPartAxis(ModelRenderer part, int axis, float val) {
		if(part == null)
			return;
		
		switch(axis) {
			case ROT_X:
				part.rotateAngleX = val; break;
			case ROT_Y:
				part.rotateAngleY = val; break;
			case ROT_Z:
				part.rotateAngleZ = val; break;
		}
	}

}

