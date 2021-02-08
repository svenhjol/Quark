package vazkii.quark.content.tweaks.client.emote;

import cpw.mods.modlauncher.Launcher;
import cpw.mods.modlauncher.api.IEnvironment;
import net.minecraft.client.renderer.entity.model.BipedModel;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import vazkii.aurelienribon.tweenengine.Timeline;
import vazkii.quark.base.Quark;

@OnlyIn(Dist.CLIENT)
public class TemplateSourcedEmote extends EmoteBase {

	private static final boolean DEOBF = Launcher.INSTANCE.environment().getProperty(IEnvironment.Keys.NAMING.get()).orElse("").equals("mcp");

	public TemplateSourcedEmote(EmoteDescriptor desc, PlayerEntity player, BipedModel<?> model, BipedModel<?> armorModel, BipedModel<?> armorLegsModel) {
		super(desc, player, model, armorModel, armorLegsModel);

		if(shouldLoadTimelineOnLaunch()) {
			Quark.LOG.debug("Loading emote " + desc.getTranslationKey());
			desc.template.readAndMakeTimeline(desc, player, model);
		}
	}
	
	public boolean shouldLoadTimelineOnLaunch() {
		return DEOBF;
	}

	@Override
	public Timeline getTimeline(PlayerEntity player, BipedModel<?> model) {
		return desc.template.getTimeline(desc, player, model);
	}

	@Override
	public boolean usesBodyPart(int part) {
		return desc.template.usesBodyPart(part);
	}

}
