package vazkii.quark.content.tweaks.recipe;

import javax.annotation.Nonnull;

import net.minecraft.inventory.CraftingInventory;
import net.minecraft.item.ElytraItem;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.item.crafting.IRecipeSerializer;
import net.minecraft.item.crafting.Ingredient;
import net.minecraft.item.crafting.SpecialRecipe;
import net.minecraft.item.crafting.SpecialRecipeSerializer;
import net.minecraft.util.NonNullList;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import vazkii.quark.content.tweaks.module.DragonScalesModule;

public class ElytraDuplicationRecipe extends SpecialRecipe {

    public static final SpecialRecipeSerializer<?> SERIALIZER = new SpecialRecipeSerializer<>(ElytraDuplicationRecipe::new);

	public ElytraDuplicationRecipe(ResourceLocation id) {
		super(id);
	}
	
	@Override
	public boolean matches(@Nonnull CraftingInventory var1, @Nonnull World var2) {
		int sources = 0;
		boolean foundTarget = false;

		for(int i = 0; i < var1.getSizeInventory(); i++) {
			ItemStack stack = var1.getStackInSlot(i);
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
		return getRecipeOutput();
	}

	@Nonnull
	@Override
	public ItemStack getRecipeOutput() {
		ItemStack stack = new ItemStack(Items.ELYTRA);
//		if(EnderdragonScales.dyeBlack && ModuleLoader.isFeatureEnabled(DyableElytra.class)) 
//			ItemNBTHelper.setInt(stack, DyableElytra.TAG_ELYTRA_DYE, 0);
		
		return stack;
	}
	
	@Nonnull
	@Override
	public NonNullList<ItemStack> getRemainingItems(CraftingInventory inv) {
		NonNullList<ItemStack> ret = NonNullList.withSize(inv.getSizeInventory(), ItemStack.EMPTY);
		
		for(int i = 0; i < inv.getSizeInventory(); i++) {
			ItemStack stack = inv.getStackInSlot(i);
			if(stack.getItem() == Items.ELYTRA)
				ret.set(i, stack.copy());
		}
		
		return ret;
	}

	@Override
	public boolean isDynamic() {
		return true;
	}
	
	@Override
	public boolean canFit(int width, int height) {
		return (width * height) >= 2;
	}
	
	@Override
	@Nonnull
	public NonNullList<Ingredient> getIngredients() {
		NonNullList<Ingredient> list = NonNullList.withSize(2, Ingredient.EMPTY);
		list.set(0, Ingredient.fromStacks(new ItemStack(Items.ELYTRA)));
		list.set(1, Ingredient.fromStacks(new ItemStack(DragonScalesModule.dragon_scale)));
		return list;
	}

	@Override
	public IRecipeSerializer<?> getSerializer() {
		return SERIALIZER;
	}

}
