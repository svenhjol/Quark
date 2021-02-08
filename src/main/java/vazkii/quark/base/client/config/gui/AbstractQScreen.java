package vazkii.quark.base.client.config.gui;

import com.mojang.blaze3d.matrix.MatrixStack;

import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.button.Button;
import net.minecraft.client.gui.widget.button.Button.IPressable;
import net.minecraft.util.Util;
import net.minecraft.util.text.StringTextComponent;
import vazkii.quark.api.config.IConfigCategory;
import vazkii.quark.base.client.config.obj.AbstractStringInputObject;
import vazkii.quark.base.client.config.obj.ListObject;

public abstract class AbstractQScreen extends Screen {
	
	private final Screen parent;
	
	public AbstractQScreen(Screen parent) {
		super(new StringTextComponent(""));
		this.parent = parent;
	}
	
	@Override
	public void render(MatrixStack mstack, int mouseX, int mouseY, float pticks) {
		super.render(mstack, mouseX, mouseY, pticks);
	}
	
	public void returnToParent(Button button) {
		minecraft.displayGuiScreen(parent);
	}
	
	public IPressable webLink(String url) {
		return b -> Util.getOSType().openURI(url);
	}
	
	public IPressable categoryLink(IConfigCategory category) {
		return b -> minecraft.displayGuiScreen(new CategoryScreen(this, category));
	}
	
	public <T> IPressable stringInput(AbstractStringInputObject<T> object) {
		return b -> minecraft.displayGuiScreen(new StringInputScreen<T>(this, object));
	}
	
	public IPressable listInput(ListObject object) {
		return b -> minecraft.displayGuiScreen(new ListInputScreen(this, object));
	}

}
