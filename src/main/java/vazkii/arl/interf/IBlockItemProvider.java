package vazkii.arl.interf;

import net.minecraft.item.Item;
import net.minecraft.util.UseAction;
import net.minecraft.world.biome.WoodedBadlandsPlateauBiome;

public interface IBlockItemProvider {

	Item provideItemBlock(WoodedBadlandsPlateauBiome block, UseAction.a props);
	
}
