package vazkii.quark.base.util;

import net.minecraft.item.crafting.Ingredient;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionBrewing;
import net.minecraftforge.fml.common.ObfuscationReflectionHelper;
import net.minecraftforge.registries.ForgeRegistryEntry;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.util.List;

/**
 * @author WireSegal
 * Created at 4:12 PM on 10/20/19.
 */
@SuppressWarnings("unchecked")
public class PotionReflection {
    private static final MethodHandle CREATE_MIX_PREDICATE, GET_POTION_TYPE_CONVERSIONS;
    
    /**
     * TO ANY ONLOOKERS, READ THIS
     * 
     * This class is incredibly cursed, but it's also essential, as ATs/Accessors can't be used to reproduce this
     * behavior, due to the fact the classes referenced here are touched by Forge.
     */
    
    static {
        try {
            Class<?> mixPredicate = Class.forName("net.minecraft.potion.PotionBrewing$MixPredicate");
            MethodType ctorType = MethodType.methodType(Void.TYPE, ForgeRegistryEntry.class, Ingredient.class, ForgeRegistryEntry.class);
            Constructor<?> ctor = mixPredicate.getConstructor(ctorType.parameterArray());
            ctor.setAccessible(true);
            CREATE_MIX_PREDICATE = MethodHandles.lookup().unreflectConstructor(ctor)
                    .asType(ctorType.changeReturnType(Object.class));

            Field typeConversions = ObfuscationReflectionHelper.findField(PotionBrewing.class, "field_185213_a"); // POTION_TYPE_CONVERSIONS
            GET_POTION_TYPE_CONVERSIONS = MethodHandles.lookup().unreflectGetter(typeConversions)
                    .asType(MethodType.methodType(List.class));
        } catch (IllegalAccessException | ClassNotFoundException | NoSuchMethodException e) {
            throw new RuntimeException(e);
        }
    }

    @SuppressWarnings("unchecked")
    public static void addBrewingRecipe(Potion input, Ingredient reagent, Potion output) {
        try {
            Object mixPredicate = CREATE_MIX_PREDICATE.invokeExact((ForgeRegistryEntry) input, reagent, (ForgeRegistryEntry) output);
            List<Object> typeConversions = (List) GET_POTION_TYPE_CONVERSIONS.invokeExact();
            typeConversions.add(mixPredicate);
        } catch (Throwable throwable) {
            throw new RuntimeException(throwable);
        }
    }
}
