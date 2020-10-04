package vazkii.quark.base.client.screen;

import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.ButtonWidget.PressAction;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.LiteralText;
import net.minecraft.util.Util;
import vazkii.quark.base.client.config.ConfigCategory;

public abstract class QScreen extends Screen {
	
	private final Screen parent;
	
	public QScreen(Screen parent) {
		super(new LiteralText(""));
		this.parent = parent;
	}
	
	@Override
	public void render(MatrixStack mstack, int mouseX, int mouseY, float pticks) {
		super.render(mstack, mouseX, mouseY, pticks);
	}
	
	public void returnToParent(ButtonWidget button) {
		client.openScreen(parent);
	}
	
	public PressAction webLink(String url) {
		return b -> Util.getOperatingSystem().open(url);
	}
	
	public PressAction categoryLink(ConfigCategory category) {
		return b -> client.openScreen(new QCategoryScreen(this, category));
	}
	

}
