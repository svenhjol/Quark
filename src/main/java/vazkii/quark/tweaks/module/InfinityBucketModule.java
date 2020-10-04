package vazkii.quark.tweaks.module;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang3.tuple.Pair;

import net.minecraft.block.DispenserBlock;
import net.minecraft.block.dispenser.DispenserBehavior;
import net.minecraft.block.dispenser.ItemDispenserBehavior;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.BucketItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPointer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.event.AnvilUpdateEvent;
import net.minecraftforge.event.TickEvent.Phase;
import net.minecraftforge.event.TickEvent.PlayerTickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import vazkii.quark.base.module.LoadModule;
import vazkii.quark.base.module.Module;
import vazkii.quark.base.module.ModuleCategory;
import vazkii.quark.base.module.config.Config;

@LoadModule(category = ModuleCategory.TWEAKS, hasSubscriptions = true)
public class InfinityBucketModule extends Module {

	private static Map<Pair<PlayerEntity, Hand>, ItemStack> bukkitPlayers = new HashMap<>();

	@Config public static int cost = 10;
	
	@Config(description = "Set this to false to prevent dispensers from using infinite water buckets") 
	public static boolean allowDispensersToUse = true;

	@Override
	public void loadComplete() {
		if(enabled) {
			DispenserBehavior behaviour = new ItemDispenserBehavior() {
				private final ItemDispenserBehavior field_239793_b_ = new ItemDispenserBehavior();

				@Override
				public ItemStack dispenseSilently(BlockPointer source, ItemStack stack) {
					boolean returnItself = false;
					if(enabled && EnchantmentHelper.getLevel(Enchantments.INFINITY, stack) > 0) {
						if(!allowDispensersToUse)
							return field_239793_b_.dispense(source, stack);;
						
						returnItself = true;
					}
					
					ItemStack copy = stack.copy();
					BucketItem bucketitem = (BucketItem) stack.getItem();
					
					BlockPos blockpos = source.getBlockPos().offset(source.getBlockState().get(DispenserBlock.FACING));
					World world = source.getWorld();
					if(bucketitem.placeFluid(null, world, blockpos, null)) {
						bucketitem.onEmptied(world, stack, blockpos);
						return returnItself ? copy : new ItemStack(Items.BUCKET);
					} else
						return field_239793_b_.dispense(source, stack);
				}
			};
			
			Map<Item, DispenserBehavior> registry = DispenserBlock.BEHAVIORS;
			registry.put(Items.WATER_BUCKET, behaviour);
		}
	}

	@SubscribeEvent
	public void onAnvilUpdate(AnvilUpdateEvent event) {
		ItemStack left = event.getLeft();
		ItemStack right = event.getRight();

		if(left.getItem() == Items.WATER_BUCKET && right.getItem() == Items.ENCHANTED_BOOK && EnchantmentHelper.get(right).get(Enchantments.INFINITY) > 0) {
			ItemStack result = left.copy();

			Map<Enchantment, Integer> map = new HashMap<>();
			map.put(Enchantments.INFINITY, 1);
			EnchantmentHelper.set(map, result);

			event.setOutput(result);
			event.setCost(cost);
		}
	}

	@SubscribeEvent
	public void playerTick(PlayerTickEvent event) {
		if(event.phase == Phase.END)
			return;

		PlayerEntity player = event.player;
		for(Hand hand : Hand.values()) {
			Pair<PlayerEntity, Hand> pair = Pair.of(player, hand);

			if(bukkitPlayers.containsKey(pair)) {
				ItemStack curr = player.getStackInHand(hand);
				if(curr.getItem() == Items.BUCKET)
					player.setStackInHand(hand, bukkitPlayers.get(pair));

				bukkitPlayers.remove(pair);
			}
		}		
		for(Hand hand : Hand.values()) {
			ItemStack stack = player.getStackInHand(hand);
			if(stack.getItem() == Items.WATER_BUCKET && EnchantmentHelper.getLevel(Enchantments.INFINITY, stack) > 0)
				bukkitPlayers.put(Pair.of(player, hand), stack.copy());
		}
	}
}
