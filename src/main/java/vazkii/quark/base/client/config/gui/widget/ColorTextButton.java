package vazkii.quark.base.client.config.gui.widget;

import net.minecraft.client.gui.widget.button.Button;
import net.minecraft.util.text.ITextComponent;

public class ColorTextButton extends Button {

	private final int textColor;
	
	public ColorTextButton(int x, int y, int w, int h, ITextComponent text, int textColor, IPressable onClick) {
		super(x, y, w, h, text, onClick);
		this.textColor = textColor;
	}
	
	@Override
	public int getFGColor() {
		return textColor;
	}

}
