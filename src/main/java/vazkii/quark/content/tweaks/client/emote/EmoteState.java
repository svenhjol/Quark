/**
 * This class was created by <Vazkii>. It's distributed as
 * part of the Quark Mod. Get the Source Code in github:
 * https://github.com/Vazkii/Quark
 *
 * Quark is Open Source and distributed under the
 * CC-BY-NC-SA 3.0 License: https://creativecommons.org/licenses/by-nc-sa/3.0/deed.en_GB
 *
 * File Created @ [26/03/2016, 21:37:30 (GMT)]
 */
package vazkii.quark.content.tweaks.client.emote;

import static vazkii.quark.content.tweaks.client.emote.EmoteBase.PI_F;

import com.mojang.blaze3d.systems.RenderSystem;

import net.minecraft.client.renderer.entity.model.BipedModel;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class EmoteState {

	private float[] states = new float[0];
	private final EmoteBase emote;

	public EmoteState(EmoteBase emote) {
		this.emote = emote;
	}

	public void save(BipedModel<?> model) {
		float[] values = new float[1];
		for(int i = 0; i < ModelAccessor.STATE_COUNT; i++) {
			ModelAccessor.INSTANCE.getValues(model, i, values);
			states[i] = values[0];
		}
	}

	public void load(BipedModel<?> model) {
		if(states.length == 0) {
			states = new float[ModelAccessor.STATE_COUNT];
		} else {
			float[] values = new float[1];
			for(int i = 0; i < ModelAccessor.STATE_COUNT; i++) {
				values[0] = states[i];

				int part = (i / ModelAccessor.MODEL_PROPS) * ModelAccessor.MODEL_PROPS;
				if(emote.usesBodyPart(part))
					ModelAccessor.INSTANCE.setValues(model, i, values);
			}
		}
	}

	public void rotateAndOffset(PlayerEntity player) {
		if(states.length == 0)
			return;

		float rotX = states[ModelAccessor.MODEL_X];
		float rotY = states[ModelAccessor.MODEL_Y];
		float rotZ = states[ModelAccessor.MODEL_Z];

		float height = player.getHeight();

		RenderSystem.translatef(0, height / 2, 0);

		if (rotY != 0)
			RenderSystem.rotatef(rotY * 180 / PI_F, 0, 1, 0);
		if (rotX != 0)
			RenderSystem.rotatef(rotX * 180 / PI_F, 1, 0, 0);
		if (rotZ != 0)
			RenderSystem.rotatef(rotZ * 180 / PI_F, 0, 0, 1);

		RenderSystem.translatef(0, -height / 2, 0);
	}
}

