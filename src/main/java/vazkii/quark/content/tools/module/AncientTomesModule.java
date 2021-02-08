package vazkii.quark.content.tools.module;

import com.google.common.collect.Maps;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentData;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.item.EnchantedBookItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.loot.ItemLootEntry;
import net.minecraft.loot.LootEntry;
import net.minecraft.loot.LootFunctionType;
import net.minecraft.loot.LootTables;
import net.minecraft.loot.conditions.ILootCondition;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.registry.Registry;
import net.minecraft.util.text.StringTextComponent;
import net.minecraftforge.event.AnvilUpdateEvent;
import net.minecraftforge.event.LootTableLoadEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import vazkii.quark.base.Quark;
import vazkii.quark.base.handler.MiscUtil;
import vazkii.quark.base.module.LoadModule;
import vazkii.quark.base.module.QuarkModule;
import vazkii.quark.base.module.ModuleCategory;
import vazkii.quark.base.module.config.Config;
import vazkii.quark.content.tools.item.AncientTomeItem;
import vazkii.quark.content.tools.loot.EnchantTome;
import vazkii.quark.content.world.module.MonsterBoxModule;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

@LoadModule(category = ModuleCategory.TOOLS, hasSubscriptions = true)
public class AncientTomesModule extends QuarkModule {

	@Config public static int dungeonWeight = 20;
	@Config public static int libraryWeight = 30;
	@Config public static int monsterBoxWeight = 5;
	
	@Config public static int itemQuality = 2;
	@Config public static int mergeCost = 35;
	@Config public static int applyCost = 35;

	public static LootFunctionType tomeEnchantType;

	@Config(name = "Valid Enchantments")
	public static List<String> enchantNames = generateDefaultEnchantmentList();

	public static Item ancient_tome;
	public static final List<Enchantment> validEnchants = new ArrayList<>();
	private static boolean initialized = false;

	@SubscribeEvent
	public void onLootTableLoad(LootTableLoadEvent event) {
		int weight = 0;
		if(event.getName().equals(LootTables.CHESTS_STRONGHOLD_LIBRARY))
			weight = libraryWeight;
		else if(event.getName().equals(LootTables.CHESTS_SIMPLE_DUNGEON))
			weight = dungeonWeight;
		else if(event.getName().equals(MonsterBoxModule.MONSTER_BOX_LOOT_TABLE))
			weight = monsterBoxWeight;
		
		if(weight > 0) {
			LootEntry entry = ItemLootEntry.builder(ancient_tome)
					.weight(weight)
					.quality(itemQuality)
					.acceptFunction(() -> new EnchantTome(new ILootCondition[0]))
					.build();
			
			MiscUtil.addToLootTable(event.getTable(), entry);
		}
	}

	@Override
	public void construct() {
		ancient_tome = new AncientTomeItem(this);

		tomeEnchantType = new LootFunctionType(new EnchantTome.Serializer());
		Registry.register(Registry.LOOT_FUNCTION_TYPE, new ResourceLocation(Quark.MOD_ID, "tome_enchant"), tomeEnchantType);

	}

	@Override
	public void setup() {
		setupEnchantList();
		initialized = true;
	}

