package vazkii.quark.base.module.config;

import net.minecraft.loot.LootConditionType;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.registry.Registry;
import net.minecraftforge.common.crafting.CraftingHelper;
import vazkii.quark.base.Quark;
import vazkii.quark.base.module.QuarkModule;
import vazkii.quark.base.recipe.FlagIngredient;
import vazkii.quark.base.recipe.PotionIngredient;

import java.util.HashMap;
import java.util.Map;

public final class ConfigFlagManager {

	public static LootConditionType flagConditionType;

	private final Map<String, Boolean> flags = new HashMap<>();
	
	public ConfigFlagManager() {
		CraftingHelper.register(new FlagRecipeCondition.Serializer(this, new ResourceLocation(Quark.MOD_ID, "flag")));
		flagConditionType = new LootConditionType(new FlagLootCondition.Serializer(this));
		Registry.register(Registry.LOOT_CONDITION_TYPE, new ResourceLocation(Quark.MOD_ID, "flag"), flagConditionType);

		CraftingHelper.register(new ResourceLocation(Quark.MOD_ID, "potion"), PotionIngredient.Serializer.INSTANCE);
		CraftingHelper.register(new ResourceLocation(Quark.MOD_ID, "flag"),  new FlagIngredient.Serializer(this));
	}
	
	public void clear() {
		flags.clear();
	}
	
	public void putFlag(QuarkModule module, String flag, boolean value) {
		flags.put(flag, value && module.enabled);
	}
	
	public void putEnabledFlag(QuarkModule module) {
		flags.put(module.lowercaseName, module.enabled);
	}
	
	public boolean getFlag(String flag) {
		Boolean obj = flags.get(flag);
		return obj != null && obj;
	}
	
}
