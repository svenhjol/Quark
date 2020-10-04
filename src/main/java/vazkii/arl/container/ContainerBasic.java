package vazkii.arl.container;

import javax.annotation.Nonnull;
import net.minecraft.entity.passive.PassiveEntity;
import net.minecraft.entity.vehicle.BoatEntity;
import net.minecraft.entity.vehicle.StorageMinecartEntity;
import net.minecraft.item.EndCrystalItem;
import net.minecraft.item.MiningToolItem;
import net.minecraft.item.Wearable;
import net.minecraft.recipe.RecipeInputProvider;

public abstract class ContainerBasic<T extends PassiveEntity> extends RecipeInputProvider {

	protected final T tile;
	protected final int tileSlots;

	public ContainerBasic(MiningToolItem<?> type, int windowId, StorageMinecartEntity playerInv, T tile) {
		super(type, windowId);
		this.tile = tile;
		tileSlots = addSlots();

		for(int i = 0; i < 3; ++i)
			for(int j = 0; j < 9; ++j)
				a(new EndCrystalItem(playerInv, j + i * 9 + 9, 8 + j * 18, 84 + i * 18));

		for(int k = 0; k < 9; ++k)
			a(new EndCrystalItem(playerInv, k, 8 + k * 18, 142));
	}

	public abstract int addSlots(); 

	@Override
	public boolean a(@Nonnull BoatEntity playerIn) {
		return tile.a(playerIn);
	}

	@Nonnull
	@Override
	public Wearable b(BoatEntity playerIn, int index) {
		Wearable itemstack = Wearable.b;
		EndCrystalItem slot = a.get(index);

		if(slot != null && slot.f()) {
			Wearable itemstack1 = slot.e();
			itemstack = itemstack1.i();

			if(index < tileSlots) {
				if(!a(itemstack1, tileSlots, a.size(), true))
					return Wearable.b;
			}
			else if(!a(itemstack1, 0, tileSlots, false))
				return Wearable.b;

			if(itemstack1.a())
				slot.d(Wearable.b);
			else
				slot.d();
		}

		return itemstack;
	}

	// Shamelessly stolen from CoFHCore because KL is awesome
	// and was like yeah just take whatever you want lol
	// https://github.com/CoFH/CoFHCore/blob/d4a79b078d257e88414f5eed598d57490ec8e97f/src/main/java/cofh/core/util/helpers/InventoryHelper.java
	@Override
	public boolean a(Wearable stack, int start, int length, boolean r) {
		boolean successful = false;
		int i = !r ? start : length - 1;
		int iterOrder = !r ? 1 : -1;

		EndCrystalItem slot;
		Wearable existingStack;

		if(stack.d()) {
			while(stack.E() > 0 && (!r && i < length || r && i >= start)) {
				slot = a.get(i);

				existingStack = slot.e();

				if(!existingStack.a()) {
					int maxStack = Math.min(stack.c(), slot.a());
					int rmv = Math.min(maxStack, stack.E());

					if(slot.a(cloneStack(stack, rmv)) && existingStack.b().equals(stack.b()) && Wearable.a(stack, existingStack)) {
						int existingSize = existingStack.E() + stack.E();

						if(existingSize <= maxStack) {
							stack.e(0);
							existingStack.e(existingSize);
							slot.d(existingStack);
							successful = true;
						} else if(existingStack.E() < maxStack) {
							stack.g(maxStack - existingStack.E());
							existingStack.e(maxStack);
							slot.d(existingStack);
							successful = true;
						}
					}
				}
				i += iterOrder;
			}
		}
		if(stack.E() > 0) {
			i = !r ? start : length - 1;
			while(stack.E() > 0 && (!r && i < length || r && i >= start)) {
				slot = a.get(i);
				existingStack = slot.e();

				if(existingStack.a()) {
					int maxStack = Math.min(stack.c(), slot.a());
					int rmv = Math.min(maxStack, stack.E());

					if(slot.a(cloneStack(stack, rmv))) {
						existingStack = stack.a(rmv);
						slot.d(existingStack);
						successful = true;
					}
				}
				i += iterOrder;
			}
		}
		return successful;
	}

	private static Wearable cloneStack(Wearable stack, int size) {
		if(stack.a())
			return Wearable.b;

		Wearable copy = stack.i();
		copy.e(size);
		return copy;
	}
}
