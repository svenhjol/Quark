package vazkii.quark.base.handler;

import com.google.common.collect.Lists;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.item.crafting.Ingredient;
import net.minecraft.potion.*;
import net.minecraft.util.ResourceLocation;
import org.apache.commons.lang3.tuple.ImmutableTriple;
import org.apache.commons.lang3.tuple.Triple;
import vazkii.arl.util.RegistryHelper;
import vazkii.quark.base.Quark;
import vazkii.quark.base.recipe.FlagIngredient;
import vazkii.quark.base.util.PotionReflection;

import javax.annotation.Nullable;
import java.util.List;
import java.util.function.Supplier;

/**
 * @author WireSegal
 * Created at 3:34 PM on 9/23/19.
 */
public class BrewingHandler {


    public static void addPotionMix(String flag, Supplier<Ingredient> reagent, Effect effect) {
        addPotionMix(flag, reagent, effect, null);
    }

    public static void addPotionMix(String flag, Supplier<Ingredient> reagent, Effect effect,
                                                     int normalTime, int longTime, int strongTime) {
        addPotionMix(flag, reagent, effect, null, normalTime, longTime, strongTime);
    }

    public static void addPotionMix(String flag, Supplier<Ingredient> reagent, Effect effect,
                                                     @Nullable Effect negation) {
        addPotionMix(flag, reagent, effect, negation, 3600, 9600, 1800);
    }


    public static void addPotionMix(String flag, Supplier<Ingredient> reagent, Effect effect,
                                                     @Nullable Effect negation, int normalTime, int longTime, int strongTime) {
        ResourceLocation loc = effect.getRegistryName();
        if (loc != null) {
            String baseName = loc.getPath();
            boolean hasStrong = strongTime > 0;

            Potion normalType = addPotion(new EffectInstance(effect, normalTime), baseName, baseName);
            Potion longType = addPotion(new EffectInstance(effect, longTime), baseName, "long_" + baseName);
            Potion strongType = !hasStrong ? null : addPotion(new EffectInstance(effect, strongTime, 1), baseName, "strong_" + baseName);

            addPotionMix(flag, reagent, normalType, longType, strongType);

            if (negation != null) {
                ResourceLocation negationLoc = negation.getRegistryName();
                if (negationLoc != null) {
                    String negationBaseName = negationLoc.getPath();

                    Potion normalNegationType = addPotion(new EffectInstance(negation, normalTime), negationBaseName, negationBaseName);
                    Potion longNegationType = addPotion(new EffectInstance(negation, longTime), negationBaseName, "long_" + negationBaseName);
                    Potion strongNegationType = !hasStrong ? null : addPotion(new EffectInstance(negation, strongTime, 1), negationBaseName, "strong_" + negationBaseName);

                    addNegation(flag, normalType, longType, strongType, normalNegationType, longNegationType, strongNegationType);
                }
            }
        }

    }

    public static void addPotionMix(String flag, Supplier<Ingredient> reagent, Potion normalType, Potion longType, @Nullable Potion strongType) {
        boolean hasStrong = strongType != null;

        add(flag, Potions.AWKWARD, reagent, normalType);
        add(flag, Potions.WATER, reagent, Potions.MUNDANE);

        if (hasStrong)
            add(flag, normalType, BrewingHandler::glowstone, strongType);
        add(flag, normalType, BrewingHandler::redstone, longType);
    }

    public static void addNegation(String flag, Potion normalType, Potion longType, @Nullable Potion strongType,
                                   Potion normalNegatedType, Potion longNegatedType, @Nullable Potion strongNegatedType) {
        add(flag, normalType, BrewingHandler::spiderEye, normalNegatedType);

        boolean hasStrong = strongType != null && strongNegatedType != null;

        if (hasStrong) {
            add(flag, strongType, BrewingHandler::spiderEye, strongNegatedType);
            add(flag, normalNegatedType, BrewingHandler::glowstone, strongNegatedType);
        }
        add(flag, longType, BrewingHandler::spiderEye, longNegatedType);
        add(flag, normalNegatedType, BrewingHandler::redstone, longNegatedType);

    }

    public static ItemStack of(Item potionType, Potion potion) {
        ItemStack stack = new ItemStack(potionType);
        PotionUtils.addPotionToItemStack(stack, potion);
        return stack;
    }

    private static boolean isInjectionPrepared = false;
    private static final List<Triple<Potion, Supplier<Ingredient>, Potion>> toRegister = Lists.newArrayList();

    public static void setup() {
        isInjectionPrepared = true;
        for (Triple<Potion, Supplier<Ingredient>, Potion> triple : toRegister)
            PotionReflection.addBrewingRecipe(triple.getLeft(), triple.getMiddle().get(), triple.getRight());

        toRegister.clear();
    }

    private static void add(String flag, Potion potion, Supplier<Ingredient> reagent, Potion to) {
        if (isInjectionPrepared)
            PotionReflection.addBrewingRecipe(potion, new FlagIngredient(reagent.get(), flag), to);
        else
            toRegister.add(new ImmutableTriple<>(potion, () -> new FlagIngredient(reagent.get(), flag), to));
    }

    private static Potion addPotion(EffectInstance eff, String baseName, String name) {
        Potion effect = new Potion(Quark.MOD_ID + "." + baseName, eff);
        RegistryHelper.register(effect, name);

        return effect;
    }

    private static Ingredient redstone() {
        return Ingredient.fromItems(Items.REDSTONE);
    }

    private static Ingredient glowstone() {
        return Ingredient.fromItems(Items.GLOWSTONE_DUST);
    }

    private static Ingredient spiderEye() {
        return Ingredient.fromItems(Items.FERMENTED_SPIDER_EYE);
    }
}
