package vazkii.quark.content.tweaks.module;

import net.minecraft.block.material.Material;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Hand;
import net.minecraftforge.common.ToolType;
import net.minecraftforge.event.entity.player.PlayerEvent.BreakSpeed;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import vazkii.quark.base.module.LoadModule;
import vazkii.quark.base.module.QuarkModule;
import vazkii.quark.base.module.ModuleCategory;

@LoadModule(category = ModuleCategory.TWEAKS, hasSubscriptions = true)
public class AxeLeafHarvestingModule extends QuarkModule {

	@SubscribeEvent
	public void calcBreakSpeed(BreakSpeed event) {
		if (event.getOriginalSpeed() <= 0)
			return;

		ItemStack stack = event.getPlayer().getHeldItem(Hand.MAIN_HAND);
		if(stack.getItem().getToolTypes(stack).contains(ToolType.AXE) &&
				event.getState().getMaterial() == Material.LEAVES)
			event.setNewSpeed(100 * event.getState().getBlockHardness(event.getPlayer().world, event.getPos()));
	}	

}
