package vazkii.quark.addons.oddities.client.screen;

import org.lwjgl.opengl.GL11;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.systems.RenderSystem;

import net.minecraft.client.MainWindow;
import net.minecraft.client.gui.widget.list.ExtendedList;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.math.MathHelper;
import vazkii.quark.addons.oddities.container.EnchantmentMatrix.Piece;

public class MatrixEnchantingPieceList extends ExtendedList<MatrixEnchantingPieceList.PieceEntry> {

	private final MatrixEnchantingScreen parent;
	private final int listWidth;

	public MatrixEnchantingPieceList(MatrixEnchantingScreen parent, int listWidth, int listHeight, int top, int bottom, int entryHeight) {
		super(parent.getMinecraft(), listWidth, listHeight, top, bottom, entryHeight);
		this.listWidth = listWidth;
		this.parent = parent;
	}

	@Override
	protected int getScrollbarPosition() {
		return getLeft() + this.listWidth - 5;
	}

	@Override
	public int getRowWidth() {
		return this.listWidth;
	}

	public void refresh() {
		clearEntries();

		if(parent.listPieces != null)
			for(int i : parent.listPieces) {
				Piece piece = parent.getPiece(i);
				if(piece != null)
					addEntry(new PieceEntry(piece, i));
			}
	}

	@Override
	public void render(MatrixStack stack, int p_render_1_, int p_render_2_, float p_render_3_) {
		int i = this.getScrollbarPosition();
		int j = i + 6;
		int k = this.getRowLeft();
		int l = this.y0 + 4 - (int)this.getScrollAmount();
		
		fill(stack, getLeft(), getTop(), getLeft() + getWidth() + 1, getTop() + getHeight(), 0xFF2B2B2B);
		
		MainWindow main = parent.getMinecraft().getMainWindow();
		int res = (int) main.getGuiScaleFactor();
		GL11.glEnable(GL11.GL_SCISSOR_TEST);
		GL11.glScissor(getLeft() * res, (main.getScaledHeight() - getBottom()) * res, getWidth() * res, getHeight() * res);
		renderList(stack, k, l, p_render_1_, p_render_2_, p_render_3_);
		GL11.glDisable(GL11.GL_SCISSOR_TEST);
		
		renderScroll(i, j);
	}

	protected int getMaxScroll2() {
		return Math.max(0, this.getMaxPosition() - (this.y1 - this.y0 - 4));
	}

	private void renderScroll(int i, int j) {
		int j1 = this.getMaxScroll2();
		if (j1 > 0) {
			int k1 = (int)((float)((this.y1 - this.y0) * (this.y1 - this.y0)) / (float)this.getMaxPosition());
			k1 = MathHelper.clamp(k1, 32, this.y1 - this.y0 - 8);
			int l1 = (int)this.getScrollAmount() * (this.y1 - this.y0 - k1) / j1 + this.y0;
			if (l1 < this.y0) {
				l1 = this.y0;
			}
			
			RenderSystem.disableTexture();
			Tessellator tessellator = Tessellator.getInstance();
			BufferBuilder bufferbuilder = tessellator.getBuffer();
			bufferbuilder.begin(7, DefaultVertexFormats.POSITION_TEX_COLOR);
			bufferbuilder.pos((double)i, (double)this.y1, 0.0D).tex(0.0F, 1.0F).color(0, 0, 0, 255).endVertex();
			bufferbuilder.pos((double)j, (double)this.y1, 0.0D).tex(1.0F, 1.0F).color(0, 0, 0, 255).endVertex();
			bufferbuilder.pos((double)j, (double)this.y0, 0.0D).tex(1.0F, 0.0F).color(0, 0, 0, 255).endVertex();
			bufferbuilder.pos((double)i, (double)this.y0, 0.0D).tex(0.0F, 0.0F).color(0, 0, 0, 255).endVertex();
			tessellator.draw();
			bufferbuilder.begin(7, DefaultVertexFormats.POSITION_TEX_COLOR);
			bufferbuilder.pos((double)i, (double)(l1 + k1), 0.0D).tex(0.0F, 1.0F).color(128, 128, 128, 255).endVertex();
			bufferbuilder.pos((double)j, (double)(l1 + k1), 0.0D).tex(1.0F, 1.0F).color(128, 128, 128, 255).endVertex();
			bufferbuilder.pos((double)j, (double)l1, 0.0D).tex(1.0F, 0.0F).color(128, 128, 128, 255).endVertex();
			bufferbuilder.pos((double)i, (double)l1, 0.0D).tex(0.0F, 0.0F).color(128, 128, 128, 255).endVertex();
			tessellator.draw();
			bufferbuilder.begin(7, DefaultVertexFormats.POSITION_TEX_COLOR);
			bufferbuilder.pos((double)i, (double)(l1 + k1 - 1), 0.0D).tex(0.0F, 1.0F).color(192, 192, 192, 255).endVertex();
			bufferbuilder.pos((double)(j - 1), (double)(l1 + k1 - 1), 0.0D).tex(1.0F, 1.0F).color(192, 192, 192, 255).endVertex();
			bufferbuilder.pos((double)(j - 1), (double)l1, 0.0D).tex(1.0F, 0.0F).color(192, 192, 192, 255).endVertex();
			bufferbuilder.pos((double)i, (double)l1, 0.0D).tex(0.0F, 0.0F).color(192, 192, 192, 255).endVertex();
			tessellator.draw();
			RenderSystem.enableTexture();
		}
	}

	@Override
	protected void renderBackground(MatrixStack stack) {
		// NO-OP
	}

	protected class PieceEntry extends ExtendedList.AbstractListEntry<PieceEntry> {

		final Piece piece;
		final int index;

		PieceEntry(Piece piece, int index) {
			this.piece = piece;
			this.index = index;
		}

		@Override
		public void render(MatrixStack stack, int entryIdx, int top, int left, int entryWidth, int entryHeight, int mouseX, int mouseY, boolean hover, float partialTicks) {
			if(hover)
				parent.hoveredPiece = piece;

			parent.getMinecraft().getTextureManager().bindTexture(MatrixEnchantingScreen.BACKGROUND);
			RenderSystem.pushMatrix();
			RenderSystem.translatef(left + (listWidth - 7) / 2f, top + entryHeight / 2f, 0);
			RenderSystem.scaled(0.5, 0.5, 0.5);
			RenderSystem.translatef(-8, -8, 0);
			parent.renderPiece(stack, piece, 1F);
			RenderSystem.popMatrix();
		}

		@Override
		public boolean mouseClicked(double p_mouseClicked_1_, double p_mouseClicked_3_, int p_mouseClicked_5_) {
			parent.selectedPiece = index;
			setSelected(this);
			return false;
		}

	}

}