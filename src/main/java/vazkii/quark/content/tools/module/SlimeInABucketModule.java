package vazkii.quark.content.tools.module;

import net.minecraft.entity.EntityType;
import net.minecraft.entity.monster.SlimeEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemModelsProperties;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.Hand;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvents;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import vazkii.arl.util.ItemNBTHelper;
import vazkii.quark.base.module.LoadModule;
import vazkii.quark.base.module.QuarkModule;
import vazkii.quark.content.tools.item.SlimeInABucketItem;
import vazkii.quark.base.module.ModuleCategory;

@LoadModule(category = ModuleCategory.TOOLS, hasSubscriptions = true)
public class SlimeInABucketModule extends QuarkModule {

	public static Item slime_in_a_bucket;

	@Override
	public void construct() {
		slime_in_a_bucket = new SlimeInABucketItem(this);
	}

	@Override
	@OnlyIn(Dist.CLIENT)
	public void clientSetup() {
		ItemModelsProperties.registerProperty(slime_in_a_bucket, new ResourceLocation("excited"), 
				(stack, world, e) -> ItemNBTHelper.getBoolean(stack, SlimeInABucketItem.TAG_EXCITED, false) ? 1 : 0);
	}

	@SubscribeEvent
	public void entityInteract(PlayerInteractEvent.EntityInteract event) {
		if(event.getTarget() != null) {
			if(event.getTarget().getType() == EntityType.SLIME && ((SlimeEntity) event.getTarget()).getSlimeSize() == 1 && event.getTarget().isAlive()) {
				PlayerEntity player = event.getPlayer();
				Hand hand = Hand.MAIN_HAND;
				ItemStack stack = player.getHeldItemMainhand();
				if(stack.isEmpty() || stack.getItem() != Items.BUCKET) {
					stack = player.getHeldItemOffhand();
					hand = Hand.OFF_HAND;
				}

				if(!stack.isEmpty() && stack.getItem() == Items.BUCKET) {
					if(!event.getWorld().isRemote) {
						ItemStack outStack = new ItemStack(slime_in_a_bucket);
						CompoundNBT cmp = event.getTarget().serializeNBT();
						ItemNBTHelper.setCompound(outStack, SlimeInABucketItem.TAG_ENTITY_DATA, cmp);
						
						if(stack.getCount() == 1)
							player.setHeldItem(hand, outStack);
						else {
							stack.shrink(1);
							if(stack.getCount() == 0)
								player.setHeldItem(hand, outStack);
							else if(!player.inventory.addItemStackToInventory(outStack))
								player.dropItem(outStack, false);
						}

						event.getTarget().remove();
					}
					else player.swingArm(hand);
				}
			}
		}
	}

}
