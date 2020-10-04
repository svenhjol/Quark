package vazkii.quark.tools.item;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.RayTraceContext.FluidHandling;
import net.minecraft.world.RayTraceContext.ShapeType;
import net.minecraft.world.World;
import vazkii.quark.base.handler.RayTraceHandler;
import vazkii.quark.base.item.QuarkItem;
import vazkii.quark.base.module.Module;
import vazkii.quark.tools.module.BottledCloudModule;

public class BottledCloudItem extends QuarkItem {

	public BottledCloudItem(Module module) {
		super("bottled_cloud", module, new Item.Settings().group(ItemGroup.TOOLS));
	}
	
	@Override
	public TypedActionResult<ItemStack> use(World world, PlayerEntity player, Hand hand) {
		ItemStack stack = player.getStackInHand(hand);
		
		HitResult result = RayTraceHandler.rayTrace(player, world, player, ShapeType.OUTLINE, FluidHandling.ANY);
		if(result instanceof BlockHitResult) {
			BlockHitResult bresult = (BlockHitResult) result;
			BlockPos pos = bresult.getBlockPos();
			if(!world.isAir(pos))
				pos = pos.offset(bresult.getSide());
			
			if(world.isAir(pos)) {
				if(!world.isClient)
					world.setBlockState(pos, BottledCloudModule.cloud.getDefaultState());
				
				stack.decrement(1);
				
				if(!player.isCreative()) {
					ItemStack returnStack = new ItemStack(Items.GLASS_BOTTLE);
					if(stack.isEmpty())
						stack = returnStack;
					else if(!player.giveItemStack(returnStack))
						player.dropItem(returnStack, false);
				}
				
				player.getItemCooldownManager().set(this, 10);
				return new TypedActionResult<ItemStack>(ActionResult.SUCCESS, stack);
			}
		}
		
		return new TypedActionResult<ItemStack>(ActionResult.PASS, stack);
	}

}
