package vazkii.quark.content.tools.module;

import java.util.List;

import org.apache.commons.lang3.tuple.Pair;

import com.google.common.collect.ImmutableSet;
import com.mojang.blaze3d.platform.GlStateManager;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.inventory.ContainerScreen;
import net.minecraft.client.gui.screen.inventory.CreativeScreen;
import net.minecraft.client.renderer.ItemRenderer;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.container.Slot;
import net.minecraft.item.Item;
import net.minecraft.item.ItemModelsProperties;
import net.minecraft.item.ItemStack;
import net.minecraft.tags.ITag;
import net.minecraft.tags.ItemTags;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvents;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.GuiScreenEvent;
import net.minecraftforge.client.event.RenderTooltipEvent;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.event.entity.player.EntityItemPickupEvent;
import net.minecraftforge.event.entity.player.ItemTooltipEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import vazkii.quark.base.Quark;
import vazkii.quark.base.module.LoadModule;
import vazkii.quark.base.module.ModuleCategory;
import vazkii.quark.base.module.QuarkModule;
import vazkii.quark.base.module.config.Config;
import vazkii.quark.base.network.QuarkNetwork;
import vazkii.quark.base.network.message.WithdrawSeedsMessage;
import vazkii.quark.content.tools.capability.SeedPouchDropIn;
import vazkii.quark.content.tools.item.SeedPouchItem;

@LoadModule(category = ModuleCategory.TOOLS, hasSubscriptions = true)
public class SeedPouchModule extends QuarkModule {

	private static final ResourceLocation SEED_POUCH_CAP = new ResourceLocation(Quark.MOD_ID, "seed_pouch_drop_in");

	public static Item seed_pouch;

    public static ITag<Item> seedPouchHoldableTag;
	
	@Config public static int maxItems = 640;
	@Config public static boolean showAllVariantsInCreative = true;
	@Config public static int shiftRange = 3;

	private static boolean shouldCancelNextRelease = false;

	@Override
	public void construct() {
		seed_pouch = new SeedPouchItem(this);
	}
	
    @Override
    public void setup() {
    	seedPouchHoldableTag = ItemTags.createOptional(new ResourceLocation(Quark.MOD_ID, "seed_pouch_holdable"));
    }

	@OnlyIn(Dist.CLIENT)
	public void clientSetup() {
		ItemModelsProperties.registerProperty(seed_pouch, new ResourceLocation("pouch_items"), SeedPouchItem::itemFraction);
	}

	@SubscribeEvent
	public void onAttachCapability(AttachCapabilitiesEvent<ItemStack> event) {
		if(event.getObject().getItem() == seed_pouch)
			event.addCapability(SEED_POUCH_CAP, new SeedPouchDropIn());
	}

	@SubscribeEvent 
	@OnlyIn(Dist.CLIENT)
	public void onRightClick(GuiScreenEvent.MouseClickedEvent.Pre event) {
		Minecraft mc = Minecraft.getInstance();
		Screen gui = mc.currentScreen;
		if(gui instanceof ContainerScreen && !(gui instanceof CreativeScreen) && event.getButton() == 1) {
			ContainerScreen<?> container = (ContainerScreen<?>) gui;
			Slot under = container.getSlotUnderMouse();
			if(under != null) {
				ItemStack underStack = under.getStack();
				ItemStack held = mc.player.inventory.getItemStack();

				if(underStack.getItem() == seed_pouch) {
					Pair<ItemStack, Integer> contents = SeedPouchItem.getContents(underStack);
					if(contents != null) {
						ItemStack seed = contents.getLeft();
						if(held.isEmpty()) {
							int takeOut = Math.min(seed.getMaxStackSize(), contents.getRight());

							ItemStack result = seed.copy();
							result.setCount(takeOut);
							mc.player.inventory.setItemStack(result);

							QuarkNetwork.sendToServer(new WithdrawSeedsMessage(under.slotNumber));

							shouldCancelNextRelease = true;
							event.setCanceled(true);
						}
					}
				}
			}
		}
	}

