package vazkii.quark.base.client.handler;

import java.util.List;
import java.util.stream.Collectors;

import com.mojang.blaze3d.matrix.MatrixStack;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.util.text.ITextProperties;
import net.minecraft.util.text.StringTextComponent;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.TickEvent.Phase;
import net.minecraftforge.event.TickEvent.RenderTickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.client.gui.GuiUtils;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import vazkii.quark.base.Quark;

@EventBusSubscriber(modid = Quark.MOD_ID, value = Dist.CLIENT)
public class TopLayerTooltipHandler {

	private static List<ITextProperties> tooltip;
	private static int tooltipX, tooltipY;

	@SubscribeEvent
	public static void renderTick(RenderTickEvent event) {
		if(event.phase == Phase.END && tooltip != null) {
			Minecraft mc = Minecraft.getInstance();
			Screen screen = Minecraft.getInstance().currentScreen;
			GuiUtils.drawHoveringText(new MatrixStack(), tooltip, tooltipX, tooltipY, screen.width, screen.height, -1, mc.fontRenderer);
			tooltip = null;
		}
	}

	public static void setTooltip(List<String> tooltip, int tooltipX, int tooltipY) {
		TopLayerTooltipHandler.tooltip = tooltip.stream().map(StringTextComponent::new).collect(Collectors.toList());
		TopLayerTooltipHandler.tooltipX = tooltipX;
		TopLayerTooltipHandler.tooltipY = tooltipY;
	}

}
