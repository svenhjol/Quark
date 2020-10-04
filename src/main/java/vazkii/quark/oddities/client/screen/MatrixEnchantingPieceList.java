package vazkii.quark.oddities.client.screen;

import org.lwjgl.opengl.GL11;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.gui.widget.AlwaysSelectedEntryListWidget;
import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.render.Tessellator;
import net.minecraft.client.render.VertexFormats;
import net.minecraft.client.util.Window;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.MathHelper;
import vazkii.quark.oddities.container.EnchantmentMatrix.Piece;

public class MatrixEnchantingPieceList extends AlwaysSelectedEntryListWidget<MatrixEnchantingPieceList.PieceEntry> {

	private final MatrixEnchantingScreen parent;
	private final int listWidth;

	public MatrixEnchantingPieceList(MatrixEnchantingScreen parent, int listWidth, int listHeight, int top, int bottom, int entryHeight) {
		super(parent.getMinecraft(), listWidth, listHeight, top, bottom, entryHeight);
		this.listWidth = listWidth;
		this.parent = parent;
	}

	@Override
	protected int getScrollbarPositionX() {
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
		int i = this.getScrollbarPositionX();
		int j = i + 6;
		int k = this.getRowLeft();
		int l = this.top + 4 - (int)this.getScrollAmount();
		
		fill(stack, getLeft(), getTop(), getLeft() + getWidth() + 1, getTop() + getHeight(), 0xFF2B2B2B);
		
		Window main = parent.getMinecraft().getWindow();
		int res = (int) main.getScaleFactor();
		GL11.glEnable(GL11.GL_SCISSOR_TEST);
		GL11.glScissor(getLeft() * res, (main.getScaledHeight() - getBottom()) * res, getWidth() * res, getHeight() * res);
		renderList(stack, k, l, p_render_1_, p_render_2_, p_render_3_);
		GL11.glDisable(GL11.GL_SCISSOR_TEST);
		
		renderScroll(i, j);
	}

	protected int getMaxScroll2() {
		return Math.max(0, this.getMaxPosition() - (this.bottom - this.top - 4));
	}

	private void renderScroll(int i, int j) {
		int j1 = this.getMaxScroll2();
		if (j1 > 0) {
			int k1 = (int)((float)((this.bottom - this.top) * (this.bottom - this.top)) / (float)this.getMaxPosition());
			k1 = MathHelper.clamp(k1, 32, this.bottom - this.top - 8);
			int l1 = (int)this.getScrollAmount() * (this.bottom - this.top - k1) / j1 + this.top;
			if (l1 < this.top) {
				l1 = this.top;
			}
			
			RenderSystem.disableTexture();
			Tessellator tessellator = Tessellator.getInstance();
			BufferBuilder bufferbuilder = tessellator.getBuffer();
			bufferbuilder.begin(7, VertexFormats.POSITION_TEXTURE_COLOR);
			bufferbuilder.vertex((double)i, (double)this.bottom, 0.0D).texture(0.0F, 1.0F).color(0, 0, 0, 255).next();
			bufferbuilder.vertex((double)j, (double)this.bottom, 0.0D).texture(1.0F, 1.0F).color(0, 0, 0, 255).next();
			bufferbuilder.vertex((double)j, (double)this.top, 0.0D).texture(1.0F, 0.0F).color(0, 0, 0, 255).next();
			bufferbuilder.vertex((double)i, (double)this.top, 0.0D).texture(0.0F, 0.0F).color(0, 0, 0, 255).next();
			tessellator.draw();
			bufferbuilder.begin(7, VertexFormats.POSITION_TEXTURE_COLOR);
			bufferbuilder.vertex((double)i, (double)(l1 + k1), 0.0D).texture(0.0F, 1.0F).color(128, 128, 128, 255).next();
			bufferbuilder.vertex((double)j, (double)(l1 + k1), 0.0D).texture(1.0F, 1.0F).color(128, 128, 128, 255).next();
			bufferbuilder.vertex((double)j, (double)l1, 0.0D).texture(1.0F, 0.0F).color(128, 128, 128, 255).next();
			bufferbuilder.vertex((double)i, (double)l1, 0.0D).texture(0.0F, 0.0F).color(128, 128, 128, 255).next();
			tessellator.draw();
			bufferbuilder.begin(7, VertexFormats.POSITION_TEXTURE_COLOR);
			bufferbuilder.vertex((double)i, (double)(l1 + k1 - 1), 0.0D).texture(0.0F, 1.0F).color(192, 192, 192, 255).next();
			bufferbuilder.vertex((double)(j - 1), (double)(l1 + k1 - 1), 0.0D).texture(1.0F, 1.0F).color(192, 192, 192, 255).next();
			bufferbuilder.vertex((double)(j - 1), (double)l1, 0.0D).texture(1.0F, 0.0F).color(192, 192, 192, 255).next();
			bufferbuilder.vertex((double)i, (double)l1, 0.0D).texture(0.0F, 0.0F).color(192, 192, 192, 255).next();
			tessellator.draw();
			RenderSystem.enableTexture();
		}
	}

	@Override
	protected void renderBackground(MatrixStack stack) {
		// NO-OP
	}

	protected class PieceEntry extends AlwaysSelectedEntryListWidget.Entry<PieceEntry> {

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