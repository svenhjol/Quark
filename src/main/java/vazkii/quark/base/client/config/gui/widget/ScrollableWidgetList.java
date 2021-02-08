package vazkii.quark.base.client.config.gui.widget;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import com.mojang.blaze3d.matrix.MatrixStack;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.widget.Widget;
import net.minecraft.client.gui.widget.list.ExtendedList;
import vazkii.quark.base.client.config.ConfigCategory;
import vazkii.quark.base.client.config.gui.AbstractScrollingWidgetScreen;
import vazkii.quark.base.client.config.gui.WidgetWrapper;

public abstract class ScrollableWidgetList<S extends AbstractScrollingWidgetScreen, E extends ScrollableWidgetList.Entry<E>> extends ExtendedList<E> {

	public final S parent;
	
	public ScrollableWidgetList(S parent) {
		super(Minecraft.getInstance(), parent.width, parent.height, 40, parent.height - 40, 30);
		this.parent = parent;
	}
	
	public void populate(Consumer<Widget> widgetConsumer) {
		List<E> children = getEventListeners();
		children.clear();

		findEntries();
		for(E e : children)
			e.commitWidgets(widgetConsumer);
	}
	
	protected abstract void findEntries();

	@Override
	protected int getScrollbarPosition() {
		return super.getScrollbarPosition() + 20;
	}

	@Override
	public int getRowWidth() {
		return super.getRowWidth() + 50;
	}

	@Override
	protected boolean isFocused() {
		return false;
	}
	
	public static abstract class Entry<E extends Entry<E>> extends ExtendedList.AbstractListEntry<E> {
		
		public List<WidgetWrapper> children = new ArrayList<>();

		public final void commitWidgets(Consumer<Widget> consumer) {
			children.stream().map(c -> c.widget).forEach(consumer);
		}
		
		@Override
		public void render(MatrixStack mstack, int index, int rowTop, int rowLeft, int rowWidth, int rowHeight, int mouseX, int mouseY, boolean hovered, float pticks) {
			children.forEach(c -> c.updatePosition(rowLeft, rowTop));
		}
		
		public void drawBackground(MatrixStack mstack, int index, int rowTop, int rowLeft, int rowWidth, int rowHeight, int mouseX, int mouseY, boolean hovered) {
			if(index % 2 == 0)
				fill(mstack, rowLeft, rowTop, rowLeft + rowWidth, rowTop + rowHeight, 0x66000000);
			
			if(hovered) {
				fill(mstack, rowLeft, rowTop, rowLeft + 1, rowTop + rowHeight, 0xFFFFFFFF);
				fill(mstack, rowLeft + rowWidth - 1, rowTop, rowLeft + rowWidth, rowTop + rowHeight, 0xFFFFFFFF);
				
				fill(mstack, rowLeft, rowTop, rowLeft + rowWidth, rowTop + 1, 0xFFFFFFFF);
				fill(mstack, rowLeft, rowTop + rowHeight - 1, rowLeft + rowWidth, rowTop + rowHeight, 0xFFFFFFFF);
			}
		}
		
	}

}
