/**
 * This class was created by <WireSegal>. It's distributed as
 * part of the Quark Mod. Get the Source Code in github:
 * https://github.com/Vazkii/Quark
 * <p>
 * Quark is Open Source and distributed under the
 * CC-BY-NC-SA 3.0 License: https://creativecommons.org/licenses/by-nc-sa/3.0/deed.en_GB
 * <p>
 * File Created @ [May 11, 2019, 16:46 AM (EST)]
 */
package vazkii.quark.content.mobs.client.layer;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.OverlayTexture;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.feature.FeatureRenderer;
import net.minecraft.client.render.entity.feature.FeatureRendererContext;
import net.minecraft.client.render.model.json.ModelTransformation.Mode;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.client.util.math.Vector3f;
import net.minecraft.item.BlockItem;
import net.minecraft.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import vazkii.quark.content.mobs.client.model.StonelingModel;
import vazkii.quark.content.mobs.entity.StonelingEntity;

@Environment(EnvType.CLIENT)
public class StonelingItemLayer extends FeatureRenderer<StonelingEntity, StonelingModel> {

	public StonelingItemLayer(FeatureRendererContext<StonelingEntity, StonelingModel> renderer) {
		super(renderer);
	}
	
	public void render(MatrixStack matrix, VertexConsumerProvider buffer, int light, StonelingEntity stoneling,  float limbAngle, float limbDistance, float tickDelta, float customAngle, float headYaw, float headPitch) {
		ItemStack stack = stoneling.getCarryingItem();
		if (!stack.isEmpty()) {
			boolean isBlock = stack.getItem() instanceof BlockItem;
			
			matrix.push();
			matrix.translate(0F, 0.5F, 0F);
			if(!isBlock) {
				matrix.multiply(Vector3f.POSITIVE_Y.getDegreesQuaternion(stoneling.getItemAngle() + 180));
				matrix.multiply(Vector3f.POSITIVE_X.getDegreesQuaternion(90F));
			} else matrix.multiply(Vector3f.POSITIVE_X.getDegreesQuaternion(180F));
			
			matrix.scale(0.725F, 0.725F, 0.725F);
			MinecraftClient mc = MinecraftClient.getInstance();
			mc.getItemRenderer().renderItem(stack, Mode.FIXED, light, OverlayTexture.DEFAULT_UV, matrix, buffer);
			matrix.pop();
		}
	}

}
