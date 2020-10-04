package vazkii.quark.oddities.client.screen;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.LiteralText;

public class MatrixEnchantingPlusButton extends ButtonWidget {

	public MatrixEnchantingPlusButton(int x, int y, PressAction onPress) {
		super(x, y, 50, 12, new LiteralText(""), onPress);
	}
	
	@Override
	public void render(MatrixStack stack, int mouseX, int mouseY, float partialTicks) {
		boolean hovered = mouseX >= x && mouseY >= y && mouseX < x + width && mouseY < y + height;
		if(!visible)
			return;
		
		MinecraftClient.getInstance().textureManager.bindTexture(MatrixEnchantingScreen.BACKGROUND);
		int u = 0;
		int v = 177;
		
		if(!active)
			v += 12;
		else if(hovered)
			v += 24;

		RenderSystem.color3f(1F, 1F, 1F);
		drawTexture(stack, x, y, u, v, width, height);
	}

}
