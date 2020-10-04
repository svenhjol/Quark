package vazkii.quark.management.module;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.slot.Slot;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.GuiScreenEvent.KeyboardKeyPressedEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import vazkii.quark.base.module.LoadModule;
import vazkii.quark.base.module.Module;
import vazkii.quark.base.module.ModuleCategory;
import vazkii.quark.base.module.ModuleLoader;
import vazkii.quark.base.network.QuarkNetwork;
import vazkii.quark.base.network.message.SwapItemsMessage;

@LoadModule(category = ModuleCategory.MANAGEMENT, hasSubscriptions = true, subscribeOn = Dist.CLIENT)
public class FToSwitchModule extends Module {

	@SubscribeEvent
	@Environment(EnvType.CLIENT)
	@SuppressWarnings("rawtypes")
	public void keyboardEvent(KeyboardKeyPressedEvent event) {
		MinecraftClient mc = MinecraftClient.getInstance();
		
		if(event.getKeyCode() == mc.options.keySwapHands.getKey().getCode() && event.getGui() instanceof HandledScreen) {
			HandledScreen gui = (HandledScreen) event.getGui();
			Slot slot = gui.getSlotUnderMouse();
			if(slot != null && slot.canTakeItems(mc.player)) {
				Inventory inv = slot.inventory;
				if(inv instanceof PlayerInventory) {
					int index = slot.getSlotIndex();

					if(index < ((PlayerInventory) inv).main.size()) {
						QuarkNetwork.sendToServer(new SwapItemsMessage(index));
						event.setCanceled(true);
					}
				}
			}
		}
	}

	public static void switchItems(PlayerEntity player, int slot) {
		if(!ModuleLoader.INSTANCE.isModuleEnabled(FToSwitchModule.class) || slot >= player.inventory.main.size())
			return;

		int offHandSlot = player.inventory.size() - 1;
		ItemStack stackAtSlot = player.inventory.getStack(slot);
		ItemStack stackAtOffhand = player.inventory.getStack(offHandSlot);

		player.inventory.setStack(slot, stackAtOffhand);
		player.inventory.setStack(offHandSlot, stackAtSlot);
	}
	
}
