package vazkii.quark.base.recipe;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import net.minecraft.inventory.CraftingInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.*;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.recipe.CraftingRecipe;
import net.minecraft.recipe.Ingredient;
import net.minecraft.recipe.Recipe;
import net.minecraft.recipe.RecipeSerializer;
import net.minecraft.recipe.RecipeType;
import net.minecraft.util.Identifier;
import net.minecraft.util.JsonHelper;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.World;
import net.minecraftforge.common.crafting.IShapedRecipe;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.ForgeRegistryEntry;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * @author WireSegal
 * Created at 2:08 PM on 8/24/19.
 */
public class ExclusionRecipe implements CraftingRecipe {
    public static final Serializer SERIALIZER = new Serializer();

    private final CraftingRecipe parent;
    private final List<Identifier> excluded;

    public ExclusionRecipe(CraftingRecipe parent, List<Identifier> excluded) {
        this.parent = parent;
        this.excluded = excluded;
    }

    @Override
    public boolean matches(@Nonnull CraftingInventory inv, @Nonnull World worldIn) {
        for (Identifier recipeLoc : excluded) {
            Optional<? extends Recipe<?>> recipeHolder = worldIn.getRecipeManager().get(recipeLoc);
            if (recipeHolder.isPresent()) {
                Recipe<?> recipe = recipeHolder.get();
                if (recipe instanceof CraftingRecipe &&
                        ((CraftingRecipe) recipe).matches(inv, worldIn)) {
                    return false;
                }
            }
        }

        return parent.matches(inv, worldIn);
    }

    @Nonnull
    @Override
    public ItemStack getCraftingResult(@Nonnull CraftingInventory inv) {
        return parent.craft(inv);
    }

    @Override
    public boolean fits(int width, int height) {
        return parent.fits(width, height);
    }

    @Nonnull
    @Override
    public ItemStack getOutput() {
        return parent.getOutput();
    }

    @Nonnull
    @Override
    public Identifier getId() {
        return parent.getId();
    }

    @Nonnull
    @Override
    public RecipeSerializer<?> getSerializer() {
        return SERIALIZER;
    }

    @Nonnull
    @Override
    public RecipeType<?> getType() {
        return parent.getType();
    }

    @Nonnull
    @Override
    public DefaultedList<ItemStack> getRemainingItems(CraftingInventory inv) {
        return parent.getRemainingStacks(inv);
    }

    @Nonnull
    @Override
    public DefaultedList<Ingredient> getPreviewInputs() {
        return parent.getPreviewInputs();
    }

    @Override
    public boolean isIgnoredInRecipeBook() {
        return parent.isIgnoredInRecipeBook();
    }

    @Nonnull
    @Override
    public String getGroup() {
        return parent.getGroup();
    }

    @Nonnull
    @Override
    public ItemStack getRecipeKindIcon() {
        return parent.getRecipeKindIcon();
    }

    private static class ShapedExclusionRecipe extends ExclusionRecipe implements IShapedRecipe<CraftingInventory> {
        private final IShapedRecipe<CraftingInventory> parent;

        public ShapedExclusionRecipe(CraftingRecipe parent, List<Identifier> excluded) {
            super(parent, excluded);
            this.parent = (IShapedRecipe<CraftingInventory>) parent;
        }

        @Override
        public int getRecipeWidth() {
            return parent.getRecipeWidth();
        }

        @Override
        public int getRecipeHeight() {
            return parent.getRecipeHeight();
        }
    }

    public static class Serializer extends ForgeRegistryEntry<RecipeSerializer<?>> implements RecipeSerializer<ExclusionRecipe> {
       
    	public Serializer() {
            setRegistryName("quark:exclusion");
        }

        @Nonnull
        @Override
        public ExclusionRecipe read(@Nonnull Identifier recipeId, @Nonnull JsonObject json) {
            String trueType = JsonHelper.getString(json, "true_type");
            if (trueType.equals("quark:exclusion"))
                throw new JsonSyntaxException("Recipe type circularity");

            JsonArray excluded = JsonHelper.getArray(json, "exclusions");
            List<Identifier> excludedRecipes = new ArrayList<>();
            for (JsonElement el : excluded) {
                Identifier loc = new Identifier(el.getAsString());
                if (!loc.equals(recipeId))
                    excludedRecipes.add(loc);
            }

            Optional<RecipeSerializer<?>> serializer = Registry.RECIPE_SERIALIZER.getOrEmpty(new Identifier(trueType));
            if (!serializer.isPresent())
                throw new JsonSyntaxException("Invalid or unsupported recipe type '" + trueType + "'");
            Recipe<?> parent = serializer.get().read(recipeId, json);
            if (!(parent instanceof CraftingRecipe))
                throw new JsonSyntaxException("Type '" + trueType + "' is not a crafting recipe");

            if (parent instanceof IShapedRecipe)
                return new ShapedExclusionRecipe((CraftingRecipe) parent, excludedRecipes);
            return new ExclusionRecipe((CraftingRecipe) parent, excludedRecipes);
        }

        @Nonnull
        @Override
        public ExclusionRecipe read(@Nonnull Identifier recipeId, @Nonnull PacketByteBuf buffer) {
            int exclusions = buffer.readVarInt();
            List<Identifier> excludedRecipes = new ArrayList<>();
            for (int i = 0; i < exclusions; i++) {
                Identifier loc = new Identifier(buffer.readString(32767));
                if (!loc.equals(recipeId))
                    excludedRecipes.add(loc);
            }
            String trueType = buffer.readString(32767);

            RecipeSerializer<?> serializer = Registry.RECIPE_SERIALIZER.getOrEmpty(new Identifier(trueType))
                    .orElseThrow(() -> new IllegalArgumentException("Invalid or unsupported recipe type '" + trueType + "'"));
            Recipe<?> parent = serializer.read(recipeId, buffer);
            if (!(parent instanceof CraftingRecipe))
                throw new IllegalArgumentException("Type '" + trueType + "' is not a crafting recipe");

            if (parent instanceof IShapedRecipe)
                return new ShapedExclusionRecipe((CraftingRecipe) parent, excludedRecipes);
            return new ExclusionRecipe((CraftingRecipe) parent, excludedRecipes);
        }

        @Override
        @SuppressWarnings("unchecked")
        public void write(@Nonnull PacketByteBuf buffer, @Nonnull ExclusionRecipe recipe) {
            buffer.writeVarInt(recipe.excluded.size());
            for (Identifier loc : recipe.excluded)
                buffer.writeString(loc.toString(), 32767);
            buffer.writeString(Objects.toString(recipe.parent.getSerializer().getRegistryName()), 32767);
            ((RecipeSerializer<Recipe<?>>) recipe.parent.getSerializer()).write(buffer, recipe.parent);
        }
    }
}
