package vazkii.quark.base.recipe;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import net.minecraft.inventory.CraftingInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.*;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.JSONUtils;
import net.minecraft.util.NonNullList;
import net.minecraft.util.ResourceLocation;
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
public class ExclusionRecipe implements ICraftingRecipe {
    public static final Serializer SERIALIZER = new Serializer();

    private final ICraftingRecipe parent;
    private final List<ResourceLocation> excluded;

    public ExclusionRecipe(ICraftingRecipe parent, List<ResourceLocation> excluded) {
        this.parent = parent;
        this.excluded = excluded;
    }

    @Override
    public boolean matches(@Nonnull CraftingInventory inv, @Nonnull World worldIn) {
        for (ResourceLocation recipeLoc : excluded) {
            Optional<? extends IRecipe<?>> recipeHolder = worldIn.getRecipeManager().getRecipe(recipeLoc);
            if (recipeHolder.isPresent()) {
                IRecipe<?> recipe = recipeHolder.get();
                if (recipe instanceof ICraftingRecipe &&
                        ((ICraftingRecipe) recipe).matches(inv, worldIn)) {
                    return false;
                }
            }
        }

        return parent.matches(inv, worldIn);
    }

    @Nonnull
    @Override
    public ItemStack getCraftingResult(@Nonnull CraftingInventory inv) {
        return parent.getCraftingResult(inv);
    }

    @Override
    public boolean canFit(int width, int height) {
        return parent.canFit(width, height);
    }

    @Nonnull
    @Override
    public ItemStack getRecipeOutput() {
        return parent.getRecipeOutput();
    }

    @Nonnull
    @Override
    public ResourceLocation getId() {
        return parent.getId();
    }

    @Nonnull
    @Override
    public IRecipeSerializer<?> getSerializer() {
        return SERIALIZER;
    }

    @Nonnull
    @Override
    public IRecipeType<?> getType() {
        return parent.getType();
    }

    @Nonnull
    @Override
    public NonNullList<ItemStack> getRemainingItems(CraftingInventory inv) {
        return parent.getRemainingItems(inv);
    }

    @Nonnull
    @Override
    public NonNullList<Ingredient> getIngredients() {
        return parent.getIngredients();
    }

    @Override
    public boolean isDynamic() {
        return parent.isDynamic();
    }

    @Nonnull
    @Override
    public String getGroup() {
        return parent.getGroup();
    }

    @Nonnull
    @Override
    public ItemStack getIcon() {
        return parent.getIcon();
    }

    private static class ShapedExclusionRecipe extends ExclusionRecipe implements IShapedRecipe<CraftingInventory> {
        private final IShapedRecipe<CraftingInventory> parent;

        public ShapedExclusionRecipe(ICraftingRecipe parent, List<ResourceLocation> excluded) {
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

    public static class Serializer extends ForgeRegistryEntry<IRecipeSerializer<?>> implements IRecipeSerializer<ExclusionRecipe> {
       
    	public Serializer() {
            setRegistryName("quark:exclusion");
        }

        @Nonnull
        @Override
        public ExclusionRecipe read(@Nonnull ResourceLocation recipeId, @Nonnull JsonObject json) {
            String trueType = JSONUtils.getString(json, "true_type");
            if (trueType.equals("quark:exclusion"))
                throw new JsonSyntaxException("Recipe type circularity");

            JsonArray excluded = JSONUtils.getJsonArray(json, "exclusions");
            List<ResourceLocation> excludedRecipes = new ArrayList<>();
            for (JsonElement el : excluded) {
                ResourceLocation loc = new ResourceLocation(el.getAsString());
                if (!loc.equals(recipeId))
                    excludedRecipes.add(loc);
            }

            Optional<IRecipeSerializer<?>> serializer = Registry.RECIPE_SERIALIZER.getOptional(new ResourceLocation(trueType));
            if (!serializer.isPresent())
                throw new JsonSyntaxException("Invalid or unsupported recipe type '" + trueType + "'");
            IRecipe<?> parent = serializer.get().read(recipeId, json);
            if (!(parent instanceof ICraftingRecipe))
                throw new JsonSyntaxException("Type '" + trueType + "' is not a crafting recipe");

            if (parent instanceof IShapedRecipe)
                return new ShapedExclusionRecipe((ICraftingRecipe) parent, excludedRecipes);
            return new ExclusionRecipe((ICraftingRecipe) parent, excludedRecipes);
        }

        @Nonnull
        @Override
        public ExclusionRecipe read(@Nonnull ResourceLocation recipeId, @Nonnull PacketBuffer buffer) {
            int exclusions = buffer.readVarInt();
            List<ResourceLocation> excludedRecipes = new ArrayList<>();
            for (int i = 0; i < exclusions; i++) {
                ResourceLocation loc = new ResourceLocation(buffer.readString(32767));
                if (!loc.equals(recipeId))
                    excludedRecipes.add(loc);
            }
            String trueType = buffer.readString(32767);

            IRecipeSerializer<?> serializer = Registry.RECIPE_SERIALIZER.getOptional(new ResourceLocation(trueType))
                    .orElseThrow(() -> new IllegalArgumentException("Invalid or unsupported recipe type '" + trueType + "'"));
            IRecipe<?> parent = serializer.read(recipeId, buffer);
            if (!(parent instanceof ICraftingRecipe))
                throw new IllegalArgumentException("Type '" + trueType + "' is not a crafting recipe");

            if (parent instanceof IShapedRecipe)
                return new ShapedExclusionRecipe((ICraftingRecipe) parent, excludedRecipes);
            return new ExclusionRecipe((ICraftingRecipe) parent, excludedRecipes);
        }

        @Override
        @SuppressWarnings("unchecked")
        public void write(@Nonnull PacketBuffer buffer, @Nonnull ExclusionRecipe recipe) {
            buffer.writeVarInt(recipe.excluded.size());
            for (ResourceLocation loc : recipe.excluded)
                buffer.writeString(loc.toString(), 32767);
            buffer.writeString(Objects.toString(recipe.parent.getSerializer().getRegistryName()), 32767);
            ((IRecipeSerializer<IRecipe<?>>) recipe.parent.getSerializer()).write(buffer, recipe.parent);
        }
    }
}
