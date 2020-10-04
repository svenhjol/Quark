package vazkii.quark.mobs.client.layer;

import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.feature.FeatureRenderer;
import net.minecraft.client.render.entity.feature.FeatureRendererContext;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Identifier;
import vazkii.quark.base.Quark;
import vazkii.quark.mobs.client.model.ToretoiseModel;
import vazkii.quark.mobs.entity.ToretoiseEntity;

public class ToretoiseOreLayer extends FeatureRenderer<ToretoiseEntity, ToretoiseModel> {

	private static final String ORE_BASE = Quark.MOD_ID + ":textures/model/entity/toretoise/ore%d.png";

	public ToretoiseOreLayer(FeatureRendererContext<ToretoiseEntity, ToretoiseModel> renderer) {
		super(renderer);
	}

	@Override
	public void render(MatrixStack matrix, VertexConsumerProvider buffer, int light, ToretoiseEntity entity, float limbAngle, float limbDistance, float tickDelta, float customAngle, float headYaw, float headPitch) {
		int ore = entity.getOreType();
		if(ore != 0 && ore <= ToretoiseEntity.ORE_TYPES) {
			Identifier res = new Identifier(String.format(ORE_BASE, ore));
			renderModel(getContextModel(), res, matrix, buffer, light, entity, 1, 1, 1);
		}
	}

}
