package vazkii.quark.addons.oddities.client.screen;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.systems.RenderSystem;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.widget.button.Button;
import net.minecraft.util.text.StringTextComponent;

public class MatrixEnchantingPlusButton extends Button {

	public MatrixEnchantingPlusButton(int x, int y, IPressable onPress) {
		super(x, y, 50, 12, new StringTextComponent(""), onPress);
	}
	
	@Override
	public void render(MatrixStack stack, int mouseX, int mouseY, float partialTicks) {
		boolean hovered = mouseX >= x && mouseY >= y && mouseX < x + width && mouseY < y + height;
		if(!visible)
			return;
		
		Minecraft.getInstance().textureManager.bindTexture(MatrixEnchantingScreen.BACKGROUND);
		int u = 0;
		int v = 177;
		
		if(!active)
			v += 12;
		else if(hovered)
			v += 24;

		RenderSystem.color3f(1F, 1F, 1F);
		blit(stack, x, y, u, v, width, height);
	}

}
