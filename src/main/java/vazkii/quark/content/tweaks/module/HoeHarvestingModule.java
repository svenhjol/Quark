package vazkii.quark.content.tweaks.module;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.material.Material;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.BlockItemUseContext;
import net.minecraft.item.HoeItem;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUseContext;
import net.minecraft.item.Items;
import net.minecraft.util.Direction;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.IWorld;
import net.minecraft.world.World;
import net.minecraftforge.common.IPlantable;
import net.minecraftforge.common.PlantType;
import net.minecraftforge.event.world.BlockEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import vazkii.quark.base.handler.MiscUtil;
import vazkii.quark.base.module.LoadModule;
import vazkii.quark.base.module.QuarkModule;
import vazkii.quark.base.module.ModuleCategory;
import vazkii.quark.base.module.ModuleLoader;
import vazkii.quark.base.module.config.Config;

@LoadModule(category = ModuleCategory.TWEAKS, hasSubscriptions = true)
public class HoeHarvestingModule extends QuarkModule {

	@Config
	public static boolean hoesCanHaveFortune = true;
	
	public static int getRange(ItemStack hoe) {
		if(!ModuleLoader.INSTANCE.isModuleEnabled(HoeHarvestingModule.class))
			return 1;
		
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
		IWorld world = event.getWorld();
		if(!(world instanceof World))
			return;
		
		PlayerEntity player = event.getPlayer();
		BlockPos basePos = event.getPos();
		ItemStack stack = player.getHeldItemMainhand();
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
							block.harvestBlock((World) world, player, pos, state, world.getTileEntity(pos), stack);
						world.destroyBlock(pos, false);
						world.playEvent(2001, pos, Block.getStateId(state));
					}
				}

			MiscUtil.damageStack(player, Hand.MAIN_HAND, stack, 1);
		}
	}

	private boolean canHarvest(PlayerEntity player, IWorld world, BlockPos pos, BlockState state) {
		Block block = state.getBlock();
		if(block instanceof IPlantable) {
			IPlantable plant = (IPlantable) block;
			PlantType type = plant.getPlantType(world, pos);
			return type != PlantType.WATER && type != PlantType.DESERT;
		}

		Material mat = state.getMaterial();
		return (mat == Material.PLANTS || mat == Material.NETHER_PLANTS || mat == Material.TALL_PLANTS || mat == Material.OCEAN_PLANT) && 
				state.isReplaceable(new BlockItemUseContext(new ItemUseContext(player, Hand.MAIN_HAND, new BlockRayTraceResult(new Vector3d(0.5, 0.5, 0.5), Direction.DOWN, pos, false))));
	}

}
