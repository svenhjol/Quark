package vazkii.quark.tools.module;

import com.google.common.collect.Maps;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.enchantment.EnchantmentLevelEntry;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.item.EnchantedBookItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.loot.LootTables;
import net.minecraft.loot.condition.LootCondition;
import net.minecraft.loot.entry.ItemEntry;
import net.minecraft.loot.entry.LootPoolEntry;
import net.minecraft.loot.function.LootFunctionType;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.text.LiteralText;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;
import net.minecraftforge.event.AnvilUpdateEvent;
import net.minecraftforge.event.LootTableLoadEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.registries.ForgeRegistries;
import vazkii.quark.base.Quark;
import vazkii.quark.base.handler.MiscUtil;
import vazkii.quark.base.module.LoadModule;
import vazkii.quark.base.module.Module;
import vazkii.quark.base.module.ModuleCategory;
import vazkii.quark.base.module.config.Config;
import vazkii.quark.tools.item.AncientTomeItem;
import vazkii.quark.tools.loot.EnchantTome;
import vazkii.quark.world.module.MonsterBoxModule;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

@LoadModule(category = ModuleCategory.TOOLS, hasSubscriptions = true)
public class AncientTomesModule extends Module {

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
		if(event.getName().equals(LootTables.STRONGHOLD_LIBRARY_CHEST))
			weight = libraryWeight;
		else if(event.getName().equals(LootTables.SIMPLE_DUNGEON_CHEST))
			weight = dungeonWeight;
		else if(event.getName().equals(MonsterBoxModule.MONSTER_BOX_LOOT_TABLE))
			weight = monsterBoxWeight;
		
		if(weight > 0) {
			LootPoolEntry entry = ItemEntry.builder(ancient_tome)
					.weight(weight)
					.quality(itemQuality)
					.apply(() -> new EnchantTome(new LootCondition[0]))
					.build();
			
			MiscUtil.addToLootTable(event.getTable(), entry);
		}
	}

	@Override
	public void construct() {
		ancient_tome = new AncientTomeItem(this);

		tomeEnchantType = new LootFunctionType(new EnchantTome.Serializer());
		Registry.register(Registry.LOOT_FUNCTION_TYPE, new Identifier(Quark.MOD_ID, "tome_enchant"), tomeEnchantType);

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
				Map<Enchantment, Integer> enchants = EnchantmentHelper.get(right);
				Map<Enchantment, Integer> currentEnchants = EnchantmentHelper.get(left);
				boolean hasOverLevel = false;
				boolean hasMatching = false;
				for (Map.Entry<Enchantment, Integer> entry : enchants.entrySet()) {
					Enchantment enchantment = entry.getKey();
					if(enchantment == null)
						continue;

					int level = entry.getValue();
					if (level > enchantment.getMaxLevel()) {
						hasOverLevel = true;
						if (enchantment.isAcceptableItem(left)) {
							hasMatching = true;
							//remove incompatible enchantments
							for (Iterator<Enchantment> iterator = currentEnchants.keySet().iterator(); iterator.hasNext(); ) {
								Enchantment comparingEnchantment = iterator.next();
								if (comparingEnchantment == enchantment)
									continue;

								if (!comparingEnchantment.canCombine(enchantment)) {
									iterator.remove();
								}
							}
							currentEnchants.put(enchantment, level);
						}
					} else if (enchantment.isAcceptableItem(left)) {
						boolean compatible = true;
						//don't apply incompatible enchantments
						for (Enchantment comparingEnchantment : currentEnchants.keySet()) {
							if (comparingEnchantment == enchantment)
								continue;

							if (comparingEnchantment != null && !comparingEnchantment.canCombine(enchantment)) {
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
						EnchantmentHelper.set(currentEnchants, out);
						String name = event.getName();
						int cost = applyCost;
						if(name != null && !name.isEmpty()) {
							out.setCustomName(new LiteralText(name));
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
		Map<Enchantment, Integer> enchantsBook = EnchantmentHelper.get(book);
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
			EnchantedBookItem.addEnchantment(output, new EnchantmentLevelEntry(entry.getKey(), entry.getValue()));

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
		ListTag listnbt = EnchantedBookItem.getEnchantmentTag(stack);

		for(int i = 0; i < listnbt.size(); ++i) {
			CompoundTag compoundnbt = listnbt.getCompound(i);
			Enchantment e = ForgeRegistries.ENCHANTMENTS.getValue(Identifier.tryParse(compoundnbt.getString("id")));
			map.put(e, compoundnbt.getInt("lvl"));
		}

		return map;
	}

}
