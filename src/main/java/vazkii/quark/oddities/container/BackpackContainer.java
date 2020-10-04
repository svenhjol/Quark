package vazkii.quark.oddities.container;

import javax.annotation.Nonnull;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.EquipmentSlot.Type;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.CraftingInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.screen.PlayerScreenHandler;
import net.minecraft.screen.ScreenHandlerType;
import net.minecraft.screen.slot.Slot;
import net.minecraft.screen.slot.SlotActionType;
import vazkii.arl.util.InventoryIIH;
import vazkii.quark.oddities.module.BackpackModule;

public class BackpackContainer extends PlayerScreenHandler {

	public BackpackContainer(int windowId, PlayerEntity player) {
		super(player.inventory, !player.world.isClient, player);

		this.syncId = windowId;
		
		for(Slot slot : slots)
			if (slot.inventory == player.inventory && slot.getSlotIndex() < player.inventory.size() - 5)
				slot.y += 58;

		Slot anchor = slots.get(9);
		int left = anchor.x;
		int top = anchor.y - 58;

		ItemStack backpack = player.inventory.armor.get(2);
		if(backpack.getItem() == BackpackModule.backpack) {
			InventoryIIH inv = new InventoryIIH(backpack);

			for(int i = 0; i < 3; ++i)
				for(int j = 0; j < 9; ++j) {
					int k = j + i * 9;
					addSlot(new SlotCachingItemHandler(inv, k, left + j * 18, top + i * 18));
				}
		}
	}

	public static BackpackContainer fromNetwork(int windowId, PlayerInventory playerInventory, PacketByteBuf buf) {
		return new BackpackContainer(windowId, playerInventory.player);
	}

	@Nonnull
	@Override
	public ItemStack transferSlot(@Nonnull PlayerEntity playerIn, int index) {
		ItemStack baseStack = ItemStack.EMPTY;
		Slot slot = this.slots.get(index);

		if (slot != null && slot.hasStack()) {
			ItemStack stack = slot.getStack();
			baseStack = stack.copy();
			EquipmentSlot slotType = stack.getEquipmentSlot();
			int equipIndex = 8 - (slotType == null ? 0 : slotType.getEntitySlotId());

			if (index == 0) {
				if (!this.insertItem(stack, 9, 45, false) && !this.insertItem(stack, 46, 73, false)) 
					return ItemStack.EMPTY;

				slot.onStackChanged(stack, baseStack);
			} else if (index < 5) {
				if (!this.insertItem(stack, 9, 45, false)) 
					return ItemStack.EMPTY;
			} else if (index < 9) {
				if (!this.insertItem(stack, 9, 45, false) && !this.insertItem(stack, 46, 73, false)) 
					return ItemStack.EMPTY;
			} else if (slotType != null && slotType.getType() == Type.ARMOR && !this.slots.get(equipIndex).hasStack()) {
				if (!this.insertItem(stack, equipIndex, equipIndex + 1, false)) 
					return ItemStack.EMPTY;
			} else if (slotType != null && slotType == EquipmentSlot.OFFHAND && !this.slots.get(45).hasStack()) {
				if (!this.insertItem(stack, 45, 46, false)) 
					return ItemStack.EMPTY;
			} else if (index < 36) {
				if (!this.insertItem(stack, 46, 73, false) && !this.insertItem(stack, 36, 45, false)) 
					return ItemStack.EMPTY;
			} else if (index < 73) {
				if (!this.insertItem(stack, 9, 36, false)) 
					return ItemStack.EMPTY;
			} else {
				if (!this.insertItem(stack, 46, 73, false) && !this.insertItem(stack, 9, 45, false)) 
					return ItemStack.EMPTY;
			}

			if (stack.isEmpty())
				slot.setStack(ItemStack.EMPTY);
			else slot.markDirty();

			if (stack.getCount() == baseStack.getCount())
				return ItemStack.EMPTY;

			ItemStack remainder = slot.onTakeItem(playerIn, stack);

			if (index == 0) 
				playerIn.dropItem(remainder, false);
		}

		return baseStack;
	}

	// Shamelessly stolen from CoFHCore because KL is awesome
	// and was like yeah just take whatever you want lol
	// https://github.com/CoFH/CoFHCore/blob/d4a79b078d257e88414f5eed598d57490ec8e97f/src/main/java/cofh/core/util/helpers/InventoryHelper.java
	@Override
	public boolean insertItem(ItemStack stack, int start, int length, boolean r) {
		boolean successful = false;
		int i = !r ? start : length - 1;
		int iterOrder = !r ? 1 : -1;

		Slot slot;
		ItemStack existingStack;

		if(stack.isStackable()) while (stack.getCount() > 0 && (!r && i < length || r && i >= start)) {
			slot = slots.get(i);

			existingStack = slot.getStack();

			if (!existingStack.isEmpty()) {
				int maxStack = Math.min(stack.getMaxCount(), slot.getMaxStackAmount());
				int rmv = Math.min(maxStack, stack.getCount());

				if (slot.canInsert(cloneStack(stack, rmv)) && existingStack.getItem().equals(stack.getItem()) && ItemStack.areTagsEqual(stack, existingStack)) {
					int existingSize = existingStack.getCount() + stack.getCount();

					if (existingSize <= maxStack) {
						stack.setCount(0);
						existingStack.setCount(existingSize);
						slot.setStack(existingStack);
						successful = true;
					} else if (existingStack.getCount() < maxStack) {
						stack.decrement(maxStack - existingStack.getCount());
						existingStack.setCount(maxStack);
						slot.setStack(existingStack);
						successful = true;
					}
				}
			}
			i += iterOrder;
		}
		if(stack.getCount() > 0) {
			i = !r ? start : length - 1;
			while(stack.getCount() > 0 && (!r && i < length || r && i >= start)) {
				slot = slots.get(i);
				existingStack = slot.getStack();

				if(existingStack.isEmpty()) {
					int maxStack = Math.min(stack.getMaxCount(), slot.getMaxStackAmount());
					int rmv = Math.min(maxStack, stack.getCount());

					if(slot.canInsert(cloneStack(stack, rmv))) {
						existingStack = stack.split(rmv);
						slot.setStack(existingStack);
						successful = true;
					}
				}
				i += iterOrder;
			}
		}
		return successful;
	}

	@Nonnull
	@Override
	public ItemStack onSlotClick(int slotId, int dragType, SlotActionType clickTypeIn, PlayerEntity player) {
		SlotCachingItemHandler.cache(this);
		ItemStack stack = super.onSlotClick(slotId, dragType, clickTypeIn, player);
		SlotCachingItemHandler.applyCache(this);
		return stack;
	}

	private static ItemStack cloneStack(ItemStack stack, int size) {
		if(stack.isEmpty())
			return ItemStack.EMPTY;

		ItemStack copy = stack.copy();
		copy.setCount(size);
		return copy;
	}

	public static void saveCraftingInventory(PlayerEntity player) {
		CraftingInventory crafting = ((PlayerScreenHandler) player.currentScreenHandler).craftingInput;
		for(int i = 0; i < crafting.size(); i++) {
			ItemStack stack = crafting.getStack(i);
			if(!stack.isEmpty() && !player.giveItemStack(stack))
				player.dropItem(stack, false);
		}
	}

	@Override
	public ScreenHandlerType<?> getType() {
		return BackpackModule.container;
	}

}
