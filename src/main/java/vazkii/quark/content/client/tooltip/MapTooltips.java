package vazkii.quark.content.client.tooltip;

import net.minecraft.util.math.vector.Matrix4f;
import org.lwjgl.opengl.GL11;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.systems.RenderSystem;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.item.FilledMapItem;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.storage.MapData;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.RenderTooltipEvent;
import net.minecraftforge.event.entity.player.ItemTooltipEvent;
import vazkii.quark.content.client.module.ImprovedTooltipsModule;

public class MapTooltips {

	private static final ResourceLocation RES_MAP_BACKGROUND = new ResourceLocation("textures/map/map_background.png");


	@OnlyIn(Dist.CLIENT)
	public static void makeTooltip(ItemTooltipEvent event) {
		if(!event.getItemStack().isEmpty() && event.getItemStack().getItem() instanceof FilledMapItem) {
			if(ImprovedTooltipsModule.mapRequireShift && !Screen.hasShiftDown())
				event.getToolTip().add(1, new TranslationTextComponent("quark.misc.map_shift"));
		}
	}

	@OnlyIn(Dist.CLIENT)
	public static void renderTooltip(RenderTooltipEvent.PostText event) {
		if(!event.getStack().isEmpty() && event.getStack().getItem() instanceof FilledMapItem && (!ImprovedTooltipsModule.mapRequireShift || Screen.hasShiftDown())) {
			Minecraft mc = Minecraft.getInstance();

			MapData mapdata = FilledMapItem.getMapData(event.getStack(), mc.world);
			if(mapdata == null)
				return;

			MatrixStack ms = event.getMatrixStack();
			RenderSystem.color3f(1F, 1F, 1F);
			mc.getTextureManager().bindTexture(RES_MAP_BACKGROUND);
			Tessellator tessellator = Tessellator.getInstance();
			BufferBuilder buffer = tessellator.getBuffer();

			int pad = 7;
			float size = 135;
			float scale = 0.5F;

			ms.translate(event.getX(), event.getY() - size * scale - 5, 500);
			ms.scale(scale, scale, 1F);
			RenderSystem.enableBlend();

			Matrix4f mat = ms.getLast().getMatrix();
			buffer.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX);
			buffer.pos(mat, -pad, size, 0.0F).tex(0.0F, 1.0f).endVertex();
			buffer.pos(mat, size, size, 0.0F).tex(1.0F, 1.0f).endVertex();
			buffer.pos(mat, size, -pad, 0.0F).tex(1.0F, 0.0F).endVertex();
			buffer.pos(mat, -pad, -pad, 0.0F).tex(0.0F, 0.0F).endVertex();
			tessellator.draw();

			IRenderTypeBuffer.Impl immediateBuffer = IRenderTypeBuffer.getImpl(buffer);
			mc.gameRenderer.getMapItemRenderer().renderMap(ms, immediateBuffer, mapdata, true, 240);
			immediateBuffer.finish();
		}
	}

}
