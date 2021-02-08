package vazkii.quark.content.tweaks.client.emote;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.resource.ResourceType;
import net.minecraft.util.Identifier;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import vazkii.quark.content.tweaks.module.EmotesModule;

@Environment(EnvType.CLIENT)
public class CustomEmoteDescriptor extends EmoteDescriptor {

	public CustomEmoteDescriptor(String name, String regName, int index) {
		super(CustomEmote.class, name, regName, index, getSprite(name), new CustomEmoteTemplate(name));
	}
	
	public static Identifier getSprite(String name) {
		Identifier customRes = new Identifier(EmoteHandler.CUSTOM_EMOTE_NAMESPACE, name);
		if(EmotesModule.resourcePack.contains(ResourceType.CLIENT_RESOURCES, customRes))
			return customRes;
		
		return new Identifier("quark", "textures/emotes/custom.png");
	}
	
	@Override
	public String getTranslationKey() {
		return ((CustomEmoteTemplate) template).getName();
	}
	
	@Override
	public String getLocalizedName() {
		return getTranslationKey();
	}

}
