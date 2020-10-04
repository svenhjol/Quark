package vazkii.arl.container.slot;

import net.minecraft.entity.passive.PassiveEntity;

public class SlotType extends SlotFiltered {

	public SlotType(PassiveEntity inventoryIn, int index, int xPosition, int yPosition, Class<?> clazz) {
		super(inventoryIn, index, xPosition, yPosition,
				(stack) -> clazz.isAssignableFrom(stack.b().getClass()));
	}

}
