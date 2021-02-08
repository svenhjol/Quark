package vazkii.quark.content.tools.capability;

import org.apache.commons.lang3.tuple.Pair;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraftforge.common.Tags;
import vazkii.arl.util.AbstractDropIn;
import vazkii.quark.content.tools.item.SeedPouchItem;
import vazkii.quark.content.tools.module.SeedPouchModule;

public class SeedPouchDropIn extends AbstractDropIn {

	@Override
	public boolean canDropItemIn(PlayerEntity player, ItemStack stack, ItemStack incoming) {
		return SeedPouchItem.canTakeItem(stack, incoming);
	}

	@Override
	public ItemStack dropItemIn(PlayerEntity player, ItemStack stack, ItemStack incoming) {
		Pair<ItemStack, Integer> contents = SeedPouchItem.getContents(stack);

		if(contents == null) {
			SeedPouchItem.setItemStack(stack, incoming);
			incoming.setCount(0);
		} else {
			int curr = contents.getRight();
			int missing = SeedPouchModule.maxItems - curr;
			int incCount = incoming.getCount();
			int toDrop = Math.min(incCount, missing);
			
			SeedPouchItem.setCount(stack, curr + toDrop);
			incoming.setCount(incCount - toDrop);
		}
		
		return stack;
	}

}
