package vazkii.quark.base.client.config;

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Predicate;
import java.util.function.Supplier;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import vazkii.quark.base.Quark;
import vazkii.quark.base.module.ModuleCategory;
import vazkii.quark.base.module.config.IConfigCallback;

@Environment(EnvType.CLIENT)
public final class IngameConfigHandler implements IConfigCallback {

	public static final IngameConfigHandler INSTANCE = new IngameConfigHandler();

	public Map<String, ConfigCategory> topLevelCategories = new LinkedHashMap<>();
	
	private ConfigCategory currCategory = null;
	
	private IngameConfigHandler() {}
	
	@Override
	public void push(String s, String comment) {
		ConfigCategory newCategory = null;
		if(currCategory == null) {
			newCategory = new ConfigCategory(s, comment, null);
			topLevelCategories.put(s, newCategory);
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
			currCategory.addObject(name, default_, getter, comment, restriction);
	}
	
	public ConfigObject<Boolean> getCategoryEnabledObject(ModuleCategory category) {
		return topLevelCategories.get("categories").getModuleOption(category);
	}
	
	public ConfigCategory getConfigCategory(ModuleCategory category) {
		return topLevelCategories.get(category == null ? "general" : category.name);
	}
	
	public void refresh() {
		topLevelCategories.values().forEach(IConfigElement::refresh);
	}

	public void debug() {
		if(!Quark.DEBUG_MODE)
			return;
		
		writeToFile(new File("config", "quark-common.toml-generated"));
	}
	
	public void commit() {
		for(ConfigCategory c : topLevelCategories.values()) {
			if(c.isDirty()) {
				save();
				return;
			}
		}
	}
	
	private void save() {
		writeToFile(new File("config", "quark-common.toml"));
		for(ConfigCategory c1 : topLevelCategories.values())
			c1.clean();
	}
	
	private void writeToFile(File file) {
		try {
			file.createNewFile();
			PrintStream stream = new PrintStream(file);
			
			for(String name : topLevelCategories.keySet())
				topLevelCategories.get(name).print("", stream);
			
			stream.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
}
