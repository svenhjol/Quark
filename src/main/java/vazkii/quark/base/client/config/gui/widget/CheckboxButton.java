package vazkii.quark.base.client.config.gui.widget;

import java.util.function.Supplier;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.systems.RenderSystem;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.widget.button.Button;
import net.minecraft.util.text.StringTextComponent;
import vazkii.quark.api.config.IConfigObject;
import vazkii.quark.base.handler.ContributorRewardHandler;
import vazkii.quark.base.handler.MiscUtil;

public class CheckboxButton extends Button {

	private final Supplier<Boolean> checkedSupplier;
	
	public CheckboxButton(int x, int y, Supplier<Boolean> checkedSupplier, IPressable onClick) {
		super(x, y, 20, 20, new StringTextComponent(""), onClick);
		this.checkedSupplier = checkedSupplier;
	}
	
	public CheckboxButton(int x, int y, IConfigObject<Boolean> configObj) {
		this(x, y, () -> configObj.getCurrentObj(), (b) -> configObj.setCurrentObj(!configObj.getCurrentObj()));
	}
	
	@Override
	public void renderButton(MatrixStack mstack, int p_renderButton_1_, int p_renderButton_2_, float p_renderButton_3_) {
		super.renderButton(mstack, p_renderButton_1_, p_renderButton_2_, p_renderButton_3_);
		
		if(ContributorRewardHandler.localPatronTier > 0) {
			RenderSystem.color3f(1F, 1F, 1F);
			boolean enabled = checkedSupplier.get() && active;
			int u = enabled ? 0 : 16;
			int v = 93;
			
			Minecraft.getInstance().textureManager.bindTexture(MiscUtil.GENERAL_ICONS);
			blit(mstack, x + 2, y + 1, u, v, 16, 16);
		}
	}

}
