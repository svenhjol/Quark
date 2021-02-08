package vazkii.quark.base.client.config;

import java.io.PrintStream;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.function.Predicate;
import java.util.function.Supplier;

import org.apache.commons.lang3.text.WordUtils;

import net.minecraft.client.resources.I18n;
import vazkii.quark.api.config.IConfigCategory;
import vazkii.quark.api.config.IConfigElement;
import vazkii.quark.api.config.IConfigObject;
import vazkii.quark.base.client.config.external.ExternalCategory;
import vazkii.quark.base.client.config.gui.CategoryScreen;
import vazkii.quark.base.client.config.gui.WidgetWrapper;
import vazkii.quark.base.client.config.gui.widget.IWidgetProvider;
import vazkii.quark.base.client.config.gui.widget.PencilButton;

public class ConfigCategory extends AbstractConfigElement implements IConfigCategory, IWidgetProvider {

	public final List<IConfigElement> subElements = new LinkedList<>();
	
	private final String path;
	private final int depth;
	private boolean dirty = false;
	
	public ConfigCategory(String name, String comment, IConfigCategory parent) {
		super(name, comment, parent);
		
		if(parent == null || (parent instanceof ExternalCategory)) {
			path = name;
			depth = 0;
		} else {
			path = String.format("%s.%s", parent.getPath(), name);
			depth = 1 + parent.getDepth();
		}
	}
	
	@Override
	public String getPath() {
		return path;
	}
	
	@Override
	public int getDepth() {
		return depth;
	}
	
	@Override
	public List<IConfigElement> getSubElements() {
		return subElements;
	}
	
	@Override
	@SuppressWarnings("deprecation")
	public String getGuiDisplayName() {
		return WordUtils.capitalize(getName().replaceAll("_", " "));
	}

	@Override
	public void updateDirty() {
		dirty = false;
		for(IConfigElement sub : subElements)
			if(sub.isDirty()) {
				dirty = true;
				break;
			}
	
		if(parent != null)
			parent.updateDirty();
	}

	@Override
	public void clean() {
		subElements.forEach(IConfigElement::clean);
		dirty = false;
	}
	
	@Override
	public boolean isDirty() {
		return dirty;
	}
	
	@Override
	public void refresh() {
		subElements.forEach(IConfigElement::refresh);
	}
	
	@Override
	public void reset(boolean hard) {
		subElements.forEach(e -> e.reset(hard));		
	}
	
	@Override
	public IConfigCategory addCategory(String name, String comment) {
		return addCategory(new ConfigCategory(name, comment, this));
	}
	
	public IConfigCategory addCategory(IConfigCategory category) {
		subElements.add(category);
		return category;
	}
	
	@Override
	public <T> IConfigObject<T> addEntry(String name, T default_, Supplier<T> getter, String comment, Predicate<Object> restriction) {
		IConfigObject<T> obj = ConfigObject.create(name, comment, default_, getter, restriction, this); 
		addEntry(obj, default_);
		return obj;
	}
	
	public <T> void addEntry(IConfigObject<T> obj, T default_) {
		subElements.add(obj);
	}
	
	@Override
	public void close() {
		subElements.removeIf(e -> e instanceof ConfigCategory && ((ConfigCategory) e).subElements.isEmpty());
		Collections.sort(subElements);
	}

	@Override
	public void addWidgets(CategoryScreen parent, List<WidgetWrapper> widgets) {
		widgets.add(new WidgetWrapper(new PencilButton(230, 3, parent.categoryLink(this))));
	}

	@Override
	public void print(String pad, PrintStream stream) {
		stream.println();
		super.print(pad, stream);
		stream.printf("%s[%s]%n", pad, path);
		
		final String newPad = String.format("\t%s", pad);
		subElements.forEach(e -> e.print(newPad, stream));
	}
	
	@Override
	public String getSubtitle() {
		int size = subElements.size();
		return size == 1 ? I18n.format("quark.gui.config.onechild") : I18n.format("quark.gui.config.nchildren", subElements.size());
	}

	@Override
	public int compareTo(IConfigElement o) {
		if(o == this)
			return 0;
		if(!(o instanceof ConfigCategory))
			return 1;
		
		return name.compareTo(((ConfigCategory) o).name);
	}

}
