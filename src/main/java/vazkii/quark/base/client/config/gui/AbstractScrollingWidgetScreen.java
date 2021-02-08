package vazkii.quark.base.client.config.gui;

import java.util.LinkedList;
import java.util.List;

import org.lwjgl.opengl.GL11;

import com.mojang.blaze3d.matrix.MatrixStack;

import net.minecraft.client.MainWindow;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.Widget;
import net.minecraft.client.gui.widget.button.Button;
import net.minecraft.util.text.TranslationTextComponent;
import vazkii.quark.base.client.config.gui.widget.ScrollableWidgetList;

public abstract class AbstractScrollingWidgetScreen extends AbstractQScreen {

	private List<Widget> scrollingWidgets = new LinkedList<>();
	private ScrollableWidgetList<?, ?> elementList;
	
	private Button resetButton;
	
	private boolean needsScrollUpdate = false;
	private double currentScroll = 0;
	
	public AbstractScrollingWidgetScreen(Screen parent) {
		super(parent);
	}
	
	@Override
	protected void init() {
		super.init();

		elementList = createWidgetList();
		children.add(elementList);
		refresh();
		needsScrollUpdate = true;
		
		int pad = 3;
		int bWidth = 121;
		int left = (width - (bWidth + pad) * 3) / 2;
		int vStart = height - 30;
		
		addButton(new Button(left, vStart, bWidth, 20, new TranslationTextComponent("quark.gui.config.default"), this::onClickDefault));
		addButton(resetButton = new Button(left + bWidth + pad, vStart, bWidth, 20, new TranslationTextComponent("quark.gui.config.discard"), this::onClickDiscard));
		addButton(new Button(left + (bWidth + pad) * 2, vStart, bWidth, 20, new TranslationTextComponent("gui.done"), this::onClickDone));
	}
	
	@Override
	public void tick() {
		super.tick();
		
		resetButton.active = isDirty();
	}
	
	public void refresh() {
		children.removeIf(scrollingWidgets::contains);
		buttons.removeIf(scrollingWidgets::contains);
		scrollingWidgets.clear();
		
		elementList.populate(w -> {
			children.add(w);
			scrollingWidgets.add(w);
			if(w instanceof Button)
				addButton(w);
		});
	}
	
	public void render(MatrixStack mstack, int mouseX, int mouseY, float pticks) {
		if(needsScrollUpdate) {
			elementList.setScrollAmount(currentScroll);
			needsScrollUpdate = false;
		}
		
		currentScroll = elementList.getScrollAmount();
		
		scrollingWidgets.forEach(w -> w.visible = false);
		
		renderBackground(mstack);
		elementList.render(mstack, mouseX, mouseY, pticks);
		
		List<Widget> visibleWidgets = new LinkedList<>();
		scrollingWidgets.forEach(w -> {
			if(w.visible)
				visibleWidgets.add(w);
			w.visible = false;
		});
		
		super.render(mstack, mouseX, mouseY, pticks);
		
		MainWindow main = minecraft.getMainWindow();
		int res = (int) main.getGuiScaleFactor();
		
		GL11.glEnable(GL11.GL_SCISSOR_TEST);
		GL11.glScissor(0, 40 * res, width * res, (height - 80) * res);
		visibleWidgets.forEach(w -> {
			w.visible = true;
			w.render(mstack, mouseX, mouseY, pticks);
		});
		GL11.glDisable(GL11.GL_SCISSOR_TEST);
	}
	
	@Override
	public boolean mouseClicked(double x, double y, int button) {
		return super.mouseClicked(x, y, button);
	}
	
	protected abstract ScrollableWidgetList<?, ?> createWidgetList();
	protected abstract void onClickDefault(Button b);
	protected abstract void onClickDiscard(Button b);
	protected abstract boolean isDirty();
	
	protected void onClickDone(Button b) {
		returnToParent(b);
	}

}
