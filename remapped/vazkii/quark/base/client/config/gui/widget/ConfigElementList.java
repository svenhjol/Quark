package vazkii.quark.base.client.config.gui.widget;

import java.util.LinkedList;
import java.util.List;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.resource.language.I18n;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Formatting;
import vazkii.quark.api.config.IConfigElement;
import vazkii.quark.api.config.IConfigObject;
import vazkii.quark.base.client.config.ConfigCategory;
import vazkii.quark.base.client.config.gui.CategoryScreen;
import vazkii.quark.base.client.handler.TopLayerTooltipHandler;

public class ConfigElementList<T extends IConfigElement & IWidgetProvider> extends ScrollableWidgetList<CategoryScreen, ConfigElementList.Entry<T>> {

	public ConfigElementList(CategoryScreen parent) {
		super(parent);
	}

	@Override
	@SuppressWarnings("unchecked")
	protected void findEntries() {
		boolean hadObjects = false;
		boolean isObject = true;
		for(IConfigElement elm : parent.category.getSubElements()) {
			boolean wasObject = isObject;
			isObject = elm instanceof IConfigObject;
			
			if(wasObject && !isObject && hadObjects)
				addEntry(new vazkii.quark.base.client.config.gui.widget.ConfigElementList.Entry<T>(parent, null)); // separator
			
			vazkii.quark.base.client.config.gui.widget.ConfigElementList.Entry<T> entry = new vazkii.quark.base.client.config.gui.widget.ConfigElementList.Entry<T>(parent, (T) elm); 
			addEntry(entry);
			
			hadObjects = hadObjects || isObject;
		}		
	}

	public static final class Entry<T extends IConfigElement & IWidgetProvider> extends ScrollableWidgetList.Entry<vazkii.quark.base.client.config.gui.widget.ConfigElementList.Entry<T>> {

		private final IConfigElement element;

		public Entry(CategoryScreen parent, T element) {
			this.element = element;
			
			if(element != null)
				element.addWidgets(parent, children);
		}
		
		@Override
		public void render(MatrixStack mstack, int index, int rowTop, int rowLeft, int rowWidth, int rowHeight, int mouseX, int mouseY, boolean hovered, float pticks) {
			super.render(mstack, index, rowTop, rowLeft, rowWidth, rowHeight, mouseX, mouseY, hovered, pticks);
			
			MinecraftClient mc = MinecraftClient.getInstance();
			
			if(element != null) {
				int left = rowLeft + 10;
				int top = rowTop + 4;
				
				int effIndex = index + 1;
				if(element instanceof ConfigCategory)
					effIndex--; // compensate for the divider
				drawBackground(mstack, effIndex, rowTop, rowLeft, rowWidth, rowHeight, mouseX, mouseY, hovered);
				
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
			} else {
				String s = I18n.translate("quark.gui.config.subcategories");
				mc.textRenderer.drawWithShadow(mstack, s, rowLeft + (rowWidth - mc.textRenderer.getWidth(s)) / 2, rowTop + 7, 0x6666FF);
			}
		}

	}

}
