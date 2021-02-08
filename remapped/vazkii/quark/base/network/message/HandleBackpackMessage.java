package vazkii.quark.base.network.message;

import net.minecraft.entity.EquipmentSlot;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.NamedScreenHandlerFactory;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraftforge.fml.network.NetworkEvent.Context;
import net.minecraftforge.fml.network.NetworkHooks;
import vazkii.arl.network.IMessage;
import vazkii.quark.addons.oddities.container.BackpackContainer;

public class HandleBackpackMessage implements IMessage {

	private static final long serialVersionUID = 3474816381329541425L;

	public boolean open;

	public HandleBackpackMessage() { }

	public HandleBackpackMessage(boolean open) { 
		this.open = open;
	}

	@Override
	public boolean receive(Context context) {
		ServerPlayerEntity player = context.getSender();
		context.enqueueWork(() -> {
			if(open) {
				ItemStack stack = player.getEquippedStack(EquipmentSlot.CHEST);
				if(stack.getItem() instanceof NamedScreenHandlerFactory) {
					ItemStack holding = player.inventory.getCursorStack();
					player.inventory.setCursorStack(ItemStack.EMPTY);
					NetworkHooks.openGui(player, (NamedScreenHandlerFactory) stack.getItem(), player.getBlockPos());
					player.inventory.setCursorStack(holding);
				}
			} else {
				BackpackContainer.saveCraftingInventory(player);
				player.currentScreenHandler = player.playerScreenHandler;
			}
		});

		return true;
	}

}
