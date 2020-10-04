package vazkii.quark.client.tooltip;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Lists;
import com.google.common.collect.Multimap;
import com.mojang.blaze3d.systems.RenderSystem;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.ingame.EnchantmentScreen;
import net.minecraft.client.resource.language.I18n;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.enchantment.EnchantmentLevelEntry;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.text.LiteralText;
import net.minecraft.text.StringRenderable;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.RenderTooltipEvent;
import net.minecraftforge.event.entity.player.ItemTooltipEvent;
import net.minecraftforge.registries.GameData;
import vazkii.quark.base.item.QuarkItem;
import vazkii.quark.base.module.ModuleLoader;
import vazkii.quark.client.module.ImprovedTooltipsModule;
import vazkii.quark.tools.module.AncientTomesModule;
import vazkii.quark.tweaks.module.InfinityBucketModule;

public class EnchantedBookTooltips {

	private static List<ItemStack> testItems = null;
	private static Multimap<Enchantment, ItemStack> additionalStacks = null;

	public static void reloaded() {
		additionalStacks = null;
		testItems = null;
	}

	@Environment(EnvType.CLIENT)
	public static void makeTooltip(ItemTooltipEvent event) {
		if(event.getPlayer() == null)
			return;

		ItemStack stack = event.getItemStack();
		if(stack.getItem() == Items.ENCHANTED_BOOK || stack.getItem() == AncientTomesModule.ancient_tome) {
			MinecraftClient mc = MinecraftClient.getInstance();
			List<Text> tooltip = event.getToolTip();
			int tooltipIndex = 0;

			List<EnchantmentLevelEntry> enchants = getEnchantedBookEnchantments(stack);
			for(EnchantmentLevelEntry ed : enchants) {
				Text match = ed.enchantment.getName(ed.level);

				for(; tooltipIndex < tooltip.size(); tooltipIndex++)
					if(tooltip.get(tooltipIndex).equals(match)) {
						List<ItemStack> items = getItemsForEnchantment(ed.enchantment);
						if(!items.isEmpty()) {
							int len = 3 + items.size() * 9;
							String spaces = "";
							while(mc.textRenderer.getWidth(spaces) < len)
								spaces += " ";

							tooltip.add(tooltipIndex + 1, new LiteralText(spaces));
						}

						break;
					}
			}
		}
	}

	private static final ThreadLocal<Enchantment> clueHolder = new ThreadLocal<>();
	private static final ThreadLocal<Integer> clueLevelHolder = ThreadLocal.withInitial(() -> 0);

	@Environment(EnvType.CLIENT)
	public static List<String> captureEnchantingData(List<String> list, EnchantmentScreen screen, Enchantment enchantment, int level) {
		ItemStack last = screen.stack;
		if (!last.isEmpty() && last.getItem() == Items.BOOK) {
			clueHolder.set(enchantment);
			clueLevelHolder.set(level);
			if(enchantment != null) {
				MinecraftClient mc = MinecraftClient.getInstance();
				int tooltipIndex = 0;

				String match = Formatting.strip(I18n.translate("container.enchant.clue", enchantment.getName(level).getString()));

				for(; tooltipIndex < list.size(); tooltipIndex++) {
					String line = Formatting.strip(list.get(tooltipIndex));
					if (line != null && line.equals(match)) {
						List<ItemStack> items = getItemsForEnchantment(enchantment);
						if (!items.isEmpty()) {
							int len = 3 + items.size() * 9;
							String spaces = "";
							while (mc.textRenderer.getWidth(spaces) < len)
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

	@Environment(EnvType.CLIENT)
	public static void renderTooltip(RenderTooltipEvent.PostText event) {
		ItemStack stack = event.getStack();

		Enchantment enchantment = clueHolder.get();
		int level = clueLevelHolder.get();

		RenderSystem.pushMatrix();
		RenderSystem.translatef(event.getX(), event.getY() + 12, 500);
		RenderSystem.scalef(0.5f, 0.5f, 1.0f);
		MinecraftClient mc = MinecraftClient.getInstance();
		List<? extends StringRenderable> tooltip = event.getLines();

		if (enchantment != null) {
			clueHolder.remove();
			clueLevelHolder.remove();
			String match = Formatting.strip(I18n.translate("container.enchant.clue", enchantment.getName(level).getString()));
			for(int tooltipIndex = 0; tooltipIndex < tooltip.size(); tooltipIndex++) {
				String line = Formatting.strip(tooltip.get(tooltipIndex).getString());
				if(line != null && line.equals(match)) {
					int drawn = 0;

					List<ItemStack> items = getItemsForEnchantment(enchantment);
					for(ItemStack testStack : items) {
						mc.getItemRenderer().renderGuiItemIcon(testStack, 6 + drawn * 18, tooltipIndex * 20 - 2);
						drawn++;
					}

					break;
				}
			}

		} else if(stack.getItem() == Items.ENCHANTED_BOOK || stack.getItem() == AncientTomesModule.ancient_tome) {
			List<EnchantmentLevelEntry> enchants = getEnchantedBookEnchantments(stack);

			for(EnchantmentLevelEntry ed : enchants) {
				String match = ed.enchantment.getName(ed.level).getString();
				for(int tooltipIndex = 0; tooltipIndex < tooltip.size(); tooltipIndex++) {
					String line = Formatting.strip(tooltip.get(tooltipIndex).getString());
					if(line != null && line.equals(match)) {
						int drawn = 0;

						List<ItemStack> items = getItemsForEnchantment(ed.enchantment);
						for(ItemStack testStack : items) {
							mc.getItemRenderer().renderGuiItemIcon(testStack, 6 + drawn * 18, tooltipIndex * 20 - 2);
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
			
			if(!stack.isEmpty() && e.isAcceptableItem(stack))
				list.add(stack);
		}

		if(getAdditionalStacks().containsKey(e))
			list.addAll(getAdditionalStacks().get(e));
		
		if(e == Enchantments.INFINITY && ModuleLoader.INSTANCE.isModuleEnabled(InfinityBucketModule.class))
			list.add(new ItemStack(Items.WATER_BUCKET));

		return list;
	}

	public static List<EnchantmentLevelEntry> getEnchantedBookEnchantments(ItemStack stack) {
		Map<Enchantment, Integer> enchantments = EnchantmentHelper.get(stack);

		List<EnchantmentLevelEntry> retList = new ArrayList<>(enchantments.size());

		for(Enchantment enchantment : enchantments.keySet()) {
			if (enchantment != null) {
				int level = enchantments.get(enchantment);
				retList.add(new EnchantmentLevelEntry(enchantment, level));
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
			Item item = GameData.getWrapper(Item.class).get(new Identifier(loc));
			if (item != null)
				testItems.add(new ItemStack(item));
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

			Enchantment ench = GameData.getWrapper(Enchantment.class).get(new Identifier(left));
			if(ench != null) {
				tokens = right.split(",");

				for(String itemId : tokens) {
					Item item = GameData.getWrapper(Item.class).get(new Identifier(itemId));
					if(item != null)
						additionalStacks.put(ench, new ItemStack(item));
				}
			}
		}
	}
}
