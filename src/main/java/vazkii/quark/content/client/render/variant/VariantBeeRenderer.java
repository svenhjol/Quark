package vazkii.quark.content.client.render.variant;

import java.util.List;
import java.util.Locale;
import java.util.UUID;

import com.google.common.collect.ImmutableList;

import net.minecraft.client.renderer.entity.BeeRenderer;
import net.minecraft.client.renderer.entity.EntityRendererManager;
import net.minecraft.entity.passive.BeeEntity;
import net.minecraft.util.ResourceLocation;
import vazkii.quark.base.Quark;
import vazkii.quark.content.client.module.VariantAnimalTexturesModule;

public class VariantBeeRenderer extends BeeRenderer {

	private static final List<String> VARIANTS = ImmutableList.of(
			"acebee", "agenbee", "arobee", "beefluid", "beesexual", 
			"beequeer", "enbee", "gaybee", "interbee", "lesbeean", 
			"panbee", "polysexbee", "transbee", "helen");
	
	public VariantBeeRenderer(EntityRendererManager renderManagerIn) {
		super(renderManagerIn);
	}
	
	@Override
	public ResourceLocation getEntityTexture(BeeEntity entity) {
		if(entity.hasCustomName() || VariantAnimalTexturesModule.everyBeeIsLGBT) {
			String custName = entity.hasCustomName() ? entity.getCustomName().getString().trim() : "";
			String name = custName.toLowerCase(Locale.ROOT);
			
			if(VariantAnimalTexturesModule.everyBeeIsLGBT) {
				UUID id = entity.getUniqueID();
				long most = id.getMostSignificantBits();
				name = VARIANTS.get(Math.abs((int) (most % VARIANTS.size())));
			}
			
			if(custName.matches("wire(se|bee)gal"))
				name = "enbee";
			
			if(VARIANTS.contains(name)) {
				String type = "normal";
				boolean angery = entity.hasStung();
				boolean nectar = entity.hasNectar();
				
				if(angery)
					type = nectar ? "angry_nectar" : "angry";
				else if(nectar)
					type = "nectar";
				
				String path = String.format("textures/model/entity/variants/bees/%s/%s.png", name, type);
				return new ResourceLocation(Quark.MOD_ID, path);
			}
		}
		
		return super.getEntityTexture(entity);
	}

}
