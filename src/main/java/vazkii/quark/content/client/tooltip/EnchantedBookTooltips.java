package vazkii.quark.content.client.tooltip;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Lists;
import com.google.common.collect.Multimap;
import com.mojang.blaze3d.systems.RenderSystem;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screen.EnchantmentScreen;
import net.minecraft.client.resources.I18n;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentData;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.registry.Registry;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.ITextProperties;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.RenderTooltipEvent;
import net.minecraftforge.event.entity.player.ItemTooltipEvent;
import vazkii.quark.base.item.QuarkItem;
import vazkii.quark.base.module.ModuleLoader;
import vazkii.quark.content.client.module.ImprovedTooltipsModule;
import vazkii.quark.content.tools.module.AncientTomesModule;
import vazkii.quark.content.tweaks.module.InfinityBucketModule;

public class EnchantedBookTooltips {

	private static List<ItemStack> testItems = null;
	private static Multimap<Enchantment, ItemStack> additionalStacks = null;

	public static void reloaded() {
		additionalStacks = null;
		testItems = null;
	}

	@OnlyIn(Dist.CLIENT)
	public static void makeTooltip(ItemTooltipEvent event) {
		if(event.getPlayer() == null)
			return;

		ItemStack stack = event.getItemStack();
		if(stack.getItem() == Items.ENCHANTED_BOOK || stack.getItem() == AncientTomesModule.ancient_tome) {
			Minecraft mc = Minecraft.getInstance();
			List<ITextComponent> tooltip = event.getToolTip();
			int tooltipIndex = 0;

			List<EnchantmentData> enchants = getEnchantedBookEnchantments(stack);
			for(EnchantmentData ed : enchants) {
				ITextComponent match = ed.enchantment.getDisplayName(ed.enchantmentLevel);

				for(; tooltipIndex < tooltip.size(); tooltipIndex++)
					if(tooltip.get(tooltipIndex).equals(match)) {
						List<ItemStack> items = getItemsForEnchantment(ed.enchantment);
						if(!items.isEmpty()) {
							int len = 3 + items.size() * 9;
							String spaces = "";
							while(mc.fontRenderer.getStringWidth(spaces) < len)
								spaces += " ";

							tooltip.add(tooltipIndex + 1, new StringTextComponent(spaces));
						}

						break;
					}
			}
		}
	}

	private static final ThreadLocal<Enchantment> clueHolder = new ThreadLocal<>();
	private static final ThreadLocal<Integer> clueLevelHolder = ThreadLocal.withInitial(() -> 0);

	@OnlyIn(Dist.CLIENT)
	public static List<String> captureEnchantingData(List<String> list, EnchantmentScreen screen, Enchantment enchantment, int level) {
		ItemStack last = screen.last;
		if (!last.isEmpty() && last.getItem() == Items.BOOK) {
			clueHolder.set(enchantment);
			clueLevelHolder.set(level);
			if(enchantment != null) {
				Minecraft mc = Minecraft.getInstance();
				int tooltipIndex = 0;

				String match = TextFormatting.getTextWithoutFormattingCodes(I18n.format("container.enchant.clue", enchantment.getDisplayName(level).getString()));

				for(; tooltipIndex < list.size(); tooltipIndex++) {
					String line = TextFormatting.getTextWithoutFormattingCodes(list.get(tooltipIndex));
					if (line != null && line.equals(match)) {
						List<ItemStack> items = getItemsForEnchantment(enchantment);
						if (!items.isEmpty()) {
							int len = 3 + items.size() * 9;
							String spaces = "";
							while (mc.fontRenderer.getStringWidth(spaces) < len)
								spaces += " ";

							list.add(tooltipIndex + 1, spaces);
						}

						break;
					}
				}
			}
		}


		return list;
	}

