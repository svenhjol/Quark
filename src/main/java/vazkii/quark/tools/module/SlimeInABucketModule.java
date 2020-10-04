package vazkii.quark.tools.module;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.item.ModelPredicateProviderRegistry;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.mob.SlimeEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.Hand;
import net.minecraft.util.Identifier;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import vazkii.arl.util.ItemNBTHelper;
import vazkii.quark.base.module.LoadModule;
import vazkii.quark.base.module.Module;
import vazkii.quark.base.module.ModuleCategory;
import vazkii.quark.tools.item.SlimeInABucketItem;

@LoadModule(category = ModuleCategory.TOOLS, hasSubscriptions = true)
public class SlimeInABucketModule extends Module {

	public static Item slime_in_a_bucket;
	
	@Override
	public void construct() {
		slime_in_a_bucket = new SlimeInABucketItem(this);
	}
	
	@Override
	@Environment(EnvType.CLIENT)
	public void clientSetup() {
		ModelPredicateProviderRegistry.register(slime_in_a_bucket, new Identifier("excited"), 
				(stack, world, e) -> ItemNBTHelper.getBoolean(stack, SlimeInABucketItem.TAG_EXCITED, false) ? 1 : 0);
	}
	
	@SubscribeEvent
	public void entityInteract(PlayerInteractEvent.EntityInteract event) {
		if(event.getTarget() != null && !event.getWorld().isClient) {
			if(event.getTarget().getType() == EntityType.SLIME && ((SlimeEntity) event.getTarget()).getSize() == 1 && event.getTarget().isAlive()) {
				PlayerEntity player = event.getPlayer();
				Hand hand = Hand.MAIN_HAND;
				ItemStack stack = player.getMainHandStack();
				if(stack.isEmpty() || stack.getItem() != Items.BUCKET) {
					stack = player.getOffHandStack();
					hand = Hand.OFF_HAND;
				}

				if(!stack.isEmpty() && stack.getItem() == Items.BUCKET) {
					ItemStack outStack = new ItemStack(slime_in_a_bucket);
					CompoundTag cmp = event.getTarget().serializeNBT();
					ItemNBTHelper.setCompound(outStack, SlimeInABucketItem.TAG_ENTITY_DATA, cmp);
					
					if(stack.getCount() == 1)
						player.setStackInHand(hand, outStack);
					else {
						stack.decrement(1);
						if(stack.getCount() == 0)
							player.setStackInHand(hand, outStack);
						else if(!player.inventory.insertStack(outStack))
							player.dropItem(outStack, false);
					}

					player.swingHand(hand);
					event.getTarget().remove();
				}
			}
		}
	}
	
}
