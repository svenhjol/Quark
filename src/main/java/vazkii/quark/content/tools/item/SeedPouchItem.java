package vazkii.quark.content.tools.item;

import java.util.List;
import java.util.function.Predicate;

import org.apache.commons.lang3.tuple.Pair;

import net.minecraft.block.Block;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUseContext;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.text.IFormattableTextComponent;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.Tags;
import vazkii.arl.util.ItemNBTHelper;
import vazkii.quark.api.ITrowelable;
import vazkii.quark.api.IUsageTickerOverride;
import vazkii.quark.base.block.IQuarkBlock;
import vazkii.quark.base.item.IQuarkItem;
import vazkii.quark.base.item.QuarkItem;
import vazkii.quark.base.module.ModuleLoader;
import vazkii.quark.base.module.QuarkModule;
import vazkii.quark.content.tools.module.SeedPouchModule;

public class SeedPouchItem extends QuarkItem implements IUsageTickerOverride, ITrowelable {

	public static final String TAG_STORED_ITEM = "storedItem";
	public static final String TAG_COUNT = "itemCount";

	public SeedPouchItem(QuarkModule module) {
		super("seed_pouch", module, 
				new Item.Properties()
				.maxStackSize(1)
				.maxDamage(SeedPouchModule.maxItems + 1)
				.group(ItemGroup.TOOLS));
	}
	
    @OnlyIn(Dist.CLIENT)
    public static float itemFraction(ItemStack stack, ClientWorld world, LivingEntity entityIn) {
    	if(entityIn instanceof PlayerEntity) {
    		PlayerEntity player = (PlayerEntity) entityIn;
    		ItemStack held = player.inventory.getItemStack();
    		
    		if(canTakeItem(stack, held))
    			return 0F;
    	} 
    	
		Pair<ItemStack, Integer> contents = getContents(stack);
		if(contents == null)
			return 0F;
		
		return (float) contents.getRight() / (float) SeedPouchModule.maxItems;
    }

    public static Pair<ItemStack, Integer> getContents(ItemStack stack) {
		CompoundNBT nbt = ItemNBTHelper.getCompound(stack, TAG_STORED_ITEM, true);
		if(nbt == null)
			return null;

		ItemStack contained = ItemStack.read(nbt);
		int count = ItemNBTHelper.getInt(stack, TAG_COUNT, 0);
		return Pair.of(contained, count);
	}
    
    public static boolean canTakeItem(ItemStack stack, ItemStack incoming) {
		Pair<ItemStack, Integer> contents = getContents(stack);
		
		if(contents == null)
			return incoming.getItem().isIn(Tags.Items.SEEDS);
		
		return contents.getRight() < SeedPouchModule.maxItems && ItemStack.areItemsEqual(incoming, contents.getLeft());
    }

	public static void setItemStack(ItemStack stack, ItemStack target) {
		ItemStack copy = target.copy();
		copy.setCount(1);

		CompoundNBT nbt = new CompoundNBT();
		copy.write(nbt);

		ItemNBTHelper.setCompound(stack, TAG_STORED_ITEM, nbt);
		setCount(stack, target.getCount());
	}
	
	public static void setCount(ItemStack stack, int count) {
		if(count <= 0) {
			stack.getTag().remove(TAG_STORED_ITEM);
			stack.setDamage(0);

			return;
		}
		
		ItemNBTHelper.setInt(stack, TAG_COUNT, count);
		stack.setDamage(SeedPouchModule.maxItems + 1 - count);
	}
	
	@Override
	public int getRGBDurabilityForDisplay(ItemStack stack) {
		return 0x00FF00;
	}
	
	@Override
	public ITextComponent getDisplayName(ItemStack stack) {
		ITextComponent base = super.getDisplayName(stack);
	
		Pair<ItemStack, Integer> contents = getContents(stack);
		if(contents == null)
			return base;
		
		IFormattableTextComponent comp = base.deepCopy();
		comp.append(new StringTextComponent(" ("));
		comp.append(contents.getLeft().getDisplayName());
		comp.append(new StringTextComponent(")"));
		return comp;
}

	@Override
	public ActionResultType onItemUse(ItemUseContext context) {
		ItemStack stack = context.getItem();
		Pair<ItemStack, Integer> contents = getContents(stack);
		if(contents != null) {
			ItemStack target = contents.getLeft().copy();
			int total = contents.getRight();
			
			target.setCount(Math.min(target.getMaxStackSize(), total));
			
			PlayerEntity player = context.getPlayer();
			if(!player.isSneaking())
				return placeSeed(context, target, context.getPos(), total);
			
			else {
				ActionResultType bestRes = ActionResultType.FAIL;
				
				int range = SeedPouchModule.shiftRange;
				int blocks = range * range;
				int shift = -((int) Math.floor(range / 2));
						
				for(int i = 0; i < blocks; i++) {
					int x = shift + i % range;
					int z = shift + i / range;
					
					ActionResultType res = placeSeed(context, target, context.getPos().add(x, 0, z), total);
					contents = getContents(stack);
					if(contents == null)
						break;
					total = contents.getRight();
					
					if(!bestRes.isSuccessOrConsume())
						bestRes = res;
				}
				
				return bestRes;
			}
		}
		
		return super.onItemUse(context);
	}
	
	private ActionResultType placeSeed(ItemUseContext context, ItemStack target, BlockPos pos, int total) {
		ActionResultType res = target.getItem().onItemUse(new PouchItemUseContext(context, target, pos));
		int diff = res == ActionResultType.CONSUME ? 1 : 0;
		if(diff > 0 && !context.getPlayer().isCreative())
			setCount(context.getItem(), total - diff);
		
		return res;
	}
	
	@Override
	public void fillItemGroup(ItemGroup group, NonNullList<ItemStack> items) {
		super.fillItemGroup(group, items);
		
		if(SeedPouchModule.showAllVariantsInCreative && isEnabled() && isInGroup(group)) {
			List<Item> tagItems = null;
			
			try {
				tagItems = SeedPouchModule.seedPouchHoldableTag.getAllElements();
			} catch(IllegalStateException e) { // Tag not bound yet
				return;
			}
			
			for(Item i : tagItems) {
				if(!ModuleLoader.INSTANCE.isItemEnabled(i))
					continue;
				
				ItemStack stack = new ItemStack(this);
				setItemStack(stack, new ItemStack(i));
				setCount(stack, SeedPouchModule.maxItems);
				items.add(stack);
			}
		}
	}

	@Override
	public ItemStack getUsageTickerItem(ItemStack stack) {
		Pair<ItemStack, Integer> contents = getContents(stack);
		if(contents != null)
			return contents.getLeft();
		
		return stack;
	}
	
	@Override
	public int getUsageTickerCountForItem(ItemStack stack, Predicate<ItemStack> target) {
		Pair<ItemStack, Integer> contents = getContents(stack);
		if(contents != null && target.test(contents.getLeft()))
			return contents.getRight();
		
		return 0;
	}
	
	class PouchItemUseContext extends ItemUseContext {

		protected PouchItemUseContext(ItemUseContext parent, ItemStack stack, BlockPos targetPos) {
			super(parent.getWorld(), parent.getPlayer(), parent.getHand(), stack, 
					new BlockRayTraceResult(parent.getHitVec(), parent.getFace(), targetPos, parent.isInside()));
		}

	}

}
