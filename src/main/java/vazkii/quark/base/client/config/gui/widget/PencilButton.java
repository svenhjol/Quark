package vazkii.quark.base.client.config.gui.widget;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.systems.RenderSystem;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.widget.button.Button;
import net.minecraft.util.text.StringTextComponent;
import vazkii.quark.base.handler.ContributorRewardHandler;
import vazkii.quark.base.handler.MiscUtil;

public class PencilButton extends Button {

	public PencilButton(int x, int y, IPressable pressable) {
		super(x, y, 20, 20, new StringTextComponent(""), pressable);
	}
	
	@Override
	public void renderButton(MatrixStack mstack, int p_renderButton_1_, int p_renderButton_2_, float p_renderButton_3_) {
		super.renderButton(mstack, p_renderButton_1_, p_renderButton_2_, p_renderButton_3_);
		
		if(ContributorRewardHandler.localPatronTier > 0) {
			RenderSystem.color3f(1F, 1F, 1F);
			int u = 32;
			int v = 93;
			
			Minecraft.getInstance().textureManager.bindTexture(MiscUtil.GENERAL_ICONS);
			blit(mstack, x + 2, y + 1, u, v, 16, 16);
		}
	}
	
}
