package vazkii.quark.base.client.config.gui;

import net.minecraft.client.gui.widget.Widget;

public class WidgetWrapper {

	public final Widget widget;
	public final int relativeX, relativeY;
	
	public WidgetWrapper(Widget widget) {
		this.widget = widget;
		this.relativeX = widget.x;
		this.relativeY = widget.y;
	}
	
	public void updatePosition(int currX, int currY) {
		widget.x = currX + relativeX;
		widget.y = currY + relativeY;
		widget.visible = true;
	}
	
}
