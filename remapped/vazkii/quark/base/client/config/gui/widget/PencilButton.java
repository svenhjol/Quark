package vazkii.quark.base.client.config.gui.widget;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.LiteralText;
import vazkii.quark.base.handler.ContributorRewardHandler;
import vazkii.quark.base.handler.MiscUtil;

public class PencilButton extends ButtonWidget {

	public PencilButton(int x, int y, PressAction pressable) {
		super(x, y, 20, 20, new LiteralText(""), pressable);
	}
	
	@Override
	public void renderButton(MatrixStack mstack, int p_renderButton_1_, int p_renderButton_2_, float p_renderButton_3_) {
		super.renderButton(mstack, p_renderButton_1_, p_renderButton_2_, p_renderButton_3_);
		
		if(ContributorRewardHandler.localPatronTier > 0) {
			RenderSystem.color3f(1F, 1F, 1F);
			int u = 32;
			int v = 93;
			
			MinecraftClient.getInstance().textureManager.bindTexture(MiscUtil.GENERAL_ICONS);
			drawTexture(mstack, x + 2, y + 1, u, v, 16, 16);
		}
	}
	
}
