package vazkii.quark.content.mobs.client.layer;

import com.mojang.blaze3d.matrix.MatrixStack;

import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.entity.IEntityRenderer;
import net.minecraft.client.renderer.entity.layers.LayerRenderer;
import net.minecraft.util.ResourceLocation;
import vazkii.quark.base.Quark;
import vazkii.quark.content.mobs.client.model.ToretoiseModel;
import vazkii.quark.content.mobs.entity.ToretoiseEntity;

public class ToretoiseOreLayer extends LayerRenderer<ToretoiseEntity, ToretoiseModel> {

	private static final String ORE_BASE = Quark.MOD_ID + ":textures/model/entity/toretoise/ore%d.png";

	public ToretoiseOreLayer(IEntityRenderer<ToretoiseEntity, ToretoiseModel> renderer) {
		super(renderer);
	}

	@Override
	public void render(MatrixStack matrix, IRenderTypeBuffer buffer, int light, ToretoiseEntity entity, float limbAngle, float limbDistance, float tickDelta, float customAngle, float headYaw, float headPitch) {
		int ore = entity.getOreType();
		if(ore != 0 && ore <= ToretoiseEntity.ORE_TYPES) {
			ResourceLocation res = new ResourceLocation(String.format(ORE_BASE, ore));
			renderCutoutModel(getEntityModel(), res, matrix, buffer, light, entity, 1, 1, 1);
		}
	}

}
