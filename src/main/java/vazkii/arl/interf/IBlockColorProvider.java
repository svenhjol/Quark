package vazkii.arl.interf;

import com.mojang.realmsclient.gui.screens.RealmsSubscriptionInfoScreen;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public interface IBlockColorProvider extends IItemColorProvider {

	@Environment(EnvType.CLIENT)
	public RealmsSubscriptionInfoScreen getBlockColor();

}