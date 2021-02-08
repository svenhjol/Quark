package vazkii.quark.base.client.config.gui;

import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.ButtonWidget.PressAction;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.LiteralText;
import net.minecraft.util.Util;
import vazkii.quark.api.config.IConfigCategory;
import vazkii.quark.base.client.config.obj.AbstractStringInputObject;
import vazkii.quark.base.client.config.obj.ListObject;

public abstract class AbstractQScreen extends Screen {
	
	private final Screen parent;
	
	public AbstractQScreen(Screen parent) {
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
	
	public PressAction categoryLink(IConfigCategory category) {
		return b -> client.openScreen(new CategoryScreen(this, category));
	}
	
	public <T> PressAction stringInput(AbstractStringInputObject<T> object) {
		return b -> client.openScreen(new StringInputScreen<T>(this, object));
	}
	
	public PressAction listInput(ListObject object) {
		return b -> client.openScreen(new ListInputScreen(this, object));
	}

}