	@SubscribeEvent
	public void onAnvilUpdate(AnvilUpdateEvent event) {
		ItemStack left = event.getLeft();
		ItemStack right = event.getRight();

		if(!left.isEmpty() && !right.isEmpty()) {
			if(left.getItem() == Items.ENCHANTED_BOOK && right.getItem() == ancient_tome)
				handleTome(left, right, event);
			else if(right.getItem() == Items.ENCHANTED_BOOK && left.getItem() == ancient_tome)
				handleTome(right, left, event);

			else if(right.getItem() == Items.ENCHANTED_BOOK) {
				Map<Enchantment, Integer> enchants = EnchantmentHelper.getEnchantments(right);
				Map<Enchantment, Integer> currentEnchants = EnchantmentHelper.getEnchantments(left);
				boolean hasOverLevel = false;
				boolean hasMatching = false;
				for (Map.Entry<Enchantment, Integer> entry : enchants.entrySet()) {
					Enchantment enchantment = entry.getKey();
					if(enchantment == null)
						continue;

					int level = entry.getValue();
					if (level > enchantment.getMaxLevel()) {
						hasOverLevel = true;
						if (enchantment.canApply(left)) {
							hasMatching = true;
							//remove incompatible enchantments
							for (Iterator<Enchantment> iterator = currentEnchants.keySet().iterator(); iterator.hasNext(); ) {
								Enchantment comparingEnchantment = iterator.next();
								if (comparingEnchantment == enchantment)
									continue;

								if (!comparingEnchantment.isCompatibleWith(enchantment)) {
									iterator.remove();
								}
							}
							currentEnchants.put(enchantment, level);
						}
					} else if (enchantment.canApply(left)) {
						boolean compatible = true;
						//don't apply incompatible enchantments
						for (Enchantment comparingEnchantment : currentEnchants.keySet()) {
							if (comparingEnchantment == enchantment)
								continue;

							if (comparingEnchantment != null && !comparingEnchantment.isCompatibleWith(enchantment)) {
								compatible = false;
								break;
							}
						}
						if (compatible) {
							currentEnchants.put(enchantment, level);
						}
					}
				}

				if (hasOverLevel) {
					if (hasMatching) {
						ItemStack out = left.copy();
						EnchantmentHelper.setEnchantments(currentEnchants, out);
						String name = event.getName();
						int cost = applyCost;
						
						if(name != null && !name.isEmpty() && (!out.hasDisplayName() || !out.getDisplayName().getString().equals(name))) {
							out.setDisplayName(new StringTextComponent(name));
							cost++;
						}
						
						event.setOutput(out);
						event.setCost(cost);
					} else {
						event.setCanceled(true);
					}
				}
			}
		}
	}

	private void handleTome(ItemStack book, ItemStack tome, AnvilUpdateEvent event) {
		Map<Enchantment, Integer> enchantsBook = EnchantmentHelper.getEnchantments(book);
		Map<Enchantment, Integer> enchantsTome = getTomeEnchantments(tome);

		if (enchantsTome == null)
			return;

		for (Map.Entry<Enchantment, Integer> entry : enchantsTome.entrySet()) {
			if(enchantsBook.getOrDefault(entry.getKey(), 0).equals(entry.getValue()))
				enchantsBook.put(entry.getKey(), Math.min(entry.getValue(), entry.getKey().getMaxLevel()) + 1);
			else return;
		}

		ItemStack output = new ItemStack(Items.ENCHANTED_BOOK);
		for (Map.Entry<Enchantment, Integer> entry : enchantsBook.entrySet())
			EnchantedBookItem.addEnchantment(output, new EnchantmentData(entry.getKey(), entry.getValue()));

		event.setOutput(output);
		event.setCost(mergeCost);
	}

	private static List<String> generateDefaultEnchantmentList() {
		Enchantment[] enchants = new Enchantment[] {
				Enchantments.FEATHER_FALLING,
				Enchantments.THORNS,
				Enchantments.SHARPNESS,
				Enchantments.SMITE,
				Enchantments.BANE_OF_ARTHROPODS,
				Enchantments.KNOCKBACK,
				Enchantments.FIRE_ASPECT,
				Enchantments.LOOTING,
				Enchantments.SWEEPING,
				Enchantments.EFFICIENCY,
				Enchantments.UNBREAKING,
				Enchantments.FORTUNE,
				Enchantments.POWER,
				Enchantments.PUNCH,
				Enchantments.LUCK_OF_THE_SEA,
				Enchantments.LURE,
				Enchantments.LOYALTY,
				Enchantments.RIPTIDE,
				Enchantments.IMPALING,
				Enchantments.PIERCING
		};

		List<String> strings = new ArrayList<>();
		for(Enchantment e : enchants)
			if(e != null && e.getRegistryName() != null)
				strings.add(e.getRegistryName().toString());

		return strings;
	}

	@Override
	public void configChanged() {
		if(initialized)
			setupEnchantList();
	}

	private void setupEnchantList() {
		MiscUtil.initializeEnchantmentList(enchantNames, validEnchants);
		validEnchants.removeIf((ench) -> ench.getMaxLevel() == 1);
	}

	public static Map<Enchantment, Integer> getTomeEnchantments(ItemStack stack) {
		if (stack.getItem() != ancient_tome)
			return null;

		Map<Enchantment, Integer> map = Maps.newLinkedHashMap();
		ListNBT listnbt = EnchantedBookItem.getEnchantments(stack);

		for(int i = 0; i < listnbt.size(); ++i) {
			CompoundNBT compoundnbt = listnbt.getCompound(i);
			Registry.ENCHANTMENT.getOptional(ResourceLocation.tryCreate(compoundnbt.getString("id"))).ifPresent(e -> map.put(e, compoundnbt.getInt("lvl")));
		}

		return map;
	}

}
