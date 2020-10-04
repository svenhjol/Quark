package vazkii.quark.tweaks.module;

import net.minecraft.block.Material;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Hand;
import net.minecraftforge.common.ToolType;
import net.minecraftforge.event.entity.player.PlayerEvent.BreakSpeed;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import vazkii.quark.base.module.LoadModule;
import vazkii.quark.base.module.Module;
import vazkii.quark.base.module.ModuleCategory;

@LoadModule(category = ModuleCategory.TWEAKS, hasSubscriptions = true)
public class AxeLeafHarvestingModule extends Module {

	@SubscribeEvent
	public void calcBreakSpeed(BreakSpeed event) {
		if (event.getOriginalSpeed() <= 0)
			return;

		ItemStack stack = event.getPlayer().getStackInHand(Hand.MAIN_HAND);
		if(stack.getItem().getToolTypes(stack).contains(ToolType.AXE) &&
				event.getState().getMaterial() == Material.LEAVES)
			event.setNewSpeed(100 * event.getState().getHardness(event.getPlayer().world, event.getPos()));
	}	

}
