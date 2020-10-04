package vazkii.arl.block;

import net.minecraft.world.biome.WoodedBadlandsPlateauBiome;
import vazkii.arl.util.RegistryHelper;

public class BasicBlock extends WoodedBadlandsPlateauBiome {

	public BasicBlock(String regname, c properties) {
		super(properties);
		
		RegistryHelper.registerBlock(this, regname);
	}

}
