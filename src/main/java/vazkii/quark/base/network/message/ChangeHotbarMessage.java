package vazkii.quark.base.network.message;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.network.NetworkEvent.Context;
import vazkii.arl.network.IMessage;

public class ChangeHotbarMessage implements IMessage {

	private static final long serialVersionUID = -3942423443215625756L;

	public int bar;
	
	public ChangeHotbarMessage() { }
	
	public ChangeHotbarMessage(int bar) {
		this.bar = bar;
	}

	@Override
	public boolean receive(Context context) {
		context.enqueueWork(() -> {
			PlayerEntity player = context.getSender();

			if(bar > 0 && bar <= 3)
				for(int i = 0; i < 9; i++)
					swap(player.inventory, i, i + bar * 9);
		});
		
		return true;
	}
	
	public void swap(IInventory inv, int slot1, int slot2) {
		ItemStack stack1 = inv.getStackInSlot(slot1);
		ItemStack stack2 = inv.getStackInSlot(slot2);
		inv.setInventorySlotContents(slot2, stack1);
		inv.setInventorySlotContents(slot1, stack2);
	}
	
}
