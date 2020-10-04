package vazkii.quark.base.client;

import java.util.List;
import java.util.stream.Collectors;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.LiteralText;
import net.minecraft.text.StringRenderable;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.TickEvent.Phase;
import net.minecraftforge.event.TickEvent.RenderTickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.client.gui.GuiUtils;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import vazkii.quark.base.Quark;

@EventBusSubscriber(modid = Quark.MOD_ID, value = Dist.CLIENT)
public class TopLayerTooltipHandler {

	private static List<StringRenderable> tooltip;
	private static int tooltipX, tooltipY;

	@SubscribeEvent
	public static void renderTick(RenderTickEvent event) {
		if(event.phase == Phase.END && tooltip != null) {
			MinecraftClient mc = MinecraftClient.getInstance();
			Screen screen = MinecraftClient.getInstance().currentScreen;
			GuiUtils.drawHoveringText(new MatrixStack(), tooltip, tooltipX, tooltipY, screen.width, screen.height, -1, mc.textRenderer);
			tooltip = null;
		}
	}

	public static void setTooltip(List<String> tooltip, int tooltipX, int tooltipY) {
		TopLayerTooltipHandler.tooltip = tooltip.stream().map(LiteralText::new).collect(Collectors.toList());
		TopLayerTooltipHandler.tooltipX = tooltipX;
		TopLayerTooltipHandler.tooltipY = tooltipY;
	}

}
