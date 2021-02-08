package vazkii.quark.api;

import java.util.function.Predicate;

import net.minecraft.item.ItemStack;

/**
 * Implement this on an Item to change its behavior with the quark usage ticker.
 */
public interface IUsageTickerOverride {
	
	public default int getUsageTickerCountForItem(ItemStack stack, Predicate<ItemStack> target) {
		return 0;
	}
	
	public default boolean shouldUsageTickerCheckMatchSize(ItemStack stack) {
		return false;
	}
	
	public ItemStack getUsageTickerItem(ItemStack stack);

}
