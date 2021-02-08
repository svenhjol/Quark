package vazkii.quark.base.client.config.gui.widget;

import java.util.LinkedList;
import java.util.List;

import com.mojang.blaze3d.matrix.MatrixStack;

import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.I18n;
import net.minecraft.util.text.TextFormatting;
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
				addEntry(new Entry<T>(parent, null)); // separator
			
			Entry<T> entry = new Entry<T>(parent, (T) elm); 
			addEntry(entry);
			
			hadObjects = hadObjects || isObject;
		}		
	}

	public static final class Entry<T extends IConfigElement & IWidgetProvider> extends ScrollableWidgetList.Entry<Entry<T>> {

		private final IConfigElement element;

		public Entry(CategoryScreen parent, T element) {
			this.element = element;
			
			if(element != null)
				element.addWidgets(parent, children);
		}
		
		@Override
		public void render(MatrixStack mstack, int index, int rowTop, int rowLeft, int rowWidth, int rowHeight, int mouseX, int mouseY, boolean hovered, float pticks) {
			super.render(mstack, index, rowTop, rowLeft, rowWidth, rowHeight, mouseX, mouseY, hovered, pticks);
			
			Minecraft mc = Minecraft.getInstance();
			
			if(element != null) {
				int left = rowLeft + 10;
				int top = rowTop + 4;
				
				int effIndex = index + 1;
				if(element instanceof ConfigCategory)
					effIndex--; // compensate for the divider
				drawBackground(mstack, effIndex, rowTop, rowLeft, rowWidth, rowHeight, mouseX, mouseY, hovered);
				
				String name = element.getGuiDisplayName();
				if(element.isDirty())
					name += TextFormatting.GOLD + "*";
				
				int len = mc.fontRenderer.getStringWidth(name);
				int maxLen = rowWidth - 85;
				String originalName = null;
				if(len > maxLen) {
					originalName = name;
					do {
						name = name.substring(0, name.length() - 1);
						len = mc.fontRenderer.getStringWidth(name);
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
					int hoverLeft = left + mc.fontRenderer.getStringWidth(name + " ");
					int hoverRight = hoverLeft + mc.fontRenderer.getStringWidth("(?)");
					
					name += (TextFormatting.AQUA + " (?)");
					if(mouseX >= hoverLeft && mouseX < hoverRight && mouseY >= top && mouseY < (top + 10))
						TopLayerTooltipHandler.setTooltip(tooltip, mouseX, mouseY);
				}
				
				mc.fontRenderer.drawStringWithShadow(mstack, name, left, top, 0xFFFFFF);
				mc.fontRenderer.drawStringWithShadow(mstack, element.getSubtitle(), left, top + 10, 0x999999);
			} else {
				String s = I18n.format("quark.gui.config.subcategories");
				mc.fontRenderer.drawStringWithShadow(mstack, s, rowLeft + (rowWidth - mc.fontRenderer.getStringWidth(s)) / 2, rowTop + 7, 0x6666FF);
			}
		}

	}

}
