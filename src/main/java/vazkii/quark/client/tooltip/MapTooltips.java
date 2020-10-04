package vazkii.quark.client.tooltip;

import org.lwjgl.opengl.GL11;
import com.mojang.blaze3d.systems.RenderSystem;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.render.DiffuseLighting;
import net.minecraft.client.render.Tessellator;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.VertexFormats;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.FilledMapItem;
import net.minecraft.item.map.MapState;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.Identifier;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.RenderTooltipEvent;
import net.minecraftforge.event.entity.player.ItemTooltipEvent;
import vazkii.quark.client.module.ImprovedTooltipsModule;

public class MapTooltips {

	private static final Identifier RES_MAP_BACKGROUND = new Identifier("textures/map/map_background.png");


	@Environment(EnvType.CLIENT)
	public static void makeTooltip(ItemTooltipEvent event) {
		if(!event.getItemStack().isEmpty() && event.getItemStack().getItem() instanceof FilledMapItem) {
			if(ImprovedTooltipsModule.mapRequireShift && !Screen.hasShiftDown())
				event.getToolTip().add(1, new TranslatableText("quark.misc.map_shift"));
		}
	}

	@Environment(EnvType.CLIENT)
	public static void renderTooltip(RenderTooltipEvent.PostText event) {
		if(!event.getStack().isEmpty() && event.getStack().getItem() instanceof FilledMapItem && (!ImprovedTooltipsModule.mapRequireShift || Screen.hasShiftDown())) {
			MinecraftClient mc = MinecraftClient.getInstance();

			MapState mapdata = FilledMapItem.getOrCreateMapState(event.getStack(), mc.world);
			if(mapdata == null)
				return;

			RenderSystem.pushMatrix();
			RenderSystem.color3f(1F, 1F, 1F);
			DiffuseLighting.disable();
			mc.getTextureManager().bindTexture(RES_MAP_BACKGROUND);
			Tessellator tessellator = Tessellator.getInstance();
			BufferBuilder buffer = tessellator.getBuffer();

			int pad = 7;
			float size = 135;
			float scale = 0.5F;

			RenderSystem.translatef(event.getX(), event.getY() - size * scale - 5, 500);
			RenderSystem.scalef(scale, scale, 1F);
			RenderSystem.enableBlend();

			buffer.begin(GL11.GL_QUADS, VertexFormats.POSITION_TEXTURE);
			buffer.vertex(-pad, size, 0.0D).texture(0.0F, 1.0f).next();
			buffer.vertex(size, size, 0.0D).texture(1.0F, 1.0f).next();
			buffer.vertex(size, -pad, 0.0D).texture(1.0F, 0.0F).next();
			buffer.vertex(-pad, -pad, 0.0D).texture(0.0F, 0.0F).next();
			tessellator.draw();

			VertexConsumerProvider.Immediate immediateBuffer = VertexConsumerProvider.immediate(buffer);
			MatrixStack matrix = new MatrixStack();
			mc.gameRenderer.getMapRenderer().draw(matrix, immediateBuffer, mapdata, true, 240);
			immediateBuffer.draw();

			RenderSystem.disableBlend();
			RenderSystem.enableLighting();
			RenderSystem.popMatrix();
		}
	}

}
