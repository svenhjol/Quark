package vazkii.quark.addons.oddities.container;

import javax.annotation.Nonnull;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.slot.Slot;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.SlotItemHandler;

public class SlotCachingItemHandler extends SlotItemHandler {
	public SlotCachingItemHandler(IItemHandler itemHandler, int index, int xPosition, int yPosition) {
		super(itemHandler, index, xPosition, yPosition);
	}

	@Nonnull
	@Override
	public ItemStack getStack() {
		if (caching)
			return cached;
		return super.getStack();
	}

	@Nonnull
	@Override
	public ItemStack takeStack(int amount) {
		if (caching) {
			ItemStack newStack = cached.copy();
			int trueAmount = Math.min(amount, cached.getCount());
			cached.decrement(trueAmount);
			newStack.setCount(trueAmount);
			return newStack;
		}
		return super.takeStack(amount);
	}

	@Override
	public void setStack(@Nonnull ItemStack stack) {
		super.setStack(stack);
		if (caching)
			cached = stack;
	}

	private ItemStack cached = ItemStack.EMPTY;

	private boolean caching = false;

	public static void cache(ScreenHandler container) {
		for (Slot slot : container.slots) {
			if (slot instanceof SlotCachingItemHandler) {
				SlotCachingItemHandler thisSlot = (SlotCachingItemHandler) slot;
				thisSlot.cached = slot.getStack();
				thisSlot.caching = true;
			}
		}
	}

	public static void applyCache(ScreenHandler container) {
		for (Slot slot : container.slots) {
			if (slot instanceof SlotCachingItemHandler) {
				SlotCachingItemHandler thisSlot = (SlotCachingItemHandler) slot;
				if (thisSlot.caching) {
					slot.setStack(thisSlot.cached);
					thisSlot.caching = false;
				}
			}
		}
	}
}