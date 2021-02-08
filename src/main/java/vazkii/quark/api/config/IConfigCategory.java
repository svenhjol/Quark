package vazkii.quark.api.config;

import java.util.List;
import java.util.function.Predicate;
import java.util.function.Supplier;

import javax.annotation.Nonnull;

import com.google.common.base.Predicates;

import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.ForgeConfigSpec.ConfigValue;

@OnlyIn(Dist.CLIENT)
public interface IConfigCategory extends IConfigElement {

	public IConfigCategory addCategory(String name, @Nonnull String comment);
	public <T> IConfigElement addEntry(String name, T default_, Supplier<T> getter, @Nonnull String comment, @Nonnull Predicate<Object> restriction);

	// defaults that you definitely want to use
	public default <T> void addEntry(String name, T default_, Supplier<T> getter, String comment) {
		addEntry(name, default_, getter, comment, Predicates.alwaysTrue());
	}
	
	public default <T> void addEntry(String name, T default_, Supplier<T> getter) {
		addEntry(name, default_, getter, "");
	}
	
	public default <T> void addEntry(ConfigValue<T> forgeValue) {
		addEntry(forgeValue.getPath().get(0), forgeValue.get(), forgeValue::get);
	}
	
	public default IConfigCategory addCategory(String name) {
		return addCategory(name, "");
	}
	
	// getters you probably don't have any use for
	public String getPath();
	public int getDepth();
	public List<IConfigElement> getSubElements();
	
	// probably stuff you shouldn't touch
	
	public void updateDirty();
	public void close();
	
	
}
