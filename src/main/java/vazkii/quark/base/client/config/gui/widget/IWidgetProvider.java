package vazkii.quark.base.client.config.gui.widget;

import java.util.List;

import vazkii.quark.base.client.config.gui.CategoryScreen;
import vazkii.quark.base.client.config.gui.WidgetWrapper;

public interface IWidgetProvider {

	public void addWidgets(CategoryScreen parent, List<WidgetWrapper> widgets);
	
}
