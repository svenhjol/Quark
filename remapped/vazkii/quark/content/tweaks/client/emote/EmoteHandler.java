/**
 * This class was created by <Vazkii>. It's distributed as
 * part of the Quark Mod. Get the Source Code in github:
 * https://github.com/Vazkii/Quark
 *
 * Quark is Open Source and distributed under the
 * CC-BY-NC-SA 3.0 License: https://creativecommons.org/licenses/by-nc-sa/3.0/deed.en_GB
 *
 * File Created @ [26/03/2016, 21:37:17 (GMT)]
 */
package vazkii.quark.content.tweaks.client.emote;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.mojang.blaze3d.systems.RenderSystem;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.model.ModelPart;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.render.entity.EntityRenderDispatcher;
import net.minecraft.client.render.entity.PlayerEntityRenderer;
import net.minecraft.client.render.entity.feature.ArmorFeatureRenderer;
import net.minecraft.client.render.entity.feature.FeatureRenderer;
import net.minecraft.client.render.entity.model.BipedEntityModel;
import net.minecraft.client.render.entity.model.PlayerEntityModel;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityPose;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@Environment(EnvType.CLIENT)
public final class EmoteHandler {

	public static final String CUSTOM_EMOTE_NAMESPACE = "quark_custom";
	public static final String CUSTOM_PREFIX = "custom:";

	public static final Map<String, EmoteDescriptor> emoteMap = new LinkedHashMap<>();
	private static final Map<String, EmoteBase> playerEmotes = new HashMap<>();

	private static int count;

	public static void clearEmotes() {
		emoteMap.clear();
	}

	public static void addEmote(String name, Class<? extends EmoteBase> clazz) {
		EmoteDescriptor desc = new EmoteDescriptor(clazz, name, name, count++);
		emoteMap.put(name, desc);
	}

	public static void addEmote(String name) {
		addEmote(name, TemplateSourcedEmote.class);
	}

	public static void addCustomEmote(String name) {
		String reg = CUSTOM_PREFIX + name;
		EmoteDescriptor desc = new CustomEmoteDescriptor(name, reg, count++);
		emoteMap.put(reg, desc);
	}

	@Environment(EnvType.CLIENT)
	public static void putEmote(Entity player, String emoteName, int tier) {
		if(player instanceof AbstractClientPlayerEntity && emoteMap.containsKey(emoteName)) {
			putEmote((AbstractClientPlayerEntity) player, emoteMap.get(emoteName), tier);
		}
	}

	@Environment(EnvType.CLIENT)
	private static void putEmote(AbstractClientPlayerEntity player, EmoteDescriptor desc, int tier) {
		String name = player.getGameProfile().getName();
		if(desc == null)
			return;

		if(desc.getTier() > tier)
			return;

		BipedEntityModel<?> model = getPlayerModel(player);
		BipedEntityModel<?> armorModel = getPlayerArmorModel(player);
		BipedEntityModel<?> armorLegModel = getPlayerArmorLegModel(player);

		if(model != null && armorModel != null && armorLegModel != null) {
			resetPlayer(player);
			EmoteBase emote = desc.instantiate(player, model, armorModel, armorLegModel);
			emote.startAllTimelines();
			playerEmotes.put(name, emote);
		}
	}

	public static void updateEmotes(Entity e) {
		if(e instanceof AbstractClientPlayerEntity) {
			AbstractClientPlayerEntity player = (AbstractClientPlayerEntity) e;
			String name = player.getGameProfile().getName();

			if(player.getPose() == EntityPose.STANDING) {
				if(playerEmotes.containsKey(name)) {
					resetPlayer(player);
					
					EmoteBase emote = playerEmotes.get(name);
					boolean done = emote.isDone();

					if(!done)
						emote.update();
				}
			}
		}
	}

