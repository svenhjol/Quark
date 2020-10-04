package vazkii.quark.base.client.screen;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.function.Consumer;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.widget.AbstractButtonWidget;
import net.minecraft.client.gui.widget.AlwaysSelectedEntryListWidget;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Formatting;
import vazkii.quark.base.client.TopLayerTooltipHandler;
import vazkii.quark.base.client.config.ConfigCategory;
import vazkii.quark.base.client.config.ConfigObject;
import vazkii.quark.base.client.config.IConfigElement;

public class ConfigElementList extends AlwaysSelectedEntryListWidget<ConfigElementList.Entry> {

	private final QCategoryScreen parent;

	public ConfigElementList(QCategoryScreen parent, Consumer<AbstractButtonWidget> widgetConsumer) {
		super(MinecraftClient.getInstance(), parent.width, parent.height, 40, parent.height - 40, 30);
		this.parent = parent;
		
		populate(widgetConsumer);
	}
	
	private void populate(Consumer<AbstractButtonWidget> widgetConsumer) {
		boolean isObject = true;
		for(IConfigElement elm : parent.category.subElements) {
			boolean wasObject = isObject;
			isObject = elm instanceof ConfigObject;
			
			if(wasObject && !isObject)
				addEntry(new vazkii.quark.base.client.screen.ConfigElementList.Entry(parent, null)); // separator
			
			vazkii.quark.base.client.screen.ConfigElementList.Entry entry = new vazkii.quark.base.client.screen.ConfigElementList.Entry(parent, elm); 
			addEntry(entry);
			entry.commitWidgets(widgetConsumer);
		}
	}

	@Override
	protected int getScrollbarPositionX() {
		return super.getScrollbarPositionX() + 20;
	}

	@Override
	public int getRowWidth() {
		return super.getRowWidth() + 50;
	}

	@Override
	protected boolean isFocused() {
		return false;
	}

	public static final class Entry extends AlwaysSelectedEntryListWidget.Entry<vazkii.quark.base.client.screen.ConfigElementList.Entry> {

		private final IConfigElement element;
		
		private List<WidgetWrapper> children = new ArrayList<>();

		public Entry(QCategoryScreen parent, IConfigElement element) {
			this.element = element;
			
			if(element != null)
				element.addWidgets(parent, children);
		}
		
		public void commitWidgets(Consumer<AbstractButtonWidget> consumer) {
			children.stream().map(c -> c.widget).forEach(consumer);
		}
		
		@Override
		public void render(MatrixStack mstack, int index, int rowTop, int rowLeft, int rowWidth, int rowHeight, int mouseX, int mouseY, boolean hovered, float pticks) {
			MinecraftClient mc = MinecraftClient.getInstance();
			
			if(element != null) {
				int effIndex = index + 1;
				if(element instanceof ConfigCategory)
					effIndex--; // compensate for the divider
				
				if(effIndex % 2 == 0)
					fill(mstack, rowLeft, rowTop, rowLeft + rowWidth, rowTop + rowHeight, 0x66000000);

				int left = rowLeft + 10;
				int top = rowTop + 4;
				
				String name = element.getGuiDisplayName();
				if(element.isDirty())
					name += Formatting.GOLD + "*";
				
				
				int len = mc.textRenderer.getWidth(name);
				int maxLen = rowWidth - 85;
				String originalName = null;
				if(len > maxLen) {
					originalName = name;
					do {
						name = name.substring(0, name.length() - 1);
						len = mc.textRenderer.getWidth(name);
					} while(len > maxLen);
					
					name += "...";
				}
				
				List<String> tooltip = element.getTooltip();
				if(originalName != null) {
					if(tooltip == null) {
						tooltip = new LinkedList<>();
						tooltip.add(originalName);
					} else {
						tooltip.add(0, "");
						tooltip.add(0, originalName);
					}
				}
				
				if(tooltip != null) {
					int hoverLeft = left + mc.textRenderer.getWidth(name + " ");
					int hoverRight = hoverLeft + mc.textRenderer.getWidth("(?)");
					
					name += (Formatting.AQUA + " (?)");
					if(mouseX >= hoverLeft && mouseX < hoverRight && mouseY >= top && mouseY < (top + 10))
						TopLayerTooltipHandler.setTooltip(tooltip, mouseX, mouseY);
				}
				
				mc.textRenderer.drawWithShadow(mstack, name, left, top, 0xFFFFFF);
				mc.textRenderer.drawWithShadow(mstack, element.getSubtitle(), left, top + 10, 0x999999);
				
				children.forEach(c -> c.updatePosition(rowLeft, rowTop));
				

			} else {
				String s = "------- Sub Categories -------";
				mc.textRenderer.drawWithShadow(mstack, s, rowLeft + (rowWidth - mc.textRenderer.getWidth(s)) / 2, rowTop + 7, 0x6666FF);
			}
		}

	}

}
