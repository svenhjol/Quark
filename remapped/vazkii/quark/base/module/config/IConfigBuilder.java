package vazkii.quark.base.module.config;

import java.util.List;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;

import net.minecraftforge.common.ForgeConfigSpec;

public interface IConfigBuilder {

	public <T> ForgeConfigSpec configure(Function<IConfigBuilder, T> consumer);
	public void push(String s);
	public void pop();
	public void comment(String s);
	
	ForgeConfigSpec.ConfigValue<?> defineList(String name, List<?> default_, Supplier<Object> getter, Predicate<Object> predicate);
	ForgeConfigSpec.ConfigValue<?> defineObj(String name, Object default_, Supplier<Object> getter, Predicate<Object> predicate);
	
	ForgeConfigSpec.ConfigValue<Boolean> defineBool(String name, Supplier<Boolean> getter, boolean default_);
	ForgeConfigSpec.ConfigValue<Integer> defineInt(String name, Supplier<Integer> getter, int default_);
	ForgeConfigSpec.ConfigValue<Double> defineDouble(String name, Supplier<Double> getter, double default_);
}
