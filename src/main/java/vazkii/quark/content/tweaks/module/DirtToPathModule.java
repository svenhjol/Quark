package vazkii.quark.content.tweaks.module;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.material.Material;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Direction;
import net.minecraft.util.Hand;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvents;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.common.ToolType;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import vazkii.quark.base.handler.MiscUtil;
import vazkii.quark.base.module.LoadModule;
import vazkii.quark.base.module.QuarkModule;
import vazkii.quark.content.tools.item.PickarangItem;
import vazkii.quark.base.module.ModuleCategory;

@LoadModule(category = ModuleCategory.TWEAKS, hasSubscriptions = true)
public class DirtToPathModule extends QuarkModule {

	@SubscribeEvent
	public void onRightClick(PlayerInteractEvent.RightClickBlock event) {
		doTheShovelThingHomie(event, ToolType.SHOVEL, Blocks.DIRT, Blocks.GRASS_PATH);
	}
	
	public static void doTheShovelThingHomie(PlayerInteractEvent.RightClickBlock event, ToolType tool, Block target, Block result) {
		PlayerEntity player = event.getPlayer();
		BlockPos pos = event.getPos();
		Direction facing = event.getFace();
		World world = event.getWorld();
		Hand hand = event.getHand();
		ItemStack itemstack = player.getHeldItem(hand);
		BlockState state = world.getBlockState(pos);

		if(itemstack.getItem() instanceof PickarangItem || !itemstack.getItem().getToolTypes(itemstack).contains(tool) || itemstack.getDestroySpeed(state) < 1.0F)
			return;

		if(facing != null && player.canPlayerEdit(pos.offset(facing), facing, itemstack)) {
			Block block = state.getBlock();

			if(facing != Direction.DOWN && world.getBlockState(pos.up()).getMaterial() == Material.AIR && block == target) {
				BlockState pathState = result.getDefaultState();
				world.playSound(player, pos, SoundEvents.ITEM_SHOVEL_FLATTEN, SoundCategory.BLOCKS, 1.0F, 1.0F);

				if(!world.isRemote) {
					world.setBlockState(pos, pathState, 11);
					MiscUtil.damageStack(player, hand, itemstack, 1);
				}

				event.setCanceled(true);
				event.setCancellationResult(ActionResultType.SUCCESS);
			}
		}
	}
	
}
