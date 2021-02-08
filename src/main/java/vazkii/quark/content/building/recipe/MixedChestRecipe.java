package vazkii.quark.content.building.recipe;

import com.google.gson.JsonObject;

import net.minecraft.block.Blocks;
import net.minecraft.inventory.CraftingInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.ICraftingRecipe;
import net.minecraft.item.crafting.IRecipeSerializer;
import net.minecraft.network.PacketBuffer;
import net.minecraft.tags.ItemTags;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import net.minecraftforge.registries.ForgeRegistryEntry;

public class MixedChestRecipe implements ICraftingRecipe {
	
    public static final Serializer SERIALIZER = new Serializer();

	final ResourceLocation res;
	
	public MixedChestRecipe(ResourceLocation res) {
		this.res = res;
	}
	
	@Override
	public boolean canFit(int x, int y) {
		return x == 3 && y == 3;
	}

	@Override
	public ItemStack getCraftingResult(CraftingInventory arg0) {
		return new ItemStack(Blocks.CHEST);
	}

	@Override
	public ResourceLocation getId() {
		return res;
	}

	@Override
	public ItemStack getRecipeOutput() {
		return new ItemStack(Blocks.CHEST);	
	}

	@Override
	public IRecipeSerializer<?> getSerializer() {
		return SERIALIZER;
	}

	@Override
	public boolean matches(CraftingInventory inv, World world) {
		if(inv.getStackInSlot(4).isEmpty()) {
			ItemStack first = null;
			boolean foundDifference = false;
			
			for(int i = 0; i < 9; i++)
				if(i != 4) { // ignore center
					ItemStack stack = inv.getStackInSlot(i);
					if(!stack.isEmpty() && stack.getItem().isIn(ItemTags.PLANKS)) {
						if(first == null)
							first = stack;
						else if(!ItemStack.areItemsEqual(first, stack))
							foundDifference = true;
					} else return false;
				}
			
			return foundDifference;
		}
		
		
		return false;
	}
	
	private static class Serializer extends ForgeRegistryEntry<IRecipeSerializer<?>> implements IRecipeSerializer<MixedChestRecipe> {
		
        public Serializer() {
            setRegistryName("quark:mixed_chest");
        }

		@Override
		public MixedChestRecipe read(ResourceLocation arg0, JsonObject arg1) {
			return new MixedChestRecipe(arg0);
		}

		@Override
		public MixedChestRecipe read(ResourceLocation arg0, PacketBuffer arg1) {
			return new MixedChestRecipe(arg0);
		}

		@Override
		public void write(PacketBuffer arg0, MixedChestRecipe arg1) {
			// NO-OP
		}
		
	}

}
