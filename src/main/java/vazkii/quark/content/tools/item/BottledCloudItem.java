package vazkii.quark.content.tools.item;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.util.ActionResult;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.RayTraceContext.BlockMode;
import net.minecraft.util.math.RayTraceContext.FluidMode;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.world.World;
import vazkii.quark.base.handler.RayTraceHandler;
import vazkii.quark.base.item.QuarkItem;
import vazkii.quark.base.module.QuarkModule;
import vazkii.quark.content.tools.module.BottledCloudModule;

public class BottledCloudItem extends QuarkItem {

	public BottledCloudItem(QuarkModule module) {
		super("bottled_cloud", module, new Item.Properties().group(ItemGroup.TOOLS));
	}
	
	@Override
	public ActionResult<ItemStack> onItemRightClick(World world, PlayerEntity player, Hand hand) {
		ItemStack stack = player.getHeldItem(hand);
		
		RayTraceResult result = RayTraceHandler.rayTrace(player, world, player, BlockMode.OUTLINE, FluidMode.ANY);
		if(result instanceof BlockRayTraceResult) {
			BlockRayTraceResult bresult = (BlockRayTraceResult) result;
			BlockPos pos = bresult.getPos();
			if(!world.isAirBlock(pos))
				pos = pos.offset(bresult.getFace());
			
			if(world.isAirBlock(pos)) {
				if(!world.isRemote)
					world.setBlockState(pos, BottledCloudModule.cloud.getDefaultState());
				
				stack.shrink(1);
				
				if(!player.isCreative()) {
					ItemStack returnStack = new ItemStack(Items.GLASS_BOTTLE);
					if(stack.isEmpty())
						stack = returnStack;
					else if(!player.addItemStackToInventory(returnStack))
						player.dropItem(returnStack, false);
				}
				
				player.getCooldownTracker().setCooldown(this, 10);
				return new ActionResult<ItemStack>(ActionResultType.SUCCESS, stack);
			}
		}
		
		return new ActionResult<ItemStack>(ActionResultType.PASS, stack);
	}

}
