package vazkii.arl.util;

import net.minecraft.util.dynamic.GlobalPos;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.util.LazyOptional;
import vazkii.arl.interf.IDropInItem;

import javax.annotation.Nonnull;

public abstract class AbstractDropIn implements ICapabilityProvider, IDropInItem {

	@Nonnull
	@Override
	public <T> LazyOptional<T> getCapability(@Nonnull Capability<T> capability, GlobalPos facing) {
		if(capability == DropInHandler.DROP_IN_CAPABILITY)
			return LazyOptional.of(() -> this).cast();
		else return LazyOptional.empty();
	}

}
