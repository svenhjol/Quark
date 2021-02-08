package vazkii.quark.api;

import java.util.Comparator;

import net.minecraft.item.ItemStack;

public interface ICustomSorting {

	/**
	 * Gets a comparator to compare the items. 
	 */
	Comparator<ItemStack> getItemComparator();
	
	/**
	 * Gets this item's category. Items will only be compared together if they are in the same category.
	 * Make sure your category is unique. Prefixing it with the mod ID is a good idea.
	 */
	String getSortingCategory();
	
	
}
