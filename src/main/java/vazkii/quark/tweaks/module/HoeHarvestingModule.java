package vazkii.quark.tweaks.module;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Material;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.HoeItem;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUsageContext;
import net.minecraft.item.Items;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraft.world.WorldAccess;
import net.minecraftforge.common.IPlantable;
import net.minecraftforge.common.PlantType;
import net.minecraftforge.event.world.BlockEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import vazkii.quark.base.handler.MiscUtil;
import vazkii.quark.base.module.LoadModule;
import vazkii.quark.base.module.Module;
import vazkii.quark.base.module.ModuleCategory;
import vazkii.quark.base.module.config.Config;

@LoadModule(category = ModuleCategory.TWEAKS, hasSubscriptions = true)
public class HoeHarvestingModule extends Module {

	@Config
	public static boolean hoesCanHaveFortune = true;
	
	public static int getRange(ItemStack hoe) {
		if(hoe.isEmpty() || !(hoe.getItem() instanceof HoeItem))
			return 1;
		else if (hoe.getItem() == Items.DIAMOND_HOE || hoe.getItem() == Items.NETHERITE_HOE)
			return 3;
		else
			return 2;
	}

	public static boolean canFortuneApply(Enchantment enchantment, ItemStack stack) {
		return enchantment == Enchantments.FORTUNE && hoesCanHaveFortune && !stack.isEmpty() && stack.getItem() instanceof HoeItem;
	}

	@SubscribeEvent
	public void onBlockBroken(BlockEvent.BreakEvent event) {
		WorldAccess world = event.getWorld();
		if(!(world instanceof World))
			return;
		
		PlayerEntity player = event.getPlayer();
		BlockPos basePos = event.getPos();
		ItemStack stack = player.getMainHandStack();
		if (!stack.isEmpty() && stack.getItem() instanceof HoeItem && canHarvest(player, world, basePos, event.getState())) {
			int range = getRange(stack);

			for (int i = 1 - range; i < range; i++)
				for (int k = 1 - range; k < range; k++) {
					if (i == 0 && k == 0)
						continue;

					BlockPos pos = basePos.add(i, 0, k);
					BlockState state = world.getBlockState(pos);
					if (canHarvest(player, world, pos, state)) {
						Block block = state.getBlock();
						if (block.canHarvestBlock(state, world, pos, player))
							block.afterBreak((World) world, player, pos, state, world.getBlockEntity(pos), stack);
						world.breakBlock(pos, false);
						world.syncWorldEvent(2001, pos, Block.getRawIdFromState(state));
					}
				}

			MiscUtil.damageStack(player, Hand.MAIN_HAND, stack, 1);
		}
	}

	private boolean canHarvest(PlayerEntity player, WorldAccess world, BlockPos pos, BlockState state) {
		Block block = state.getBlock();
		if(block instanceof IPlantable) {
			IPlantable plant = (IPlantable) block;
			PlantType type = plant.getPlantType(world, pos);
			return type != PlantType.WATER && type != PlantType.DESERT;
		}

		return state.getMaterial() == Material.PLANT && state.canReplace(new ItemPlacementContext(new ItemUsageContext(player, Hand.MAIN_HAND,
				new BlockHitResult(new Vec3d(0.5, 0.5, 0.5), Direction.DOWN, pos, false))));
	}

}
