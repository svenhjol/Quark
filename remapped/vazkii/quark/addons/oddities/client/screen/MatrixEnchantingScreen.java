package vazkii.quark.addons.oddities.client.screen;

import java.util.LinkedList;
import java.util.List;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.resource.language.I18n;
import net.minecraft.client.sound.PositionedSoundInstance;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import vazkii.arl.util.ClientTicker;
import vazkii.quark.addons.oddities.container.EnchantmentMatrix;
import vazkii.quark.addons.oddities.container.MatrixEnchantingContainer;
import vazkii.quark.addons.oddities.container.EnchantmentMatrix.Piece;
import vazkii.quark.addons.oddities.module.MatrixEnchantingModule;
import vazkii.quark.addons.oddities.tile.MatrixEnchantingTableTileEntity;
import vazkii.quark.base.Quark;
import vazkii.quark.base.network.QuarkNetwork;
import vazkii.quark.base.network.message.MatrixEnchanterOperationMessage;

public class MatrixEnchantingScreen extends HandledScreen<MatrixEnchantingContainer> {

	public static final Identifier BACKGROUND = new Identifier(Quark.MOD_ID, "textures/misc/matrix_enchanting.png");

	protected final PlayerInventory playerInv;
	protected final MatrixEnchantingTableTileEntity enchanter;

	protected ButtonWidget plusButton;
	protected MatrixEnchantingPieceList pieceList;
	protected Piece hoveredPiece;

	protected int selectedPiece = -1;
	protected int gridHoverX, gridHoverY;
	protected List<Integer> listPieces = null;

	public MatrixEnchantingScreen(MatrixEnchantingContainer container, PlayerInventory inventory, Text component) {
		super(container, inventory, component);
		this.playerInv = inventory;
		this.enchanter = container.enchanter;
	}

	@Override
	public void init(MinecraftClient mc, int x, int y) {
		super.init(mc, x, y);

		selectedPiece = -1;
		addButton(plusButton = new MatrixEnchantingPlusButton(x + 86, y + 63, this::add));
		pieceList = new MatrixEnchantingPieceList(this, 28, 64, y + 11, y + 75, 22);
		pieceList.setLeftPos(x + 139);
		children.add(pieceList);
		updateButtonStatus();
		
		pieceList.refresh();
	}

	@Override
	public void tick() {
		super.tick();
		updateButtonStatus();

		if(enchanter.matrix == null) {
			selectedPiece = -1;
			pieceList.refresh();
		}
		
		if(enchanter.clientMatrixDirty) {
			pieceList.refresh();
			enchanter.clientMatrixDirty = false;
		}
	}

	@Override
	protected void drawBackground(MatrixStack stack, float partialTicks, int mouseX, int mouseY) {
		MinecraftClient mc = getMinecraft();
		RenderSystem.color4f(1.0F, 1.0F, 1.0F, 1.0F);
		mc.getTextureManager().bindTexture(BACKGROUND);
		int i = x;
		int j = y;
		drawTexture(stack, i, j, 0, 0, backgroundWidth, backgroundHeight);

		if(enchanter.charge > 0 && MatrixEnchantingModule.chargePerLapis > 0) {
			int maxHeight = 18;
			int barHeight = (int) (((float) enchanter.charge / MatrixEnchantingModule.chargePerLapis) * maxHeight);
			drawTexture(stack, i + 7, j + 32 + maxHeight - barHeight, 50, 176 + maxHeight - barHeight, 4, barHeight);
		}
		
		pieceList.render(stack, mouseX, mouseY, partialTicks);

		if(enchanter.matrix != null && enchanter.matrix.canGeneratePiece(enchanter.bookshelfPower, enchanter.enchantability) && !mc.player.isCreative()) {
			int x = i + 74;
			int y = j + 58;
			int xpCost = enchanter.matrix.getNewPiecePrice();
			int xpMin = enchanter.matrix.getMinXpLevel(enchanter.bookshelfPower);
			boolean has = enchanter.matrix.validateXp(mc.player, enchanter.bookshelfPower);
			drawTexture(stack, x, y, 0, backgroundHeight, 10, 10);
			String text = String.valueOf(xpCost);

			if(!has && mc.player.experienceLevel < xpMin) {
				textRenderer.drawWithShadow(stack, "!", x + 6, y + 3, 0xFF0000);
				text = I18n.translate("quark.gui.enchanting.min", xpMin);
			}

			x -= (textRenderer.getWidth(text) - 5);
			y += 3;
			textRenderer.draw(stack, text, x - 1, y, 0);
			textRenderer.draw(stack, text, x + 1, y, 0);
			textRenderer.draw(stack, text, x, y + 1, 0);
			textRenderer.draw(stack, text, x, y - 1, 0);
			textRenderer.draw(stack, text, x, y, has ? 0xc8ff8f : 0xff8f8f);
		}
	}
	
