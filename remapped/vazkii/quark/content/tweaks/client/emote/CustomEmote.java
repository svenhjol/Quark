package vazkii.quark.content.tweaks.client.emote;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.render.entity.model.BipedEntityModel;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import vazkii.quark.content.tweaks.module.EmotesModule;

@Environment(EnvType.CLIENT)
public class CustomEmote extends TemplateSourcedEmote {

	public CustomEmote(EmoteDescriptor desc, PlayerEntity player, BipedEntityModel<?> model, BipedEntityModel<?> armorModel, BipedEntityModel<?> armorLegsModel) {
		super(desc, player, model, armorModel, armorLegsModel);
	}

	@Override
	public boolean shouldLoadTimelineOnLaunch() {
		return EmotesModule.customEmoteDebug;
	}

}
