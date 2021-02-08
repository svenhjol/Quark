/**
 * This class was created by <WireSegal>. It's distributed as
 * part of the Quark Mod. Get the Source Code in github:
 * https://github.com/Vazkii/Quark
 * <p>
 * Quark is Open Source and distributed under the
 * CC-BY-NC-SA 3.0 License: https://creativecommons.org/licenses/by-nc-sa/3.0/deed.en_GB
 * <p>
 * File Created @ [Apr 17, 2019, 15:02 AM (EST)]
 */
package vazkii.quark.base.capability;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import net.minecraft.item.ItemStack;
import net.minecraft.util.Direction;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.event.AttachCapabilitiesEvent;

public class SelfProvider<V> implements ICapabilityProvider {

	private final Capability<V> capability;
	private final V self;

	public SelfProvider(Capability<V> capability, V self) {
		this.capability = capability;
		this.self = self;
	}

	@SuppressWarnings("unchecked")
	public static <V> SelfProvider<V> provide(Capability<V> capability, Object self) {
		return new SelfProvider<>(capability, (V) self);
	}

	public static <V> void attachItem(ResourceLocation location,
									  Capability<V> capability,
									  AttachCapabilitiesEvent<ItemStack> event) {
		event.addCapability(location, provide(capability, event.getObject().getItem()));
	}

	public static <V, C extends ICapabilityProvider> void attach(ResourceLocation location,
																 Capability<V> capability,
																 AttachCapabilitiesEvent<C> event) {
		event.addCapability(location, provide(capability, event.getObject()));
	}


	@Nullable
	@Override
	@SuppressWarnings("unchecked")
	public <T> LazyOptional<T> getCapability(@Nonnull Capability<T> capability, @Nullable Direction facing) {
		return capability == this.capability  ? LazyOptional.of(() -> (T) self) : LazyOptional.empty();
	}
}
