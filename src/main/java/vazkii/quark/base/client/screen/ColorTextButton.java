package vazkii.quark.base.client.screen;

import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.Text;

public class ColorTextButton extends ButtonWidget {

	private final int textColor;
	
	public ColorTextButton(int x, int y, int w, int h, Text text, int textColor, PressAction onClick) {
		super(x, y, w, h, text, onClick);
		this.textColor = textColor;
	}
	
	@Override
	public int getFGColor() {
		return textColor;
	}

}
