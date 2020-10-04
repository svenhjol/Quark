package vazkii.arl.container.slot;

import java.util.function.Predicate;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.slot.Slot;

public class SlotFiltered extends Slot {

	private final Predicate<ItemStack> pred;
	
	public SlotFiltered(Inventory inventoryIn, int index, int xPosition, int yPosition, Predicate<ItemStack> pred) {
		super(inventoryIn, index, xPosition, yPosition);
		this.pred = pred;
	}
	
	@Override
	public boolean canInsert(ItemStack stack) {
		return pred.test(stack);
	}
	

}