	@Override
	protected void drawForeground(MatrixStack matrix, int mouseX, int mouseY) {
		textRenderer.draw(matrix, enchanter.getDisplayName().getString(), 12, 5, 4210752);
		textRenderer.draw(matrix, playerInv.getDisplayName().getString(), 8, backgroundHeight - 96 + 2, 4210752);

		if(enchanter.matrix != null) {
			boolean needsRefresh = listPieces == null;
			listPieces = enchanter.matrix.benchedPieces;
			if(needsRefresh)
				pieceList.refresh();
			renderMatrixGrid(matrix, enchanter.matrix);
		}
	}
	@Override
	public void render(MatrixStack stack, int mouseX, int mouseY, float partialTicks) {
		renderBackground(stack);
		super.render(stack, mouseX, mouseY, partialTicks);

		if(hoveredPiece != null) {
			List<Text> tooltip = new LinkedList<>();
			tooltip.add(new TranslatableText(hoveredPiece.enchant.getName(hoveredPiece.level).getString().replaceAll("\\u00A7.", "")).formatted(Formatting.GOLD));

			if(hoveredPiece.influence > 0)
				tooltip.add(new TranslatableText("quark.gui.enchanting.influence", (int) (hoveredPiece.influence * MatrixEnchantingModule.influencePower * 100)).formatted(Formatting.GRAY));

			int max = hoveredPiece.getMaxXP();
			if(max > 0)
				tooltip.add(new TranslatableText("quark.gui.enchanting.upgrade", hoveredPiece.xp, max).formatted(Formatting.GRAY));

			if(gridHoverX == -1) {
				tooltip.add(new LiteralText(""));
				tooltip.add(new TranslatableText("quark.gui.enchanting.left_click").formatted(Formatting.GRAY));
				tooltip.add(new TranslatableText("quark.gui.enchanting.right_click").formatted(Formatting.GRAY));
			} else if(selectedPiece != -1) {
				Piece p = getPiece(selectedPiece);
				if(p != null && p.enchant == hoveredPiece.enchant && hoveredPiece.level < hoveredPiece.enchant.getMaxLevel()) {
					tooltip.add(new LiteralText(""));
					tooltip.add(new TranslatableText("quark.gui.enchanting.merge").formatted(Formatting.GRAY));
				}
			}

			renderTooltip(stack, tooltip, mouseX, mouseY); // renderTooltip
		} else 
			drawMouseoverTooltip(stack, mouseX, mouseY);
	}

	@Override
	public void mouseMoved(double mouseX, double mouseY) {
		int gridMouseX = (int) (mouseX - x - 86);
		int gridMouseY = (int) (mouseY - y - 11);

		gridHoverX = gridMouseX < 0 ? -1 : gridMouseX / 10;
		gridHoverY = gridMouseY < 0 ? -1 : gridMouseY / 10;
		if(gridHoverX < 0 || gridHoverX > 4 || gridHoverY < 0 || gridHoverY > 4) {
			gridHoverX = -1;
			gridHoverY = -1;
			hoveredPiece = null;
		} else if(enchanter.matrix != null) {
			int hover = enchanter.matrix.matrix[gridHoverX][gridHoverY];
			hoveredPiece = getPiece(hover);
		}

		super.mouseMoved(mouseX, mouseY);
	}

	@Override
	public boolean mouseClicked(double mouseX, double mouseY, int mouseButton) {
		super.mouseClicked(mouseX, mouseY, mouseButton);
		
		if(enchanter.matrix == null)
			return true;

		if(mouseButton == 0 && gridHoverX != -1) { // left click
			int hover = enchanter.matrix.matrix[gridHoverX][gridHoverY];

			if(selectedPiece != -1) {
				if(hover == -1)
					place(selectedPiece, gridHoverX, gridHoverY);
				else merge(selectedPiece);
			} else {
				remove(hover);
				if(!hasShiftDown())
					selectedPiece = hover;
			}
		} else if(mouseButton == 1 && selectedPiece != -1) {
			rotate(selectedPiece);
		}
		
		return true;
	}

