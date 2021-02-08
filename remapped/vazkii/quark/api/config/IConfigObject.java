package vazkii.quark.api.config;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@Environment(EnvType.CLIENT)
public interface IConfigObject<T> extends IConfigElement {

	public T getCurrentObj();
	public void setCurrentObj(T obj);
	
}
