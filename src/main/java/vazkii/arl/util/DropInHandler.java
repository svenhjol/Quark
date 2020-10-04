package vazkii.arl.util;

import java.util.concurrent.Callable;

import com.mojang.blaze3d.systems.RenderSystem;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.ingame.CreativeInventoryScreen;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.Tag;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.slot.Slot;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.math.Direction;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.GuiScreenEvent;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityInject;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.client.gui.GuiUtils;
import net.minecraftforge.fml.common.Mod;
import vazkii.arl.AutoRegLib;
import vazkii.arl.interf.IDropInItem;
import vazkii.arl.network.message.MessageDropIn;
import vazkii.arl.network.message.MessageDropInCreative;
import vazkii.arl.network.message.MessageSetSelectedItem;

@Mod.EventBusSubscriber(value = Dist.CLIENT, modid = AutoRegLib.MOD_ID)
public final class DropInHandler {
	
	@CapabilityInject(IDropInItem.class)
	public static Capability<IDropInItem> DROP_IN_CAPABILITY = null;

	public static void register() {
		CapabilityManager.INSTANCE.register(IDropInItem.class, CapabilityFactory.INSTANCE, CapabilityFactory.INSTANCE);
	}

	@SubscribeEvent
	@Environment(EnvType.CLIENT)
	public static void onDrawScreen(GuiScreenEvent.DrawScreenEvent.Post event) {
		MinecraftClient mc = MinecraftClient.getInstance();
		Screen gui = mc.currentScreen;
		if(gui instanceof HandledScreen) {
			HandledScreen<?> containerGui = (HandledScreen<?>) gui;
			ItemStack held = mc.player.inventory.getCursorStack();
			if(!held.isEmpty()) {
				ScreenHandler container = containerGui.getScreenHandler();
				Slot under = containerGui.getSlotUnderMouse();
				for(Slot s : container.slots) {
					ItemStack stack = s.getStack();
					IDropInItem dropin = getDropInHandler(stack);
					if(dropin != null && dropin.canDropItemIn(mc.player, stack, held)) {
						if(s == under) {
							int x = event.getMouseX();
							int y = event.getMouseY();
							int width = gui.width;
							int height = gui.height;
							
							//This is currently broken so we not worrying bout it
							//GuiUtils.drawHoveringText(event.getMatrixStack(), dropin.getDropInTooltip(stack), x, y, width, height, -1, mc.fontRenderer);
						} else {
							int x = containerGui.getGuiLeft() + s.x;
							int y = containerGui.getGuiTop() + s.y;

							RenderSystem.pushMatrix();
							RenderSystem.disableDepthTest();
							RenderSystem.translatef(0, 0, 500);
							
							mc.textRenderer.drawWithShadow(event.getMatrixStack(), "+", x + 10, y + 8, 0xFFFF00);
							RenderSystem.enableDepthTest();
							RenderSystem.popMatrix();
						}
					}
				}
			}
		}
	}

	@SubscribeEvent
	@Environment(EnvType.CLIENT)
	public static void onRightClick(GuiScreenEvent.MouseReleasedEvent.Pre event) {
		MinecraftClient mc = MinecraftClient.getInstance();
		Screen gui = mc.currentScreen;
		if(gui instanceof HandledScreen && event.getButton() == 1) {
			HandledScreen<?> container = (HandledScreen<?>) gui;
			Slot under = container.getSlotUnderMouse();
			ItemStack held = mc.player.inventory.getCursorStack();

			if(under != null && !held.isEmpty()) {
				ItemStack stack = under.getStack();
				IDropInItem dropin = getDropInHandler(stack);
				if(dropin != null) {
					AutoRegLib.network.sendToServer(container instanceof CreativeInventoryScreen ?
							new MessageDropInCreative(under.getSlotIndex(), held) :
							new MessageDropIn(under.id));

					event.setCanceled(true);
				}
			}
		}
	}

	public static void executeDropIn(PlayerEntity player, int slot) {
		if (player == null)
			return;

		ScreenHandler container = player.currentScreenHandler;
		Slot slotObj = container.slots.get(slot);
		ItemStack target = slotObj.getStack();
		IDropInItem dropin = getDropInHandler(target);

		ItemStack stack = player.inventory.getCursorStack();

		if(dropin != null && dropin.canDropItemIn(player, target, stack)) {
			ItemStack result = dropin.dropItemIn(player, target, stack);
			slotObj.setStack(result);
			player.inventory.setCursorStack(stack);
			if (player instanceof ServerPlayerEntity) {
				((ServerPlayerEntity) player).skipPacketSlotUpdates = false;
				((ServerPlayerEntity) player).updateCursorStack();
			}
		}
	}

	public static void executeCreativeDropIn(PlayerEntity player, int slot, ItemStack held) {
		if (player == null || !player.isCreative())
			return;

		ItemStack target = player.inventory.getStack(slot);
		IDropInItem dropin = getDropInHandler(target);

		if(dropin != null && dropin.canDropItemIn(player, target, held)) {
			ItemStack result = dropin.dropItemIn(player, target, held);
			player.inventory.setStack(slot, result);
			player.inventory.setCursorStack(held);
			if (player instanceof ServerPlayerEntity)
				AutoRegLib.network.sendToPlayer(new MessageSetSelectedItem(held),
					(ServerPlayerEntity) player);
		}
	}


	public static IDropInItem getDropInHandler(ItemStack stack) {
		LazyOptional<IDropInItem> opt = stack.getCapability(DROP_IN_CAPABILITY, null);
		if(opt.isPresent())
			return opt.orElseGet(CapabilityFactory.DefaultImpl::new);

		if(stack.getItem() instanceof IDropInItem)
			return (IDropInItem) stack.getItem();

		return null;
	}

	private static class CapabilityFactory implements Capability.IStorage<IDropInItem>, Callable<IDropInItem> {

		private static CapabilityFactory INSTANCE = new CapabilityFactory(); 

		@Override
		public Tag writeNBT(Capability<IDropInItem> capability, IDropInItem instance, Direction side) {
			return null;
		}

		@Override
		public void readNBT(Capability<IDropInItem> capability, IDropInItem instance, Direction side, Tag nbt) {
			// NO-OP
		}

		@Override
		public IDropInItem call() {
			return new DefaultImpl();
		}

		private static class DefaultImpl implements IDropInItem {

			@Override
			public boolean canDropItemIn(PlayerEntity player, ItemStack stack, ItemStack incoming) {
				return false;
			}

			@Override
			public ItemStack dropItemIn(PlayerEntity player, ItemStack stack, ItemStack incoming) {
				return incoming;
			}

		}

	}

}
