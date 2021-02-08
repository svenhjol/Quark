package vazkii.quark.content.experimental.module;

import com.mojang.blaze3d.systems.RenderSystem;

import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.client.event.RenderGameOverlayEvent.ElementType;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import vazkii.quark.base.module.LoadModule;
import vazkii.quark.base.module.QuarkModule;
import vazkii.quark.base.module.ModuleCategory;
import vazkii.quark.base.module.config.Config;

@LoadModule(category = ModuleCategory.EXPERIMENTAL, enabledByDefault = false, hasSubscriptions = true, subscribeOn = Dist.CLIENT)
public class AdjustableChatModule extends QuarkModule {

	@Config public static int horizontalShift = 0;
	@Config public static int verticalShift = 0;
	
	@SubscribeEvent
	@OnlyIn(Dist.CLIENT)
	public void pre(RenderGameOverlayEvent.Pre event) {
		if(event.getType() == ElementType.CHAT)
			event.getMatrixStack().translate(horizontalShift, verticalShift, 0);
	}
	
	@SubscribeEvent
	@OnlyIn(Dist.CLIENT)
	public void post(RenderGameOverlayEvent.Post event) {
		if(event.getType() == ElementType.CHAT)
			event.getMatrixStack().translate(-horizontalShift, -verticalShift, 0);
	}
	
}
