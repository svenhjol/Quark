package vazkii.arl.container.slot;

import net.minecraft.inventory.Inventory;

public class SlotType extends SlotFiltered {

	public SlotType(Inventory inventoryIn, int index, int xPosition, int yPosition, Class<?> clazz) {
		super(inventoryIn, index, xPosition, yPosition,
				(stack) -> clazz.isAssignableFrom(stack.getItem().getClass()));
	}

}
