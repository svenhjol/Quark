package vazkii.quark.content.management.client.gui;

import java.util.LinkedList;
import java.util.List;
import java.util.function.BooleanSupplier;
import java.util.function.Consumer;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.recipebook.IRecipeShownListener;
import net.minecraft.client.gui.screen.inventory.ContainerScreen;
import net.minecraft.client.gui.widget.button.Button;
import net.minecraft.client.resources.I18n;
import net.minecraft.util.text.IFormattableTextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import vazkii.quark.base.client.handler.TopLayerTooltipHandler;
import vazkii.quark.base.handler.MiscUtil;

public class MiniInventoryButton extends Button {

	private final Consumer<List<String>> tooltip;
	private final int type;
	private final ContainerScreen<?> parent;
	private final int startX;

	private BooleanSupplier shiftTexture = () -> false;

	public MiniInventoryButton(ContainerScreen<?> parent, int type, int x, int y, Consumer<List<String>> tooltip, IPressable onPress) {
		super(parent.getGuiLeft() + x, parent.getGuiTop() + y, 10, 10, new StringTextComponent(""), onPress);
		this.parent = parent;
		this.type = type;
		this.tooltip = tooltip;
		this.startX = x;
	}

	public MiniInventoryButton(ContainerScreen<?> parent, int type, int x, int y, String tooltip, IPressable onPress) {
		this(parent, type, x, y, (t) -> t.add(I18n.format(tooltip)), onPress);
	}

	public MiniInventoryButton setTextureShift(BooleanSupplier func) {
		shiftTexture = func;
		return this;
	}

	@Override
	public void render(MatrixStack matrix, int p_render_1_, int p_render_2_, float p_render_3_) {
		if(parent instanceof IRecipeShownListener)
			x = parent.getGuiLeft() + startX;

		super.render(matrix, p_render_1_, p_render_2_, p_render_3_);
	}

	@Override
	public void renderButton(MatrixStack matrix, int mouseX, int mouseY, float pticks) {
		Minecraft mc = Minecraft.getInstance();
		mc.getTextureManager().bindTexture(MiscUtil.GENERAL_ICONS);
		RenderSystem.color4f(1.0F, 1.0F, 1.0F, alpha);
		RenderSystem.disableLighting();
		RenderSystem.enableBlend();
		RenderSystem.blendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);
		RenderSystem.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);

		int u = type * width;
		int v = 25 + (isHovered ? height : 0);
		if(shiftTexture.getAsBoolean())
			v += (height * 2);

		blit(matrix, x, y, u, v, width, height);

		if(isHovered)
			TopLayerTooltipHandler.setTooltip(getTooltip(), mouseX, mouseY);
	}

	@Override
	protected IFormattableTextComponent getNarrationMessage() {
		List<String> tooltip = getTooltip();
		return tooltip.isEmpty() ? new StringTextComponent("") : new TranslationTextComponent("gui.narrate.button", getTooltip().get(0));
	}

	public List<String> getTooltip() {
		List<String> list = new LinkedList<>();
		tooltip.accept(list);
		return list;
	}

}
