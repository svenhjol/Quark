package vazkii.quark.content.management.client.gui;

import java.util.LinkedList;
import java.util.List;
import java.util.function.BooleanSupplier;
import java.util.function.Consumer;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.client.gui.screen.recipebook.RecipeBookProvider;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.resource.language.I18n;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.LiteralText;
import net.minecraft.text.MutableText;
import net.minecraft.text.TranslatableText;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import vazkii.quark.base.client.handler.TopLayerTooltipHandler;
import vazkii.quark.base.handler.MiscUtil;

public class MiniInventoryButton extends ButtonWidget {

	private final Consumer<List<String>> tooltip;
	private final int type;
	private final HandledScreen<?> parent;
	private final int startX;

	private BooleanSupplier shiftTexture = () -> false;

	public MiniInventoryButton(HandledScreen<?> parent, int type, int x, int y, Consumer<List<String>> tooltip, PressAction onPress) {
		super(parent.getGuiLeft() + x, parent.getGuiTop() + y, 10, 10, new LiteralText(""), onPress);
		this.parent = parent;
		this.type = type;
		this.tooltip = tooltip;
		this.startX = x;
	}

	public MiniInventoryButton(HandledScreen<?> parent, int type, int x, int y, String tooltip, PressAction onPress) {
		this(parent, type, x, y, (t) -> t.add(I18n.translate(tooltip)), onPress);
	}

	public MiniInventoryButton setTextureShift(BooleanSupplier func) {
		shiftTexture = func;
		return this;
	}

	@Override
	public void render(MatrixStack matrix, int p_render_1_, int p_render_2_, float p_render_3_) {
		if(parent instanceof RecipeBookProvider)
			x = parent.getGuiLeft() + startX;

		super.render(matrix, p_render_1_, p_render_2_, p_render_3_);
	}

	@Override
	public void renderButton(MatrixStack matrix, int mouseX, int mouseY, float pticks) {
		MinecraftClient mc = MinecraftClient.getInstance();
		mc.getTextureManager().bindTexture(MiscUtil.GENERAL_ICONS);
		RenderSystem.color4f(1.0F, 1.0F, 1.0F, alpha);
		RenderSystem.disableLighting();
		RenderSystem.enableBlend();
		RenderSystem.blendFuncSeparate(GlStateManager.SrcFactor.SRC_ALPHA, GlStateManager.DstFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SrcFactor.ONE, GlStateManager.DstFactor.ZERO);
		RenderSystem.blendFunc(GlStateManager.SrcFactor.SRC_ALPHA, GlStateManager.DstFactor.ONE_MINUS_SRC_ALPHA);

		int u = type * width;
		int v = 25 + (hovered ? height : 0);
		if(shiftTexture.getAsBoolean())
			v += (height * 2);

		drawTexture(matrix, x, y, u, v, width, height);

		if(hovered)
			TopLayerTooltipHandler.setTooltip(getTooltip(), mouseX, mouseY);
	}

	@Override
	protected MutableText getNarrationMessage() {
		List<String> tooltip = getTooltip();
		return tooltip.isEmpty() ? new LiteralText("") : new TranslatableText("gui.narrate.button", getTooltip().get(0));
	}

	public List<String> getTooltip() {
		List<String> list = new LinkedList<>();
		tooltip.accept(list);
		return list;
	}

}
