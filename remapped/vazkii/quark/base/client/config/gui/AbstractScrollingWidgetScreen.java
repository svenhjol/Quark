package vazkii.quark.base.client.config.gui;

import java.util.LinkedList;
import java.util.List;

import org.lwjgl.opengl.GL11;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.AbstractButtonWidget;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.util.Window;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.TranslatableText;
import vazkii.quark.base.client.config.gui.widget.ScrollableWidgetList;

public abstract class AbstractScrollingWidgetScreen extends AbstractQScreen {

	private List<AbstractButtonWidget> scrollingWidgets = new LinkedList<>();
	private ScrollableWidgetList<?, ?> elementList;
	
	private ButtonWidget resetButton;
	
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
		
		addButton(new ButtonWidget(left, vStart, bWidth, 20, new TranslatableText("quark.gui.config.default"), this::onClickDefault));
		addButton(resetButton = new ButtonWidget(left + bWidth + pad, vStart, bWidth, 20, new TranslatableText("quark.gui.config.discard"), this::onClickDiscard));
		addButton(new ButtonWidget(left + (bWidth + pad) * 2, vStart, bWidth, 20, new TranslatableText("gui.done"), this::onClickDone));
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
			if(w instanceof ButtonWidget)
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
		
		List<AbstractButtonWidget> visibleWidgets = new LinkedList<>();
		scrollingWidgets.forEach(w -> {
			if(w.visible)
				visibleWidgets.add(w);
			w.visible = false;
		});
		
		super.render(mstack, mouseX, mouseY, pticks);
		
		Window main = client.getWindow();
		int res = (int) main.getScaleFactor();
		
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
	protected abstract void onClickDefault(ButtonWidget b);
	protected abstract void onClickDiscard(ButtonWidget b);
	protected abstract boolean isDirty();
	
	protected void onClickDone(ButtonWidget b) {
		returnToParent(b);
	}

}
