package vazkii.quark.base.client.config.gui.widget;

import java.util.function.Supplier;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.LiteralText;
import com.mojang.blaze3d.systems.RenderSystem;
import vazkii.quark.api.config.IConfigObject;
import vazkii.quark.base.handler.ContributorRewardHandler;
import vazkii.quark.base.handler.MiscUtil;

public class CheckboxButton extends ButtonWidget {

	private final Supplier<Boolean> checkedSupplier;
	
	public CheckboxButton(int x, int y, Supplier<Boolean> checkedSupplier, PressAction onClick) {
		super(x, y, 20, 20, new LiteralText(""), onClick);
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
			
			MinecraftClient.getInstance().textureManager.bindTexture(MiscUtil.GENERAL_ICONS);
			drawTexture(mstack, x + 2, y + 1, u, v, 16, 16);
		}
	}

}
