package vazkii.quark.content.tools.module;

import java.util.List;

import org.apache.commons.lang3.tuple.Pair;

import com.google.common.collect.ImmutableSet;
import com.mojang.blaze3d.platform.GlStateManager;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.ingame.CreativeInventoryScreen;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.client.item.ModelPredicateProviderRegistry;
import net.minecraft.client.render.item.ItemRenderer;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.slot.Slot;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.tag.ItemTags;
import net.minecraft.tag.Tag;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
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

	private static final Identifier SEED_POUCH_CAP = new Identifier(Quark.MOD_ID, "seed_pouch_drop_in");

	public static Item seed_pouch;

    public static Tag<Item> seedPouchHoldableTag;
	
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
    	seedPouchHoldableTag = ItemTags.createOptional(new Identifier(Quark.MOD_ID, "seed_pouch_holdable"));
    }

	@Environment(EnvType.CLIENT)
	public void clientSetup() {
		ModelPredicateProviderRegistry.register(seed_pouch, new Identifier("pouch_items"), SeedPouchItem::itemFraction);
	}

	@SubscribeEvent
	public void onAttachCapability(AttachCapabilitiesEvent<ItemStack> event) {
		if(event.getObject().getItem() == seed_pouch)
			event.addCapability(SEED_POUCH_CAP, new SeedPouchDropIn());
	}

	@SubscribeEvent 
	@Environment(EnvType.CLIENT)
	public void onRightClick(GuiScreenEvent.MouseClickedEvent.Pre event) {
		MinecraftClient mc = MinecraftClient.getInstance();
		Screen gui = mc.currentScreen;
		if(gui instanceof HandledScreen && !(gui instanceof CreativeInventoryScreen) && event.getButton() == 1) {
			HandledScreen<?> container = (HandledScreen<?>) gui;
			Slot under = container.getSlotUnderMouse();
			if(under != null) {
				ItemStack underStack = under.getStack();
				ItemStack held = mc.player.inventory.getCursorStack();

				if(underStack.getItem() == seed_pouch) {
					Pair<ItemStack, Integer> contents = SeedPouchItem.getContents(underStack);
					if(contents != null) {
						ItemStack seed = contents.getLeft();
						if(held.isEmpty()) {
							int takeOut = Math.min(seed.getMaxCount(), contents.getRight());

							ItemStack result = seed.copy();
							result.setCount(takeOut);
							mc.player.inventory.setCursorStack(result);

							QuarkNetwork.sendToServer(new WithdrawSeedsMessage(under.id));

							shouldCancelNextRelease = true;
							event.setCanceled(true);
						}
					}
				}
			}
		}
	}

	@SubscribeEvent(priority = EventPriority.HIGHEST)
	@Environment(EnvType.CLIENT)
	public void onRightClickRelease(GuiScreenEvent.MouseReleasedEvent.Pre event) {
		if(shouldCancelNextRelease) {
			shouldCancelNextRelease = false;
			event.setCanceled(true);
		}
	}

	@SubscribeEvent
	public void onItemPickup(EntityItemPickupEvent event) {
		PlayerEntity player = event.getPlayer();
		ItemStack stack = event.getItem().getStack();

		ItemStack main = player.getMainHandStack();
		ItemStack off = player.getOffHandStack();

		ImmutableSet<ItemStack> stacks = ImmutableSet.of(main, off);
		for(ItemStack heldStack : stacks)
			if(heldStack.getItem() == seed_pouch) {
				Pair<ItemStack, Integer> contents = SeedPouchItem.getContents(heldStack);
				if(contents != null) {
					ItemStack pouchStack = contents.getLeft();
					if(ItemStack.areItemsEqualIgnoreDamage(pouchStack, stack)) {
						int curr = contents.getRight();
						int missing = maxItems - curr;

						int count = stack.getCount();
						int toAdd = Math.min(missing, count);

						stack.setCount(count - toAdd);
						SeedPouchItem.setCount(heldStack, curr + toAdd);

						if(player.world instanceof ServerWorld)
							((ServerWorld) player.world).playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.ITEM_ARMOR_EQUIP_LEATHER, SoundCategory.PLAYERS, 0.2F, (player.world.random.nextFloat() - player.world.random.nextFloat()) * 1.4F + 2.0F);

						if(stack.getCount() == 0)
							break;
					}
				}
			}
	}

	@SubscribeEvent
	@Environment(EnvType.CLIENT)
	public void makeTooltip(ItemTooltipEvent event) {
		ItemStack stack = event.getItemStack();
		if(stack.getItem() == seed_pouch) {
			Pair<ItemStack, Integer> contents = SeedPouchItem.getContents(stack);
			if(contents != null) {	
				List<Text> tooltip = event.getToolTip();

				int stacks = Math.max(1, (contents.getRight() - 1) / contents.getLeft().getMaxCount() + 1);
				int len = 16 + stacks * 8;

				String s = "";
				MinecraftClient mc = MinecraftClient.getInstance();
				while(mc.textRenderer.getWidth(s) < len)
					s += " ";

				tooltip.add(1, new LiteralText(s));
				tooltip.add(1, new LiteralText(s));
			}

		}
	}

	@SubscribeEvent
	@Environment(EnvType.CLIENT)
	@SuppressWarnings("deprecation")
	public void renderTooltip(RenderTooltipEvent.PostText event) {
		ItemStack stack = event.getStack();
		if(stack.getItem() == seed_pouch) {
			Pair<ItemStack, Integer> contents = SeedPouchItem.getContents(stack);
			if(contents != null) {			
				ItemStack seed = contents.getLeft().copy();

				MinecraftClient mc = MinecraftClient.getInstance();
				ItemRenderer render = mc.getItemRenderer();

				int x = event.getX();
				int y = event.getY();

				int count = contents.getRight();
				int stacks = Math.max(1, (count - 1) / seed.getMaxCount() + 1);

				GlStateManager.pushMatrix();
				GlStateManager.translated(x, y + 12, 500);
				for(int i = 0; i < stacks; i++) {
					if(i == (stacks - 1))
						seed.setCount(count);

					GlStateManager.pushMatrix();
					GlStateManager.translated(8 * i, Math.sin(i * 498543) * 2, 0);

					render.renderInGuiWithOverrides(seed, 0, 0);
					render.renderGuiItemOverlay(mc.textRenderer, seed, 0, 0);
					GlStateManager.popMatrix();
				}
				GlStateManager.popMatrix();
			}
		}
	}

}
