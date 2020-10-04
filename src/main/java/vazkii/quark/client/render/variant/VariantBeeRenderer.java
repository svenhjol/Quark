package vazkii.quark.client.render.variant;

import java.util.List;
import java.util.Locale;
import java.util.UUID;

import com.google.common.collect.ImmutableList;
import net.minecraft.client.render.entity.BeeEntityRenderer;
import net.minecraft.client.render.entity.EntityRenderDispatcher;
import net.minecraft.entity.passive.BeeEntity;
import net.minecraft.util.Identifier;
import vazkii.quark.base.Quark;
import vazkii.quark.client.module.VariantAnimalTexturesModule;

public class VariantBeeRenderer extends BeeEntityRenderer {

	private static final List<String> VARIANTS = ImmutableList.of(
			"acebee", "agenbee", "arobee", "beefluid", "beesexual", 
			"beequeer", "enbee", "gaybee", "interbee", "lesbeean", 
			"panbee", "polysexbee", "transbee", "helen");
	
	public VariantBeeRenderer(EntityRenderDispatcher renderManagerIn) {
		super(renderManagerIn);
	}
	
	@Override
	public Identifier getTexture(BeeEntity entity) {
		if(entity.hasCustomName() || VariantAnimalTexturesModule.everyBeeIsLGBT) {
			String custName = entity.hasCustomName() ? entity.getCustomName().getString().trim() : "";
			String name = custName.toLowerCase(Locale.ROOT);
			
			if(VariantAnimalTexturesModule.everyBeeIsLGBT) {
				UUID id = entity.getUuid();
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
				return new Identifier(Quark.MOD_ID, path);
			}
		}
		
		return super.getTexture(entity);
	}

}
