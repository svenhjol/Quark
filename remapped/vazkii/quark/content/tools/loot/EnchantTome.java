package vazkii.quark.content.tools.loot;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentLevelEntry;
import net.minecraft.item.EnchantedBookItem;
import net.minecraft.item.ItemStack;
import net.minecraft.loot.condition.LootCondition;
import net.minecraft.loot.context.LootContext;
import net.minecraft.loot.function.ConditionalLootFunction;
import net.minecraft.loot.function.LootFunctionType;
import vazkii.quark.content.tools.module.AncientTomesModule;

import static vazkii.quark.content.tools.module.AncientTomesModule.validEnchants;

import javax.annotation.Nonnull;

/**
 * @author WireSegal
 * Created at 1:48 PM on 7/4/20.
 */
public class EnchantTome extends ConditionalLootFunction {
    public EnchantTome(LootCondition[] conditions) {
        super(conditions);
    }

    @Override
    @Nonnull
    public LootFunctionType getType() {
        return AncientTomesModule.tomeEnchantType;
    }

    @Override
    @Nonnull
    public ItemStack process(@Nonnull ItemStack stack, LootContext context) {
        Enchantment enchantment = validEnchants.get(context.getRandom().nextInt(validEnchants.size()));
        EnchantedBookItem.addEnchantment(stack, new EnchantmentLevelEntry(enchantment, enchantment.getMaxLevel()));
        return stack;
    }

    public static class Serializer extends ConditionalLootFunction.Serializer<EnchantTome> {
        @Override
        @Nonnull
        public EnchantTome fromJson(@Nonnull JsonObject object, @Nonnull JsonDeserializationContext deserializationContext, @Nonnull LootCondition[] conditionsIn) {
            return new EnchantTome(conditionsIn);
        }
    }
}
