package vazkii.quark.content.building.recipe;

import com.google.gson.JsonObject;

import net.minecraft.block.Blocks;
import net.minecraft.inventory.CraftingInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.recipe.CraftingRecipe;
import net.minecraft.recipe.RecipeSerializer;
import net.minecraft.tag.ItemTags;
import net.minecraft.util.Identifier;
import net.minecraft.world.World;
import net.minecraftforge.registries.ForgeRegistryEntry;

public class MixedChestRecipe implements CraftingRecipe {
	
    public static final Serializer SERIALIZER = new Serializer();

	final Identifier res;
	
	public MixedChestRecipe(Identifier res) {
		this.res = res;
	}
	
	@Override
	public boolean fits(int x, int y) {
		return x == 3 && y == 3;
	}

	@Override
	public ItemStack getCraftingResult(CraftingInventory arg0) {
		return new ItemStack(Blocks.CHEST);
	}

	@Override
	public Identifier getId() {
		return res;
	}

	@Override
	public ItemStack getOutput() {
		return new ItemStack(Blocks.CHEST);	
	}

	@Override
	public RecipeSerializer<?> getSerializer() {
		return SERIALIZER;
	}

	@Override
	public boolean matches(CraftingInventory inv, World world) {
		if(inv.getStack(4).isEmpty()) {
			ItemStack first = null;
			boolean foundDifference = false;
			
			for(int i = 0; i < 9; i++)
				if(i != 4) { // ignore center
					ItemStack stack = inv.getStack(i);
					if(!stack.isEmpty() && stack.getItem().isIn(ItemTags.PLANKS)) {
						if(first == null)
							first = stack;
						else if(!ItemStack.areItemsEqualIgnoreDamage(first, stack))
							foundDifference = true;
					} else return false;
				}
			
			return foundDifference;
		}
		
		
		return false;
	}
	
	private static class Serializer extends ForgeRegistryEntry<RecipeSerializer<?>> implements RecipeSerializer<MixedChestRecipe> {
		
        public Serializer() {
            setRegistryName("quark:mixed_chest");
        }

		@Override
		public MixedChestRecipe read(Identifier arg0, JsonObject arg1) {
			return new MixedChestRecipe(arg0);
		}

		@Override
		public MixedChestRecipe read(Identifier arg0, PacketByteBuf arg1) {
			return new MixedChestRecipe(arg0);
		}

		@Override
		public void write(PacketByteBuf arg0, MixedChestRecipe arg1) {
			// NO-OP
		}
		
	}

}
