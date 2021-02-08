package vazkii.quark.content.management.module;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.ingame.CreativeInventoryScreen;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ArmorItem;
import net.minecraft.item.ElytraItem;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.slot.Slot;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.GuiScreenEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import vazkii.quark.base.module.LoadModule;
import vazkii.quark.base.module.ModuleCategory;
import vazkii.quark.base.module.QuarkModule;
import vazkii.quark.base.network.QuarkNetwork;
import vazkii.quark.base.network.message.SwapArmorMessage;

@LoadModule(category = ModuleCategory.MANAGEMENT, hasSubscriptions = true)
public class RightClickArmorModule extends QuarkModule {

	private static boolean shouldCancelNextRelease = false;
	
	@SubscribeEvent 
	@Environment(EnvType.CLIENT)
	public void onRightClick(GuiScreenEvent.MouseClickedEvent.Pre event) {
		MinecraftClient mc = MinecraftClient.getInstance();
		Screen gui = mc.currentScreen;
		if(gui instanceof HandledScreen && !(gui instanceof CreativeInventoryScreen) && event.getButton() == 1 && !Screen.hasShiftDown()) {
			HandledScreen<?> container = (HandledScreen<?>) gui;
			Slot under = container.getSlotUnderMouse();
			if(under != null) {
				ItemStack held = mc.player.inventory.getCursorStack();

				if(held.isEmpty() && swap(mc.player, under.id)) {
					QuarkNetwork.sendToServer(new SwapArmorMessage(under.id));
					
					shouldCancelNextRelease = true;
					event.setCanceled(true);
				}
			}
		}
	}
	
	public static boolean swap(PlayerEntity player, int slot) {
		Slot slotUnder = player.currentScreenHandler.getSlot(slot);
		ItemStack stack = slotUnder.getStack();
		
		EquipmentSlot equipSlot = null;
		
		if(stack.getItem() instanceof ArmorItem) {
			ArmorItem armor = (ArmorItem) stack.getItem();
			equipSlot = armor.getSlotType();
		} else if(stack.getItem() instanceof ElytraItem)
			equipSlot = EquipmentSlot.CHEST;
		
		if(equipSlot != null) {
			ItemStack currArmor = player.getEquippedStack(equipSlot);
			
			if(slotUnder.canTakeItems(player) && slotUnder.canInsert(currArmor)) 
				if(currArmor.isEmpty() || (EnchantmentHelper.getLevel(Enchantments.BINDING_CURSE, currArmor) == 0 && currArmor != stack)) {
					player.equipStack(equipSlot, stack.copy());
					
					slotUnder.inventory.setStack(slotUnder.getSlotIndex(), currArmor.copy());
					slotUnder.onStackChanged(stack, currArmor);
					return true;
				}
		}
		
		return false;
	}

	@SubscribeEvent(priority = EventPriority.HIGHEST)
	@Environment(EnvType.CLIENT)
	public void onRightClickRelease(GuiScreenEvent.MouseReleasedEvent.Pre event) {
		if(shouldCancelNextRelease) {
			shouldCancelNextRelease = false;
			event.setCanceled(true);
		}
	}
	
}
