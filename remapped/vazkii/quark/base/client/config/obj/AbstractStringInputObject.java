package vazkii.quark.base.client.config.obj;

import java.util.List;
import java.util.function.Predicate;
import java.util.function.Supplier;

import javax.annotation.Nullable;

import vazkii.quark.base.client.config.ConfigCategory;
import vazkii.quark.base.client.config.ConfigObject;
import vazkii.quark.base.client.config.gui.CategoryScreen;
import vazkii.quark.base.client.config.gui.WidgetWrapper;
import vazkii.quark.base.client.config.gui.widget.PencilButton;

public abstract class AbstractStringInputObject<T> extends ConfigObject<T> {

	public AbstractStringInputObject(String name, String comment, T defaultObj, Supplier<T> objGetter, Predicate<Object> restriction, ConfigCategory parent) {
		super(name, comment, defaultObj, objGetter, restriction, parent);
	}

	@Override
	public void addWidgets(CategoryScreen parent, List<WidgetWrapper> widgets) {
		widgets.add(new WidgetWrapper(new PencilButton(230, 3, parent.stringInput(this))));
	}
	
	public abstract @Nullable T fromString(String s);
	public abstract @Nullable boolean isStringValid(String s);
	public abstract int getMaxStringLength();

}
