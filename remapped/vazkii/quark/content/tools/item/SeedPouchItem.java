package vazkii.quark.content.tools.item;

import java.util.List;
import java.util.function.Predicate;

import org.apache.commons.lang3.tuple.Pair;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.block.Block;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUsageContext;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.text.LiteralText;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
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
import vazkii.quark.content.tools.item.SeedPouchItem.PouchItemUseContext;
import vazkii.quark.content.tools.module.SeedPouchModule;

public class SeedPouchItem extends QuarkItem implements IUsageTickerOverride, ITrowelable {

	public static final String TAG_STORED_ITEM = "storedItem";
	public static final String TAG_COUNT = "itemCount";

	public SeedPouchItem(QuarkModule module) {
		super("seed_pouch", module, 
				new Item.Settings()
				.maxCount(1)
				.maxDamage(SeedPouchModule.maxItems + 1)
				.group(ItemGroup.TOOLS));
	}
	
    @Environment(EnvType.CLIENT)
    public static float itemFraction(ItemStack stack, ClientWorld world, LivingEntity entityIn) {
    	if(entityIn instanceof PlayerEntity) {
    		PlayerEntity player = (PlayerEntity) entityIn;
    		ItemStack held = player.inventory.getCursorStack();
    		
    		if(canTakeItem(stack, held))
    			return 0F;
    	} 
    	
		Pair<ItemStack, Integer> contents = getContents(stack);
		if(contents == null)
			return 0F;
		
		return (float) contents.getRight() / (float) SeedPouchModule.maxItems;
    }

    public static Pair<ItemStack, Integer> getContents(ItemStack stack) {
		CompoundTag nbt = ItemNBTHelper.getCompound(stack, TAG_STORED_ITEM, true);
		if(nbt == null)
			return null;

		ItemStack contained = ItemStack.fromTag(nbt);
		int count = ItemNBTHelper.getInt(stack, TAG_COUNT, 0);
		return Pair.of(contained, count);
	}
    
    public static boolean canTakeItem(ItemStack stack, ItemStack incoming) {
		Pair<ItemStack, Integer> contents = getContents(stack);
		
		if(contents == null)
			return incoming.getItem().isIn(Tags.Items.SEEDS);
		
		return contents.getRight() < SeedPouchModule.maxItems && ItemStack.areItemsEqualIgnoreDamage(incoming, contents.getLeft());
    }

	public static void setItemStack(ItemStack stack, ItemStack target) {
		ItemStack copy = target.copy();
		copy.setCount(1);

		CompoundTag nbt = new CompoundTag();
		copy.toTag(nbt);

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
	public Text getName(ItemStack stack) {
		Text base = super.getName(stack);
	
		Pair<ItemStack, Integer> contents = getContents(stack);
		if(contents == null)
			return base;
		
		MutableText comp = base.shallowCopy();
		comp.append(new LiteralText(" ("));
		comp.append(contents.getLeft().getName());
		comp.append(new LiteralText(")"));
		return comp;
}

	@Override
	public ActionResult useOnBlock(ItemUsageContext context) {
		ItemStack stack = context.getStack();
		Pair<ItemStack, Integer> contents = getContents(stack);
		if(contents != null) {
			ItemStack target = contents.getLeft().copy();
			int total = contents.getRight();
			
			target.setCount(Math.min(target.getMaxCount(), total));
			
			PlayerEntity player = context.getPlayer();
			if(!player.isSneaking())
				return placeSeed(context, target, context.getBlockPos(), total);
			
			else {
				ActionResult bestRes = ActionResult.FAIL;
				
				int range = SeedPouchModule.shiftRange;
				int blocks = range * range;
				int shift = -((int) Math.floor(range / 2));
						
				for(int i = 0; i < blocks; i++) {
					int x = shift + i % range;
					int z = shift + i / range;
					
					ActionResult res = placeSeed(context, target, context.getBlockPos().add(x, 0, z), total);
					contents = getContents(stack);
					if(contents == null)
						break;
					total = contents.getRight();
					
					if(!bestRes.isAccepted())
						bestRes = res;
				}
				
				return bestRes;
			}
		}
		
		return super.useOnBlock(context);
	}
	
	private ActionResult placeSeed(ItemUsageContext context, ItemStack target, BlockPos pos, int total) {
		ActionResult res = target.getItem().useOnBlock(new PouchItemUseContext(context, target, pos));
		int diff = res == ActionResult.CONSUME ? 1 : 0;
		if(diff > 0 && !context.getPlayer().isCreative())
			setCount(context.getStack(), total - diff);
		
		return res;
	}
	
	@Override
	public void appendStacks(ItemGroup group, DefaultedList<ItemStack> items) {
		super.appendStacks(group, items);
		
		if(SeedPouchModule.showAllVariantsInCreative && isEnabled() && isIn(group)) {
			List<Item> tagItems = null;
			
			try {
				tagItems = SeedPouchModule.seedPouchHoldableTag.values();
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
	
	class PouchItemUseContext extends ItemUsageContext {

		protected PouchItemUseContext(ItemUsageContext parent, ItemStack stack, BlockPos targetPos) {
			super(parent.getWorld(), parent.getPlayer(), parent.getHand(), stack, 
					new BlockHitResult(parent.getHitPos(), parent.getSide(), targetPos, parent.hitsInsideBlock()));
		}

	}

}
