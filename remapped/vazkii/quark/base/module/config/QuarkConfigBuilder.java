package vazkii.quark.base.module.config;

import java.util.List;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;

import com.google.common.base.Predicates;

import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.common.ForgeConfigSpec.ConfigValue;

public class QuarkConfigBuilder implements IConfigBuilder {

	private final ForgeConfigSpec.Builder parent;
	private final IConfigCallback callback;
	
	private String currComment = "";
	
	public QuarkConfigBuilder(ForgeConfigSpec.Builder parent, IConfigCallback callback) {
		this.parent = parent;
		this.callback = callback;
	}

	@Override
	public <T> ForgeConfigSpec configure(Function<IConfigBuilder, T> func) {
		return parent.configure(b -> func.apply(this)).getRight();
	}
	
	@Override
	public void push(String s) {
		parent.push(s);
		callback.push(s, currComment);
		currComment = "";
	}

	@Override
	public void pop() {
		parent.pop();
		callback.pop();
	}

	@Override
	public void comment(String s) {
		currComment += s;
	}

	@Override
	public ConfigValue<?> defineList(String name, List<?> default_, Supplier<Object> getter, Predicate<Object> predicate) {
		onDefine(name, default_, getter, predicate);
		return parent.defineList(name, default_, predicate);
	}

	@Override
	public ConfigValue<?> defineObj(String name, Object default_, Supplier<Object> getter, Predicate<Object> predicate) {
		onDefine(name, default_, getter, predicate);
		return parent.define(name, default_, predicate);
	}

	@Override
	public ConfigValue<Boolean> defineBool(String name, Supplier<Boolean> getter, boolean default_) {
		onDefine(name, default_, getter, Predicates.alwaysTrue());
		return parent.define(name, default_);
	}

	@Override
	public ConfigValue<Integer> defineInt(String name, Supplier<Integer> getter, int default_) {
		onDefine(name, default_, getter, Predicates.alwaysTrue());
		return parent.define(name, default_);
	}
	
	@Override
	public ConfigValue<Double> defineDouble(String name, Supplier<Double> getter, double default_) {
		onDefine(name, default_, getter, Predicates.alwaysTrue());
		return parent.define(name, default_);
	}
	
	private <T> void onDefine(String name, T default_, Supplier<T> getter, Predicate<Object> predicate) {
		if(currComment.length() > 0)
			parent.comment(currComment);
		
		callback.addEntry(name, default_, getter, currComment, predicate);
		currComment = "";
	}

}
