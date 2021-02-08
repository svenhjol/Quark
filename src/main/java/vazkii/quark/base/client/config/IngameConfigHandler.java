package vazkii.quark.base.client.config;

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Predicate;
import java.util.function.Supplier;

import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import vazkii.quark.api.config.IConfigCategory;
import vazkii.quark.api.config.IConfigElement;
import vazkii.quark.api.config.IConfigObject;
import vazkii.quark.base.Quark;
import vazkii.quark.base.client.config.external.ExternalConfigHandler;
import vazkii.quark.base.module.ModuleCategory;
import vazkii.quark.base.module.config.IConfigCallback;

@OnlyIn(Dist.CLIENT)
public final class IngameConfigHandler implements IConfigCallback {

	public static final IngameConfigHandler INSTANCE = new IngameConfigHandler();

	public Map<String, TopLevelCategory> topLevelCategories = new LinkedHashMap<>();
	
	private IConfigCategory currCategory = null;
	
	private IngameConfigHandler() {}
	
	@Override
	public void push(String s, String comment) {
		IConfigCategory newCategory = null;
		if(currCategory == null) {
			newCategory = new TopLevelCategory(s, comment, null);
			topLevelCategories.put(s, (TopLevelCategory) newCategory);
		} else newCategory = currCategory.addCategory(s, comment);
		
		currCategory = newCategory;
	}

	@Override
	public void pop() {
		if(currCategory != null) {
			currCategory.close();
			currCategory = currCategory.getParent();
		}
	}

	@Override
	public <T> void addEntry(String name, T default_, Supplier<T> getter, String comment, Predicate<Object> restriction) {
		if(currCategory != null)
			currCategory.addEntry(name, default_, getter, comment, restriction);
	}
	
	public IConfigObject<Boolean> getCategoryEnabledObject(ModuleCategory category) {
		return topLevelCategories.get("categories").getModuleOption(category);
	}
	
	public IConfigCategory getConfigCategory(ModuleCategory category) {
		return topLevelCategories.get(category == null ? "general" : category.name);
	}
	
	public void refresh() {
		topLevelCategories.values().forEach(IConfigElement::refresh);
	}

	public void debug() {
		if(!Quark.DEBUG_MODE)
			return;
		
		writeToFile(new File("config", "quark-common.toml-generated"), topLevelCategories);
	}
	
	public void commit() {
		commit(new File("config", "quark-common.toml"), topLevelCategories);
		ExternalConfigHandler.instance.commit();
	}

	public static <T extends IConfigCategory> void commit(File file, Map<String, T> map) {
		for(IConfigCategory c : map.values()) {
			if(c.isDirty()) {
				save(file, map);
				return;
			}
		}
	}
	
	public static <T extends IConfigCategory> void save(File file, Map<String, T> map) {
		writeToFile(file, map);
		for(IConfigCategory c1 : map.values())
			c1.clean();
	}
	
	public static <T extends IConfigCategory> void writeToFile(File file, Map<String, T> map) {
		try {
			file.createNewFile();
			PrintStream stream = new PrintStream(file);
			
			for(String name : map.keySet())
				map.get(name).print("", stream);
			
			stream.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
}
