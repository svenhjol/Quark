package vazkii.quark.tweaks.client.emote;

import cpw.mods.modlauncher.Launcher;
import cpw.mods.modlauncher.api.IEnvironment;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.render.entity.model.BipedEntityModel;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import vazkii.aurelienribon.tweenengine.Timeline;
import vazkii.quark.base.Quark;

@Environment(EnvType.CLIENT)
public class TemplateSourcedEmote extends EmoteBase {

	private static final boolean DEOBF = Launcher.INSTANCE.environment().getProperty(IEnvironment.Keys.NAMING.get()).orElse("").equals("mcp");

	public TemplateSourcedEmote(EmoteDescriptor desc, PlayerEntity player, BipedEntityModel<?> model, BipedEntityModel<?> armorModel, BipedEntityModel<?> armorLegsModel) {
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
	public Timeline getTimeline(PlayerEntity player, BipedEntityModel<?> model) {
		System.out.println(model);
		return desc.template.getTimeline(desc, player, model);
	}

	@Override
	public boolean usesBodyPart(int part) {
		return desc.template.usesBodyPart(part);
	}

}
