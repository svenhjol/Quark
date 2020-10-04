package vazkii.arl.item;

import net.minecraft.util.UseAction;
import vazkii.arl.util.RegistryHelper;

public class BasicItem extends UseAction {

	public BasicItem(String regname, a properties) {
		super(properties);
		
		RegistryHelper.registerItem(this, regname);
	}

}
