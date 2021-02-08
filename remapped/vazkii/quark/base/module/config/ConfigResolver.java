package vazkii.quark.base.module.config;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.text.WordUtils;

import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.config.ModConfig;
import vazkii.quark.base.Quark;
import vazkii.quark.base.handler.GeneralConfig;
import vazkii.quark.base.module.QuarkModule;
import vazkii.quark.base.module.ModuleCategory;

public class ConfigResolver {

	private final ConfigFlagManager flagManager;
	
	private List<Runnable> refreshRunnables = new LinkedList<>();
	
	public ConfigResolver() {
		this.flagManager = new ConfigFlagManager();
	}
	
	public void makeSpec() {
		ForgeConfigSpec.Builder forgeBuilder = new ForgeConfigSpec.Builder();
		IConfigCallback callback = Quark.proxy.getConfigCallback();
		IConfigBuilder builder = new QuarkConfigBuilder(forgeBuilder, callback);
		
		ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, builder.configure(this::build));
	}
	
	public void configChanged() {
		flagManager.clear();
		refreshRunnables.forEach(Runnable::run);
	}
	
	private Void build(IConfigBuilder builder) {
		builder.push("general");
		try {
			ConfigObjectSerializer.serialize(builder, flagManager, refreshRunnables, GeneralConfig.INSTANCE);
		} catch (ReflectiveOperationException e) {
			throw new RuntimeException("Failed to create quark general config", e);
		}
		builder.pop();
		
		builder.push("categories");
		buildCategoryList(builder);
		builder.pop();
		
		for(ModuleCategory category : ModuleCategory.values())
			buildCategory(builder, category);
		
		return null;
	}
	
	@SuppressWarnings("deprecation")
	private void buildCategoryList(IConfigBuilder builder) { 
		for(ModuleCategory category : ModuleCategory.values()) {
			ForgeConfigSpec.ConfigValue<Boolean> value = builder.defineBool(WordUtils.capitalizeFully(category.name), () -> category.enabled, true);
			refreshRunnables.add(() -> category.enabled = value.get());
		}
	}
	
	private void buildCategory(IConfigBuilder builder, ModuleCategory category) {
		builder.push(category.name);
		
		List<QuarkModule> modules = category.getOwnedModules();
		Map<QuarkModule, Runnable> setEnabledRunnables = new HashMap<>();
		
		for(QuarkModule module : modules) {
			ForgeConfigSpec.ConfigValue<Boolean> value = builder.defineBool(module.displayName, () -> module.configEnabled, module.enabledByDefault);
			setEnabledRunnables.put(module, () -> {
				module.setEnabled(value.get() && category.enabled);
				flagManager.putEnabledFlag(module);
			});
		}
	
		for(QuarkModule module : modules)
			buildModule(builder, module, setEnabledRunnables.get(module));
		
		builder.pop();
	}
	
	private void buildModule(IConfigBuilder builder, QuarkModule module, Runnable setEnabled) {
		if(!module.description.isEmpty())
			builder.comment(module.description);
		
		builder.push(module.lowercaseName);
		
		if(module.antiOverlap != null && module.antiOverlap.size() > 0)
			addModuleAntiOverlap(builder, module);
		
		refreshRunnables.add(setEnabled);
		
		try {
			ConfigObjectSerializer.serialize(builder, flagManager, refreshRunnables, module);
		} catch(ReflectiveOperationException e) {
			throw new RuntimeException("Failed to create config spec for module " + module.displayName, e);
		}
		
		refreshRunnables.add(() -> module.pushFlags(flagManager));
		
		builder.pop();
	}
	
	private void addModuleAntiOverlap(IConfigBuilder builder, QuarkModule module) {
		StringBuilder desc = new StringBuilder("This feature disables itself if any of the following mods are loaded: \n");
		for(String s : module.antiOverlap)
			desc.append(" - ").append(s).append("\n");
		desc.append("This is done to prevent content overlap.\nYou can turn this on to force the feature to be loaded even if the above mods are also loaded.");
		String descStr = desc.toString();
		
		builder.comment(descStr);
		ForgeConfigSpec.ConfigValue<Boolean> value = builder.defineBool("Ignore Anti Overlap", () -> module.ignoreAntiOverlap, false);
		refreshRunnables.add(() -> module.ignoreAntiOverlap = !GeneralConfig.useAntiOverlap || value.get());
	}
	
}
