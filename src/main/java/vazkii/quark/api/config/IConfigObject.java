package vazkii.quark.api.config;

import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public interface IConfigObject<T> extends IConfigElement {

	public T getCurrentObj();
	public void setCurrentObj(T obj);
	
}