	private void renderMatrixGrid(MatrixStack stack, EnchantmentMatrix matrix) {
		MinecraftClient mc = getMinecraft();
		mc.getTextureManager().bindTexture(BACKGROUND);
		RenderSystem.pushMatrix();
		RenderSystem.translatef(86, 11, 0);

		for(int i : matrix.placedPieces) {
			Piece piece = getPiece(i);
			if (piece != null) {
				RenderSystem.pushMatrix();
				RenderSystem.translatef(piece.x * 10, piece.y * 10, 0);
				renderPiece(stack, piece, 1F);
				RenderSystem.popMatrix();
			}
		}

		if(selectedPiece != -1 && gridHoverX != -1) {
			Piece piece = getPiece(selectedPiece);
			if(piece != null && !(hoveredPiece != null && piece.enchant == hoveredPiece.enchant && hoveredPiece.level < hoveredPiece.enchant.getMaxLevel())) {
				RenderSystem.pushMatrix();
				RenderSystem.translatef(gridHoverX * 10, gridHoverY * 10, 0);

				float a = 0.2F;
				if(matrix.canPlace(piece, gridHoverX, gridHoverY))
					a = (float) ((Math.sin(ClientTicker.total * 0.2) + 1) * 0.4 + 0.4);

				renderPiece(stack, piece, a);
				RenderSystem.popMatrix();
			}
		}

		if(hoveredPiece == null && gridHoverX != -1)
			renderHover(stack, gridHoverX, gridHoverY);

		RenderSystem.popMatrix();
	}

	protected void renderPiece(MatrixStack stack, Piece piece, float a) {
		float r = ((piece.color >> 16) & 0xFF) / 255F;
		float g = ((piece.color >> 8) & 0xFF) / 255F;
		float b = (piece.color & 0xFF) / 255F;

		boolean hovered = hoveredPiece == piece;

		for(int[] block : piece.blocks)
			renderBlock(stack, block[0], block[1], piece.type, r, g, b, a, hovered);

		RenderSystem.color3f(1F, 1F, 1F);
	}

	private void renderBlock(MatrixStack stack, int x, int y, int type, float r, float g, float b, float a, boolean hovered) {
		RenderSystem.color4f(r, g, b, a);
		drawTexture(stack, x * 10, y * 10, 11 + type * 10, backgroundHeight, 10, 10);
		if(hovered)
			renderHover(stack, x, y);
	}

	private void renderHover(MatrixStack stack, int x, int y) {
		fill(stack, x * 10, y * 10, x * 10 + 10, y * 10 + 10, 0x66FFFFFF);
	}

	public void add(ButtonWidget button) {
		send(MatrixEnchantingTableTileEntity.OPER_ADD, 0, 0, 0);
	}

	public void place(int id, int x, int y) {
		send(MatrixEnchantingTableTileEntity.OPER_PLACE, id, x, y);
		selectedPiece = -1;
		click();
	}

	public void remove(int id) {
		send(MatrixEnchantingTableTileEntity.OPER_REMOVE, id, 0, 0);
	}

	public void rotate(int id) {
		send(MatrixEnchantingTableTileEntity.OPER_ROTATE, id, 0, 0);
	}

	public void merge(int id) {
		int hover = enchanter.matrix.matrix[gridHoverX][gridHoverY];
		Piece p = getPiece(hover);
		Piece p1 = getPiece(selectedPiece);
		if(p != null && p1 != null && p.enchant == p1.enchant && p.level < p.enchant.getMaxLevel()) {
			send(MatrixEnchantingTableTileEntity.OPER_MERGE, hover, id, 0);
			selectedPiece = -1;
			click();
		}
	}	

	private void send(int operation, int arg0, int arg1, int arg2) {
		MatrixEnchanterOperationMessage message = new MatrixEnchanterOperationMessage(operation, arg0, arg1, arg2);
		QuarkNetwork.sendToServer(message);
	}

	private void click() {
		getMinecraft().getSoundManager().play(PositionedSoundInstance.master(SoundEvents.UI_BUTTON_CLICK, 1.0F));
	}

	private void updateButtonStatus() {
		plusButton.active = (enchanter.matrix != null 
				&& (getMinecraft().player.isCreative() || enchanter.charge > 0)
				&& enchanter.matrix.validateXp(getMinecraft().player, enchanter.bookshelfPower)
				&& enchanter.matrix.canGeneratePiece(enchanter.bookshelfPower, enchanter.enchantability));
	}

	protected Piece getPiece(int id) {
		EnchantmentMatrix matrix = enchanter.matrix;
		if(matrix != null)
			return matrix.pieces.get(id);

		return null;
	}

}
