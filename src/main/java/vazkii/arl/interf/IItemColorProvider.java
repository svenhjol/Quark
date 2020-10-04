package vazkii.arl.interf;

import com.mojang.realmsclient.util.JsonUtils;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public interface IItemColorProvider {

	@Environment(EnvType.CLIENT)
	public JsonUtils getItemColor();

}