	@SubscribeEvent(priority = EventPriority.HIGHEST)
	@OnlyIn(Dist.CLIENT)
	public void onRightClickRelease(GuiScreenEvent.MouseReleasedEvent.Pre event) {
		if(shouldCancelNextRelease) {
			shouldCancelNextRelease = false;
			event.setCanceled(true);
		}
	}

	@SubscribeEvent
	public void onItemPickup(EntityItemPickupEvent event) {
		PlayerEntity player = event.getPlayer();
		ItemStack stack = event.getItem().getItem();

		ItemStack main = player.getHeldItemMainhand();
		ItemStack off = player.getHeldItemOffhand();

		ImmutableSet<ItemStack> stacks = ImmutableSet.of(main, off);
		for(ItemStack heldStack : stacks)
			if(heldStack.getItem() == seed_pouch) {
				Pair<ItemStack, Integer> contents = SeedPouchItem.getContents(heldStack);
				if(contents != null) {
					ItemStack pouchStack = contents.getLeft();
					if(ItemStack.areItemsEqual(pouchStack, stack)) {
						int curr = contents.getRight();
						int missing = maxItems - curr;

						int count = stack.getCount();
						int toAdd = Math.min(missing, count);

						stack.setCount(count - toAdd);
						SeedPouchItem.setCount(heldStack, curr + toAdd);

						if(player.world instanceof ServerWorld)
							((ServerWorld) player.world).playSound(null, player.getPosX(), player.getPosY(), player.getPosZ(), SoundEvents.ITEM_ARMOR_EQUIP_LEATHER, SoundCategory.PLAYERS, 0.2F, (player.world.rand.nextFloat() - player.world.rand.nextFloat()) * 1.4F + 2.0F);

						if(stack.getCount() == 0)
							break;
					}
				}
			}
	}

	@SubscribeEvent
	@OnlyIn(Dist.CLIENT)
	public void makeTooltip(ItemTooltipEvent event) {
		ItemStack stack = event.getItemStack();
		if(stack.getItem() == seed_pouch) {
			Pair<ItemStack, Integer> contents = SeedPouchItem.getContents(stack);
			if(contents != null) {	
				List<ITextComponent> tooltip = event.getToolTip();

				int stacks = Math.max(1, (contents.getRight() - 1) / contents.getLeft().getMaxStackSize() + 1);
				int len = 16 + stacks * 8;

				String s = "";
				Minecraft mc = Minecraft.getInstance();
				while(mc.fontRenderer.getStringWidth(s) < len)
					s += " ";

				tooltip.add(1, new StringTextComponent(s));
				tooltip.add(1, new StringTextComponent(s));
			}

		}
	}

	@SubscribeEvent
	@OnlyIn(Dist.CLIENT)
	@SuppressWarnings("deprecation")
	public void renderTooltip(RenderTooltipEvent.PostText event) {
		ItemStack stack = event.getStack();
		if(stack.getItem() == seed_pouch) {
			Pair<ItemStack, Integer> contents = SeedPouchItem.getContents(stack);
			if(contents != null) {			
				ItemStack seed = contents.getLeft().copy();

				Minecraft mc = Minecraft.getInstance();
				ItemRenderer render = mc.getItemRenderer();

				int x = event.getX();
				int y = event.getY();

				int count = contents.getRight();
				int stacks = Math.max(1, (count - 1) / seed.getMaxStackSize() + 1);

				GlStateManager.pushMatrix();
				GlStateManager.translated(x, y + 12, 500);
				for(int i = 0; i < stacks; i++) {
					if(i == (stacks - 1))
						seed.setCount(count);

					GlStateManager.pushMatrix();
					GlStateManager.translated(8 * i, Math.sin(i * 498543) * 2, 0);

					render.renderItemAndEffectIntoGUI(seed, 0, 0);
					render.renderItemOverlays(mc.fontRenderer, seed, 0, 0);
					GlStateManager.popMatrix();
				}
				GlStateManager.popMatrix();
			}
		}
	}

}
