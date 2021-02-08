package vazkii.quark.base.module.config;

import java.util.function.Predicate;
import java.util.function.Supplier;

public interface IConfigCallback {
	
	public abstract void push(String s, String comment);
	public abstract void pop();
	
	public abstract <T> void addEntry(String name, T default_, Supplier<T> getter, String comment, Predicate<Object> restriction);
	
	public static final class Dummy implements IConfigCallback {

		@Override
		public void push(String s, String comment) {
			// NO-OP
		}

		@Override
		public void pop() {
			// NO-OP			
		}

		@Override
		public <T> void addEntry(String name, T default_, Supplier<T> getter, String comment, Predicate<Object> restriction) {
			// NO-OP			
		}
		
	}
	
}
