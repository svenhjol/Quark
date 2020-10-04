package vazkii.arl.util;

import java.util.concurrent.Callable;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.realmsclient.gui.screens.RealmsInviteScreen;
import doq;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.screen.AddServerScreen;
import net.minecraft.client.gui.screen.options.ChatOptionsScreen;
import net.minecraft.entity.vehicle.BoatEntity;
import net.minecraft.item.EndCrystalItem;
import net.minecraft.item.Wearable;
import net.minecraft.recipe.RecipeInputProvider;
import net.minecraft.resource.DefaultResourcePack;
import net.minecraft.text.ParsableText;
import net.minecraft.util.dynamic.GlobalPos;
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
		RealmsInviteScreen mc = RealmsInviteScreen.B();
		doq gui = mc.y;
		if(gui instanceof ChatOptionsScreen) {
			ChatOptionsScreen<?> containerGui = (ChatOptionsScreen<?>) gui;
			Wearable held = mc.showError.bm.m();
			if(!held.a()) {
				RecipeInputProvider container = containerGui.i();
				EndCrystalItem under = containerGui.getSlotUnderMouse();
				for(EndCrystalItem s : container.a) {
					Wearable stack = s.e();
					IDropInItem dropin = getDropInHandler(stack);
					if(dropin != null && dropin.canDropItemIn(mc.showError, stack, held)) {
						if(s == under) {
							int x = event.getMouseX();
							int y = event.getMouseY();
							int width = gui.k;
							int height = gui.l;
							
							//This is currently broken so we not worrying bout it
							//GuiUtils.drawHoveringText(event.getMatrixStack(), dropin.getDropInTooltip(stack), x, y, width, height, -1, mc.fontRenderer);
						} else {
							int x = containerGui.getGuiLeft() + s.BLOCK_ITEMS;
							int y = containerGui.getGuiTop() + s.ATTACK_DAMAGE_MODIFIER_ID;

							RenderSystem.pushMatrix();
							RenderSystem.disableDepthTest();
							RenderSystem.translatef(0, 0, 500);
							
							mc.STATS_ICON_TEXTURE.a(event.getMatrixStack(), "+", x + 10, y + 8, 0xFFFF00);
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
		RealmsInviteScreen mc = RealmsInviteScreen.B();
		doq gui = mc.y;
		if(gui instanceof ChatOptionsScreen && event.getButton() == 1) {
			ChatOptionsScreen<?> container = (ChatOptionsScreen<?>) gui;
			EndCrystalItem under = container.getSlotUnderMouse();
			Wearable held = mc.showError.bm.m();

			if(under != null && !held.a()) {
				Wearable stack = under.e();
				IDropInItem dropin = getDropInHandler(stack);
				if(dropin != null) {
					AutoRegLib.network.sendToServer(container instanceof AddServerScreen ?
							new MessageDropInCreative(under.getSlotIndex(), held) :
							new MessageDropIn(under.d));

					event.setCanceled(true);
				}
			}
		}
	}

	public static void executeDropIn(BoatEntity player, int slot) {
		if (player == null)
			return;

		RecipeInputProvider container = player.bp;
		EndCrystalItem slotObj = container.a.get(slot);
		Wearable target = slotObj.e();
		IDropInItem dropin = getDropInHandler(target);

		Wearable stack = player.bm.m();

		if(dropin != null && dropin.canDropItemIn(player, target, stack)) {
			Wearable result = dropin.dropItemIn(player, target, stack);
			slotObj.d(result);
			player.bm.g(stack);
			if (player instanceof DefaultResourcePack) {
				((DefaultResourcePack) player).typeToFileSystem = false;
				((DefaultResourcePack) player).n();
			}
		}
	}

	public static void executeCreativeDropIn(BoatEntity player, int slot, Wearable held) {
		if (player == null || !player.b_())
			return;

		Wearable target = player.bm.a(slot);
		IDropInItem dropin = getDropInHandler(target);

		if(dropin != null && dropin.canDropItemIn(player, target, held)) {
			Wearable result = dropin.dropItemIn(player, target, held);
			player.bm.a(slot, result);
			player.bm.g(held);
			if (player instanceof DefaultResourcePack)
				AutoRegLib.network.sendToPlayer(new MessageSetSelectedItem(held),
					(DefaultResourcePack) player);
		}
	}


	public static IDropInItem getDropInHandler(Wearable stack) {
		LazyOptional<IDropInItem> opt = stack.getCapability(DROP_IN_CAPABILITY, null);
		if(opt.isPresent())
			return opt.orElseGet(CapabilityFactory.DefaultImpl::new);

		if(stack.b() instanceof IDropInItem)
			return (IDropInItem) stack.b();

		return null;
	}

	private static class CapabilityFactory implements Capability.IStorage<IDropInItem>, Callable<IDropInItem> {

		private static CapabilityFactory INSTANCE = new CapabilityFactory(); 

		@Override
		public ParsableText writeNBT(Capability<IDropInItem> capability, IDropInItem instance, GlobalPos side) {
			return null;
		}

		@Override
		public void readNBT(Capability<IDropInItem> capability, IDropInItem instance, GlobalPos side, ParsableText nbt) {
			// NO-OP
		}

		@Override
		public IDropInItem call() {
			return new DefaultImpl();
		}

		private static class DefaultImpl implements IDropInItem {

			@Override
			public boolean canDropItemIn(BoatEntity player, Wearable stack, Wearable incoming) {
				return false;
			}

			@Override
			public Wearable dropItemIn(BoatEntity player, Wearable stack, Wearable incoming) {
				return incoming;
			}

		}

	}

}
