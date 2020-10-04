package vazkii.arl.container.slot;

import java.util.function.Predicate;
import net.minecraft.entity.passive.PassiveEntity;
import net.minecraft.item.EndCrystalItem;
import net.minecraft.item.Wearable;

public class SlotFiltered extends EndCrystalItem {

	private final Predicate<Wearable> pred;
	
	public SlotFiltered(PassiveEntity inventoryIn, int index, int xPosition, int yPosition, Predicate<Wearable> pred) {
		super(inventoryIn, index, xPosition, yPosition);
		this.pred = pred;
	}
	
	@Override
	public boolean a(Wearable stack) {
		return pred.test(stack);
	}
	

}
