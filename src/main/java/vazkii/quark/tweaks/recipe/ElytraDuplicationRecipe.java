package vazkii.quark.tweaks.recipe;

import javax.annotation.Nonnull;

import com.google.gson.JsonObject;

import net.minecraft.inventory.CraftingInventory;
import net.minecraft.item.ElytraItem;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.recipe.CraftingRecipe;
import net.minecraft.recipe.Ingredient;
import net.minecraft.recipe.RecipeSerializer;
import net.minecraft.util.Identifier;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.world.World;
import net.minecraftforge.registries.ForgeRegistryEntry;
import vazkii.quark.tweaks.module.DragonScalesModule;

public class ElytraDuplicationRecipe implements CraftingRecipe {

    public static final Serializer SERIALIZER = new Serializer();
	
	@Override
	public boolean matches(@Nonnull CraftingInventory var1, @Nonnull World var2) {
		int sources = 0;
		boolean foundTarget = false;

		for(int i = 0; i < var1.size(); i++) {
			ItemStack stack = var1.getStack(i);
			if(!stack.isEmpty()) {
				if(stack.getItem() instanceof ElytraItem) {
					if(foundTarget)
						return false;
					foundTarget = true;
				} else if(stack.getItem() == DragonScalesModule.dragon_scale) {
					if(sources >= 1)
						return false;
					sources++;
				} else return false;
			}
		}

		return sources == 1 && foundTarget;
	}

	@Nonnull
	@Override
	public ItemStack getCraftingResult(@Nonnull CraftingInventory var1) {
		return getOutput();
	}

	@Nonnull
	@Override
	public ItemStack getOutput() {
		ItemStack stack = new ItemStack(Items.ELYTRA);
//		if(EnderdragonScales.dyeBlack && ModuleLoader.isFeatureEnabled(DyableElytra.class)) 
//			ItemNBTHelper.setInt(stack, DyableElytra.TAG_ELYTRA_DYE, 0);
		
		return stack;
	}
	
	@Nonnull
	@Override
	public DefaultedList<ItemStack> getRemainingItems(CraftingInventory inv) {
		DefaultedList<ItemStack> ret = DefaultedList.ofSize(inv.size(), ItemStack.EMPTY);
		
		for(int i = 0; i < inv.size(); i++) {
			ItemStack stack = inv.getStack(i);
			if(stack.getItem() == Items.ELYTRA)
				ret.set(i, stack.copy());
		}
		
		return ret;
	}

	@Override
	public boolean isIgnoredInRecipeBook() {
		return true;
	}
	
	@Override
	public boolean fits(int width, int height) {
		return (width * height) >= 2;
	}
	
	@Override
	@Nonnull
	public DefaultedList<Ingredient> getPreviewInputs() {
		DefaultedList<Ingredient> list = DefaultedList.ofSize(2, Ingredient.EMPTY);
		list.set(0, Ingredient.ofStacks(new ItemStack(Items.ELYTRA)));
		list.set(1, Ingredient.ofStacks(new ItemStack(DragonScalesModule.dragon_scale)));
		return list;
	}
	
	
	@Override
	public Identifier getId() {
		return SERIALIZER.getRegistryName();
	}

	@Override
	public RecipeSerializer<?> getSerializer() {
		return SERIALIZER;
	}
	
    public static class Serializer extends ForgeRegistryEntry<RecipeSerializer<?>> implements RecipeSerializer<ElytraDuplicationRecipe> {

        public Serializer() {
            setRegistryName("quark:elytra_duplication");
        }
    	
		@Override
		public ElytraDuplicationRecipe read(Identifier recipeId, JsonObject json) {
			return new ElytraDuplicationRecipe();
		}

		@Override
		public ElytraDuplicationRecipe read(Identifier recipeId, PacketByteBuf buffer) {
			return new ElytraDuplicationRecipe();
		}

		@Override
		public void write(PacketByteBuf buffer, ElytraDuplicationRecipe recipe) {
			// NO-OP
		}
    	
    }

}
