package vazkii.quark.base.client.screen;

import java.util.LinkedList;
import java.util.List;

import org.lwjgl.opengl.GL11;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.AbstractButtonWidget;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.util.Window;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.Formatting;
import vazkii.quark.base.client.config.ConfigCategory;

public class QCategoryScreen extends QScreen {

	public final ConfigCategory category;

	private List<AbstractButtonWidget> vigiledWidgets = new LinkedList<>();
	
	private ConfigElementList elementList;
	private String breadcrumbs;
	
	public QCategoryScreen(Screen parent, ConfigCategory category) {
		super(parent);
		this.category = category;
	}
	
	@Override
	protected void init() {
		super.init();
		
		breadcrumbs = category.getName();
		ConfigCategory currCategory = category.getParent();
		while(currCategory != null) {
			breadcrumbs = String.format("%s > %s", currCategory.getName(), breadcrumbs);
			currCategory = currCategory.getParent();
		}
		breadcrumbs = String.format("> %s", breadcrumbs);
		
		elementList = new ConfigElementList(this, w -> {
			children.add(w);
			vigiledWidgets.add(w);
			if(w instanceof ButtonWidget)
				addButton(w);
		});
		
		children.add(elementList);
		
		int pad = 3;
		int bWidth = 121;
		int left = (width - (bWidth + pad) * 3) / 2;
		int vStart = height - 30;
		
		addButton(new ButtonWidget(left, vStart, bWidth, 20, new TranslatableText("Set to Default"), b -> category.reset(true)));
		addButton(new ButtonWidget(left + bWidth + pad, vStart, bWidth, 20, new TranslatableText("Discard Changes"), b -> category.reset(false)));
		addButton(new ButtonWidget(left + (bWidth + pad) * 2, vStart, bWidth, 20, new TranslatableText("Done"), this::returnToParent));
	}
	
	@Override
	public void render(MatrixStack mstack, int mouseX, int mouseY, float pticks) {
		vigiledWidgets.forEach(w -> w.visible = false);
		
		renderBackground(mstack);
		elementList.render(mstack, mouseX, mouseY, pticks);
		
		List<AbstractButtonWidget> visibleWidgets = new LinkedList<>();
		vigiledWidgets.forEach(w -> {
			if(w.visible)
				visibleWidgets.add(w);
			w.visible = false;
		});
		
		super.render(mstack, mouseX, mouseY, pticks);
		
		GL11.glEnable(GL11.GL_SCISSOR_TEST);
		
		Window main = client.getWindow();
		int res = (int) main.getScaleFactor();

		GL11.glScissor(0, 40 * res, width * res, (height - 80) * res);
		for(AbstractButtonWidget w : visibleWidgets) {
			w.visible = true;
			w.render(mstack, mouseX, mouseY, pticks);
		}
		GL11.glDisable(GL11.GL_SCISSOR_TEST);
		
		int left = 20;
		textRenderer.draw(mstack, Formatting.BOLD + "Quark Configuration", left, 10, 0x48ddbc);
		textRenderer.draw(mstack, breadcrumbs, left, 20, 0xFFFFFF);
	}

}
