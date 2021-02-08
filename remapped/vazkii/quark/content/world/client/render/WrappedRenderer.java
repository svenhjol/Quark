package vazkii.quark.content.world.client.render;

import net.minecraft.client.render.entity.EntityRenderDispatcher;
import net.minecraft.client.render.entity.ZombieEntityRenderer;
import net.minecraft.entity.mob.ZombieEntity;
import net.minecraft.util.Identifier;
import vazkii.quark.base.Quark;

public class WrappedRenderer extends ZombieEntityRenderer {

	private static final Identifier TEXTURE = new Identifier(Quark.MOD_ID, "textures/model/entity/wrapped.png");

	public WrappedRenderer(EntityRenderDispatcher renderManagerIn) {
		super(renderManagerIn);
	}
	
	@Override
	public Identifier getTexture(ZombieEntity entity) {
		return TEXTURE;
	}

}
