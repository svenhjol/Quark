package vazkii.quark.content.client.module;

import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.entity.passive.horse.AbstractHorseEntity;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.client.event.RenderGameOverlayEvent.ElementType;
import net.minecraftforge.client.gui.ForgeIngameGui;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import vazkii.quark.base.module.LoadModule;
import vazkii.quark.base.module.QuarkModule;
import vazkii.quark.base.module.ModuleCategory;

@LoadModule(category = ModuleCategory.CLIENT, hasSubscriptions = true, subscribeOn = Dist.CLIENT)
public class ImprovedMountHudModule extends QuarkModule {

	@SubscribeEvent
	public void onRenderHUD(RenderGameOverlayEvent.Pre event) {
		if(event.getType() == ElementType.ALL) {
			Minecraft mc = Minecraft.getInstance();
			Entity riding = mc.player.getRidingEntity();
			
			if(riding != null) {
				ForgeIngameGui.renderFood = true;
				if(riding instanceof AbstractHorseEntity)
					ForgeIngameGui.renderJumpBar = mc.gameSettings.keyBindJump.isKeyDown() && mc.currentScreen == null;
			}
		}
	}
	
}
