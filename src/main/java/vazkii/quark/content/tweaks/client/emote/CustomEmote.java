package vazkii.quark.content.tweaks.client.emote;

import net.minecraft.client.renderer.entity.model.BipedModel;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import vazkii.quark.content.tweaks.module.EmotesModule;

@OnlyIn(Dist.CLIENT)
public class CustomEmote extends TemplateSourcedEmote {

	public CustomEmote(EmoteDescriptor desc, PlayerEntity player, BipedModel<?> model, BipedModel<?> armorModel, BipedModel<?> armorLegsModel) {
		super(desc, player, model, armorModel, armorLegsModel);
	}

	@Override
	public boolean shouldLoadTimelineOnLaunch() {
		return EmotesModule.customEmoteDebug;
	}

}
