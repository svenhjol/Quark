/**
 * This class was created by <WireSegal>. It's distributed as
 * part of the Quark Mod. Get the Source Code in github:
 * https://github.com/Vazkii/Quark
 * <p>
 * Quark is Open Source and distributed under the
 * CC-BY-NC-SA 3.0 License: https://creativecommons.org/licenses/by-nc-sa/3.0/deed.en_GB
 * <p>
 * File Created @ [Jul 13, 2019, 13:30 AM (EST)]
 */
package vazkii.quark.content.mobs.client.render;

import java.util.UUID;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import net.minecraft.client.renderer.entity.EntityRendererManager;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.util.ResourceLocation;
import vazkii.quark.base.Quark;
import vazkii.quark.content.mobs.client.layer.FoxhoundCollarLayer;
import vazkii.quark.content.mobs.client.model.FoxhoundModel;
import vazkii.quark.content.mobs.entity.FoxhoundEntity;

public class FoxhoundRenderer extends MobRenderer<FoxhoundEntity, FoxhoundModel> {
	
	private static final ResourceLocation FOXHOUND_IDLE = new ResourceLocation(Quark.MOD_ID, "textures/model/entity/foxhound/red/idle.png");
	private static final ResourceLocation FOXHOUND_HOSTILE = new ResourceLocation(Quark.MOD_ID, "textures/model/entity/foxhound/red/hostile.png");
	private static final ResourceLocation FOXHOUND_SLEEPING = new ResourceLocation(Quark.MOD_ID, "textures/model/entity/foxhound/red/sleeping.png");
	
	private static final ResourceLocation SOULHOUND_IDLE = new ResourceLocation(Quark.MOD_ID, "textures/model/entity/foxhound/blue/idle.png");
	private static final ResourceLocation SOULHOUND_HOSTILE = new ResourceLocation(Quark.MOD_ID, "textures/model/entity/foxhound/blue/hostile.png");
	private static final ResourceLocation SOULHOUND_SLEEPING = new ResourceLocation(Quark.MOD_ID, "textures/model/entity/foxhound/blue/sleeping.png");

	private static final ResourceLocation BASALT_FOXHOUND_IDLE = new ResourceLocation(Quark.MOD_ID, "textures/model/entity/foxhound/black/idle.png");
	private static final ResourceLocation BASALT_FOXHOUND_HOSTILE = new ResourceLocation(Quark.MOD_ID, "textures/model/entity/foxhound/black/hostile.png");
	private static final ResourceLocation BASALT_FOXHOUND_SLEEPING = new ResourceLocation(Quark.MOD_ID, "textures/model/entity/foxhound/black/sleeping.png");
	
	private static final int SHINY_CHANCE = 256;
	
	public FoxhoundRenderer(EntityRendererManager render) {
		super(render, new FoxhoundModel(), 0.5F);
		addLayer(new FoxhoundCollarLayer(this));
	}

	@Nullable
	@Override
	public ResourceLocation getEntityTexture(@Nonnull FoxhoundEntity entity) {
		if(entity.isBlue())
			return entity.isSleeping() ? SOULHOUND_SLEEPING : (entity.getAngerTime() > 0 ? SOULHOUND_HOSTILE : SOULHOUND_IDLE); 

		UUID id = entity.getUniqueID();
		long most = id.getMostSignificantBits();
		if(SHINY_CHANCE > 0 && (most % SHINY_CHANCE) == 0)
			return entity.isSleeping() ? BASALT_FOXHOUND_SLEEPING : (entity.getAngerTime() > 0 ? BASALT_FOXHOUND_HOSTILE : BASALT_FOXHOUND_IDLE);
		
		return entity.isSleeping() ? FOXHOUND_SLEEPING : (entity.getAngerTime() > 0 ? FOXHOUND_HOSTILE : FOXHOUND_IDLE);
	}
}
