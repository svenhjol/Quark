package vazkii.quark.tweaks.client.emote;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.render.entity.model.BipedEntityModel;
import net.minecraft.client.resource.language.I18n;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.Identifier;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@Environment(EnvType.CLIENT)
public class EmoteDescriptor {

	public static final Identifier TIER_1 = new Identifier("quark", "textures/emote/patreon_t1.png");
	public static final Identifier TIER_2 = new Identifier("quark", "textures/emote/patreon_t2.png");
	public static final Identifier TIER_3 = new Identifier("quark", "textures/emote/patreon_t3.png");
	public static final Identifier TIER_4 = new Identifier("quark", "textures/emote/patreon_t4.png");
	public static final Identifier TIER_GOD = new Identifier("quark", "textures/emote/patreon_t99.png");

	public final Class<? extends EmoteBase> clazz;
	public final int index;
	public final String name;
	public final String regName;
	public final Identifier texture;
	public final EmoteTemplate template;

	private int tier;
	
	public EmoteDescriptor(Class<? extends EmoteBase> clazz, String name, String regName, int index) {
		this(clazz, name, regName, index, new Identifier("quark", "textures/emote/" + name + ".png"), new EmoteTemplate(name + ".emote"));
	}
	
	public EmoteDescriptor(Class<? extends EmoteBase> clazz, String name, String regName, int index, Identifier texture, EmoteTemplate template) {
		this.clazz = clazz;
		this.index = index;
		this.name = name;
		this.regName = regName;
		this.texture = texture;
		this.template = template;
		this.tier = template.tier;
	}

	public void updateTier(EmoteTemplate template) {
		this.tier = template.tier;
	}
	
	public String getTranslationKey() {
		return "quark.emote." + name;
	}
	
	@Environment(EnvType.CLIENT)
	public String getLocalizedName() {
		return I18n.translate(getTranslationKey());
	}
	
	public String getRegistryName() {
		return regName;
	}
	
	public int getTier() {
		return tier;
	}

	public Identifier getTierTexture() {
		if (tier >= 99)
			return TIER_GOD;
		if (tier >= 4)
			return TIER_4;
		if (tier >= 3)
			return TIER_3;
		if (tier >= 2)
			return TIER_2;
		if (tier >= 1)
			return TIER_1;
		return null;
	}

	@Override
	public String toString() {
		return name;
	}

	public EmoteBase instantiate(PlayerEntity player, BipedEntityModel<?> model, BipedEntityModel<?> armorModel, BipedEntityModel<?> armorLegModel) {
		try {
			return clazz.getConstructor(EmoteDescriptor.class, PlayerEntity.class, BipedEntityModel.class, BipedEntityModel.class, BipedEntityModel.class).newInstance(this, player, model, armorModel, armorLegModel);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
	
}
