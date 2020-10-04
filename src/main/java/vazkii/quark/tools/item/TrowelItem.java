package vazkii.quark.tools.item;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import javax.annotation.Nonnull;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUsageContext;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import vazkii.arl.util.ItemNBTHelper;
import vazkii.quark.base.handler.MiscUtil;
import vazkii.quark.base.item.QuarkItem;
import vazkii.quark.base.module.Module;
import vazkii.quark.tools.item.TrowelItem.TrowelBlockItemUseContext;
import vazkii.quark.tools.module.TrowelModule;

public class TrowelItem extends QuarkItem {

	private static final String TAG_PLACING_SEED = "placing_seed";
	private static final String TAG_LAST_STACK = "last_stack";
	
	public TrowelItem(Module module) {
		super("trowel", module, new Item.Settings()
				.maxDamage(255)
				.group(ItemGroup.TOOLS));
	}
	
	@Nonnull
	@Override
	public ActionResult useOnBlock(ItemUsageContext context) {
		PlayerEntity player = context.getPlayer();
		Hand hand = context.getHand();
		
		List<ItemStack> targets = new ArrayList<>();
		for(int i = 0; i < PlayerInventory.getHotbarSize(); i++) {
			ItemStack stack = player.inventory.getStack(i);
			if(!stack.isEmpty() && stack.getItem() instanceof BlockItem)
				targets.add(stack);
		}
		
		ItemStack ourStack = player.getStackInHand(hand);
		if(targets.isEmpty())
			return ActionResult.PASS;

		long seed = ItemNBTHelper.getLong(ourStack, TAG_PLACING_SEED, 0);
		Random rand = new Random(seed);
		ItemNBTHelper.setLong(ourStack, TAG_PLACING_SEED, rand.nextLong());
		
		ItemStack target = targets.get(rand.nextInt(targets.size()));
		int count = target.getCount();
		ActionResult result = placeBlock(target, context);
		if(player.isCreative())
			target.setCount(count);
		
		if(result == ActionResult.SUCCESS) {
			CompoundTag cmp = target.serializeNBT();
			ItemNBTHelper.setCompound(ourStack, TAG_LAST_STACK, cmp);
			
			MiscUtil.damageStack(player, hand, context.getStack(), 1);
		}
		
		return result;
	}
	
	private ActionResult placeBlock(ItemStack itemstack, ItemUsageContext context) {
		if(itemstack.getItem() instanceof BlockItem) {
			BlockItem item = (BlockItem) itemstack.getItem();
			ItemPlacementContext newContext = new TrowelBlockItemUseContext(context, itemstack);
			return item.place(newContext);
		}

		return ActionResult.PASS;
	}

	public static ItemStack getLastStack(ItemStack stack) {
		CompoundTag cmp = ItemNBTHelper.getCompound(stack, TAG_LAST_STACK, false);
		return ItemStack.fromTag(cmp);
	}
	
	@Override
	public int getMaxDamage(ItemStack stack) {
		return TrowelModule.maxDamage;
	}
	
	class TrowelBlockItemUseContext extends ItemPlacementContext {

		public TrowelBlockItemUseContext(ItemUsageContext context, ItemStack stack) {
			super(context.getWorld(), context.getPlayer(), context.getHand(), stack, 
					new BlockHitResult(context.getHitPos(), context.getSide(), context.getBlockPos(), context.hitsInsideBlock()));
		}
		
	}

}
