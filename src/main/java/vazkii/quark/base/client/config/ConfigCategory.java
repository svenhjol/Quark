package vazkii.quark.base.client.config;

import java.io.PrintStream;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;
import java.util.function.Supplier;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.LiteralText;
import org.apache.commons.lang3.text.WordUtils;
import vazkii.quark.base.client.screen.CheckboxButton;
import vazkii.quark.base.client.screen.QCategoryScreen;
import vazkii.quark.base.client.screen.WidgetWrapper;
import vazkii.quark.base.module.ModuleCategory;

public class ConfigCategory extends AbstractConfigElement {

	public final List<IConfigElement> subElements = new LinkedList<>();
	
	private Map<String, ConfigObject<Boolean>> moduleOptions = new HashMap<>();
	
	private final String path;
	private final int depth;
	private boolean dirty = false;
	
	public ConfigCategory(String name, String comment, ConfigCategory parent) {
		super(name, comment, parent);
		
		if(parent == null) {
			path = name;
			depth = 0;
		} else {
			path = String.format("%s.%s", parent.path, name);
			depth = 1 + parent.depth;
		}
	}
	
	@Override
	@SuppressWarnings("deprecation")
	public String getGuiDisplayName() {
		return WordUtils.capitalize(getName().replaceAll("_", " "));
	}

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
	
	public ConfigCategory addCategory(String name, String comment) {
		ConfigCategory newCategory = new ConfigCategory(name, comment, this);
		subElements.add(newCategory);
		return newCategory;
	}
	
	@SuppressWarnings("unchecked")
	public <T> void addObject(String name, T default_, Supplier<T> getter, String comment, Predicate<Object> restriction) {
		ConfigObject<T> obj = new ConfigObject<T>(name, comment, default_, getter, this); 
		subElements.add(obj);
		
		if(parent == null && default_ instanceof Boolean)
			moduleOptions.put(name, (ConfigObject<Boolean>) obj);
	}
	
	@SuppressWarnings("deprecation")
	public ConfigObject<Boolean> getModuleOption(ModuleCategory category) {
		return moduleOptions.get(WordUtils.capitalizeFully(category.name));
	}
	
	public void close() {
		subElements.removeIf(e -> e instanceof ConfigCategory && ((ConfigCategory) e).subElements.isEmpty());
		Collections.sort(subElements);
	}

	@Override
	public void addWidgets(QCategoryScreen parent, List<WidgetWrapper> widgets) {
		widgets.add(new WidgetWrapper(new ButtonWidget(230, 3, 20, 20, new LiteralText("->"), parent.categoryLink(this))));
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
		return size == 1 ? "1 child" : String.format("%d children", subElements.size());
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
