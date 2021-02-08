package vazkii.quark.content.tweaks.client.gui;

import com.mojang.blaze3d.matrix.MatrixStack;

import net.minecraft.client.gui.widget.button.Button;
import net.minecraft.util.text.ITextComponent;

public class TranslucentButton extends Button {

	public TranslucentButton(int xIn, int yIn, int widthIn, int heightIn, ITextComponent text, IPressable onPress) {
		super(xIn, yIn, widthIn, heightIn, text, onPress);
	}
	
	@Override
	public void blit(MatrixStack stack, int x, int y, int textureX, int textureY, int width, int height) {
		fill(stack, x, y, x + width, y + height, Integer.MIN_VALUE);
	}

}
