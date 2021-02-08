package vazkii.quark.content.world.client.render;

import net.minecraft.client.renderer.entity.EntityRendererManager;
import net.minecraft.client.renderer.entity.ZombieRenderer;
import net.minecraft.entity.monster.ZombieEntity;
import net.minecraft.util.ResourceLocation;
import vazkii.quark.base.Quark;

public class WrappedRenderer extends ZombieRenderer {

	private static final ResourceLocation TEXTURE = new ResourceLocation(Quark.MOD_ID, "textures/model/entity/wrapped.png");

	public WrappedRenderer(EntityRendererManager renderManagerIn) {
		super(renderManagerIn);
	}
	
	@Override
	public ResourceLocation getEntityTexture(ZombieEntity entity) {
		return TEXTURE;
	}

}