	public static void preRender(PlayerEntity player) {
		EmoteBase emote = getPlayerEmote(player);
		if (emote != null) {
			RenderSystem.pushMatrix();
			emote.rotateAndOffset();
		}
	}

	public static void postRender(PlayerEntity player) {
		EmoteBase emote = getPlayerEmote(player);
		if (emote != null) {
			RenderSystem.popMatrix();
		}
	}

	public static void onRenderTick(MinecraftClient mc) {
		World world = mc.world;
		if(world == null)
			return;

		for(PlayerEntity player : world.getPlayers())
			updatePlayerStatus(player);
	}

	private static void updatePlayerStatus(PlayerEntity e) {
		if(e instanceof AbstractClientPlayerEntity) {
			AbstractClientPlayerEntity player = (AbstractClientPlayerEntity) e;
			String name = player.getGameProfile().getName();

			if(playerEmotes.containsKey(name)) {
				EmoteBase emote = playerEmotes.get(name);
				boolean done = emote.isDone();
				if(done) {
					playerEmotes.remove(name);
					resetPlayer(player);
				} else
					emote.update();
			}
		}
	}

	public static EmoteBase getPlayerEmote(PlayerEntity player) {
		return playerEmotes.get(player.getGameProfile().getName());
	}

	private static PlayerEntityRenderer getRenderPlayer(AbstractClientPlayerEntity player) {
		MinecraftClient mc = MinecraftClient.getInstance();
		EntityRenderDispatcher manager = mc.getEntityRenderDispatcher();
		return manager.getSkinMap().get(player.getModel());
	}

	private static BipedEntityModel<?> getPlayerModel(AbstractClientPlayerEntity player) {
		PlayerEntityRenderer render = getRenderPlayer(player);
		if(render != null)
			return render.getModel();

		return null;
	}

	private static BipedEntityModel<?> getPlayerArmorModel(AbstractClientPlayerEntity player) {
		return getPlayerArmorModelForSlot(player, EquipmentSlot.CHEST);
	}

	private static BipedEntityModel<?> getPlayerArmorLegModel(AbstractClientPlayerEntity player) {
		return getPlayerArmorModelForSlot(player, EquipmentSlot.LEGS);
	}

	private static BipedEntityModel<?> getPlayerArmorModelForSlot(AbstractClientPlayerEntity player, EquipmentSlot slot) {
		PlayerEntityRenderer render = getRenderPlayer(player);
		if(render == null)
			return null;

		List<FeatureRenderer<AbstractClientPlayerEntity,
				PlayerEntityModel<AbstractClientPlayerEntity>>> list = render.features;
		for(FeatureRenderer<?, ?> r : list) {
			if(r instanceof ArmorFeatureRenderer)	
				return ((ArmorFeatureRenderer<?, ?, ?>) r).getArmor(slot);
		}
		
		return null;
	}
	
	private static void resetPlayer(AbstractClientPlayerEntity player) {
		resetModel(getPlayerModel(player));
		resetModel(getPlayerArmorModel(player));
		resetModel(getPlayerArmorLegModel(player));
	}

	private static void resetModel(BipedEntityModel<?> model) {
		if (model != null) {
			resetPart(model.head);
			resetPart(model.helmet);
			resetPart(model.torso);
			resetPart(model.leftArm);
			resetPart(model.rightArm);
			resetPart(model.leftLeg);
			resetPart(model.rightLeg);
			if(model instanceof PlayerEntityModel) {
				PlayerEntityModel<?> pmodel = (PlayerEntityModel<?>) model;
				resetPart(pmodel.jacket);
				resetPart(pmodel.leftSleeve);
				resetPart(pmodel.rightSleeve);
				resetPart(pmodel.leftPantLeg);
				resetPart(pmodel.rightPantLeg);
			}
			

			ModelAccessor.INSTANCE.resetModel(model);
		}
	}

	private static void resetPart(ModelPart part) {
		if(part != null)
			part.roll = 0F;
	}
}