	@OnlyIn(Dist.CLIENT)
	public static void renderTooltip(RenderTooltipEvent.PostText event) {
		ItemStack stack = event.getStack();

		Enchantment enchantment = clueHolder.get();
		int level = clueLevelHolder.get();

		RenderSystem.pushMatrix();
		RenderSystem.translatef(event.getX(), event.getY() + 12, 500);
		RenderSystem.scalef(0.5f, 0.5f, 1.0f);
		Minecraft mc = Minecraft.getInstance();
		List<? extends ITextProperties> tooltip = event.getLines();

		if (enchantment != null) {
			clueHolder.remove();
			clueLevelHolder.remove();
			String match = TextFormatting.getTextWithoutFormattingCodes(I18n.format("container.enchant.clue", enchantment.getDisplayName(level).getString()));
			for(int tooltipIndex = 0; tooltipIndex < tooltip.size(); tooltipIndex++) {
				String line = TextFormatting.getTextWithoutFormattingCodes(tooltip.get(tooltipIndex).getString());
				if(line != null && line.equals(match)) {
					int drawn = 0;

					List<ItemStack> items = getItemsForEnchantment(enchantment);
					for(ItemStack testStack : items) {
						mc.getItemRenderer().renderItemIntoGUI(testStack, 6 + drawn * 18, tooltipIndex * 20 - 2);
						drawn++;
					}

					break;
				}
			}

		} else if(stack.getItem() == Items.ENCHANTED_BOOK || stack.getItem() == AncientTomesModule.ancient_tome) {
			List<EnchantmentData> enchants = getEnchantedBookEnchantments(stack);

			for(EnchantmentData ed : enchants) {
				String match = ed.enchantment.getDisplayName(ed.enchantmentLevel).getString();
				for(int tooltipIndex = 0; tooltipIndex < tooltip.size(); tooltipIndex++) {
					String line = TextFormatting.getTextWithoutFormattingCodes(tooltip.get(tooltipIndex).getString());
					if(line != null && line.equals(match)) {
						int drawn = 0;

						List<ItemStack> items = getItemsForEnchantment(ed.enchantment);
						for(ItemStack testStack : items) {
							mc.getItemRenderer().renderItemIntoGUI(testStack, 6 + drawn * 18, tooltipIndex * 20 - 2);
							drawn++;
						}

						break;
					}
				}
			}
		}

		RenderSystem.popMatrix();
	}

	public static List<ItemStack> getItemsForEnchantment(Enchantment e) {
		List<ItemStack> list = new ArrayList<>();

		for(ItemStack stack : getTestItems()) {
			Item item = stack.getItem();
			if(item instanceof QuarkItem && !((QuarkItem) item).isEnabled())
				continue;
			
			if(!stack.isEmpty() && e.canApply(stack))
				list.add(stack);
		}

		if(getAdditionalStacks().containsKey(e))
			list.addAll(getAdditionalStacks().get(e));
		
		if(e == Enchantments.INFINITY && ModuleLoader.INSTANCE.isModuleEnabled(InfinityBucketModule.class))
			list.add(new ItemStack(Items.WATER_BUCKET));

		return list;
	}

	public static List<EnchantmentData> getEnchantedBookEnchantments(ItemStack stack) {
		Map<Enchantment, Integer> enchantments = EnchantmentHelper.getEnchantments(stack);

		List<EnchantmentData> retList = new ArrayList<>(enchantments.size());

		for(Enchantment enchantment : enchantments.keySet()) {
			if (enchantment != null) {
				int level = enchantments.get(enchantment);
				retList.add(new EnchantmentData(enchantment, level));
			}
		}

		return retList;
	}

	private static Multimap<Enchantment, ItemStack> getAdditionalStacks() {
		if (additionalStacks == null)
			computeAdditionalStacks();
		return additionalStacks;
	}

	private static List<ItemStack> getTestItems() {
		if (testItems == null)
			computeTestItems();
		return testItems;
	}

	private static void computeTestItems() {
		testItems = Lists.newArrayList();

		for (String loc : ImprovedTooltipsModule.enchantingStacks) {
			Registry.ITEM.getOptional(new ResourceLocation(loc)).ifPresent(item -> testItems.add(new ItemStack(item)));
		}
	}

	private static void computeAdditionalStacks() {
		additionalStacks = HashMultimap.create();

		for(String s : ImprovedTooltipsModule.enchantingAdditionalStacks) {
			if(!s.contains("="))
				continue;

			String[] tokens = s.split("=");
			String left = tokens[0];
			String right = tokens[1];

			Optional<Enchantment> ench = Registry.ENCHANTMENT.getOptional(new ResourceLocation(left));
			if(ench.isPresent()) {
				tokens = right.split(",");

				for(String itemId : tokens) {
					Registry.ITEM.getOptional(new ResourceLocation(itemId)).ifPresent(item -> additionalStacks.put(ench.get(), new ItemStack(item)));
				}
			}
		}
	}
}
