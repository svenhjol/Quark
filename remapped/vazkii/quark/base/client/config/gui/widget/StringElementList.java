package vazkii.quark.base.client.config.gui.widget;

import java.util.List;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.LiteralText;
import net.minecraft.util.Formatting;
import vazkii.quark.base.client.config.gui.ListInputScreen;
import vazkii.quark.base.client.config.gui.WidgetWrapper;

public class StringElementList extends ScrollableWidgetList<ListInputScreen, StringElementList.Entry>{

	public StringElementList(ListInputScreen parent) {
		super(parent);
	}
	
	@Override
	protected void findEntries() {
		int i = 0;
		for(String s : parent.list) {
			addEntry(new vazkii.quark.base.client.config.gui.widget.StringElementList.Entry(parent, s, i));
			i++;
		}
		
		addEntry(new vazkii.quark.base.client.config.gui.widget.StringElementList.Entry(parent, null, 0));
	}

	public static final class Entry extends ScrollableWidgetList.Entry<vazkii.quark.base.client.config.gui.widget.StringElementList.Entry> {
		
		public final String initialString;
		
		public String string;
		
		public Entry(ListInputScreen parent, String s, int index) {
			initialString = string = s;
			
			if(s != null) {
				MinecraftClient mc = MinecraftClient.getInstance();
				TextFieldWidget field = new TextFieldWidget(mc.textRenderer, 10, 3, 210, 20, new LiteralText(""));
				field.setMaxLength(256);
				field.setText(initialString);
				field.setCursor(0);
				field.setChangedListener(str -> parent.list.set(index, str));
				children.add(new WidgetWrapper(field));
				
				children.add(new WidgetWrapper(new ButtonWidget(230, 3, 20, 20, new LiteralText("-").formatted(Formatting.RED), b -> parent.remove(index)))); 
			} else {
				children.add(new WidgetWrapper(new ButtonWidget(10, 3, 20, 20, new LiteralText("+").formatted(Formatting.GREEN), b -> parent.addNew())));
			}
		}
		
		@Override
		public void render(MatrixStack mstack, int index, int rowTop, int rowLeft, int rowWidth, int rowHeight, int mouseX, int mouseY, boolean hovered, float pticks) {
			super.render(mstack, index, rowTop, rowLeft, rowWidth, rowHeight, mouseX, mouseY, hovered, pticks);
			
			if(initialString != null)
				drawBackground(mstack, index, rowTop, rowLeft, rowWidth, rowHeight, mouseX, mouseY, hovered);
		}
		
	}

	
}
