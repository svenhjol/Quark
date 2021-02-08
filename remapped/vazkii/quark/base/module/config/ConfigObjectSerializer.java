package vazkii.quark.base.module.config;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.function.Predicate;
import java.util.function.Supplier;

import org.apache.commons.lang3.text.WordUtils;

import net.minecraftforge.common.ForgeConfigSpec;
import vazkii.quark.base.module.QuarkModule;

@SuppressWarnings("deprecation")
public final class ConfigObjectSerializer {
	
	public static void serialize(IConfigBuilder builder, ConfigFlagManager flagManager, List<Runnable> callbacks, Object object) throws ReflectiveOperationException {
		List<Field> fields = recursivelyGetFields(object.getClass());
		for(Field f : fields) {
			Config config = f.getDeclaredAnnotation(Config.class);
			if(config != null)
				pushConfig(builder, flagManager, callbacks, object, f, config);
		}
	}
	
	private static List<Field> recursivelyGetFields(Class<?> clazz) {
		List<Field> list = new LinkedList<>();
		while(clazz != Object.class) {
			Field[] fields = clazz.getDeclaredFields();
			list.addAll(Arrays.asList(fields));
				
			clazz = clazz.getSuperclass();	
		}
		
		return list;
	}
	
	private static void pushConfig(IConfigBuilder builder, ConfigFlagManager flagManager, List<Runnable> callbacks, Object object, Field field, Config config) throws ReflectiveOperationException {
		field.setAccessible(true);
		
		String name = config.name();
		if(name.isEmpty())
			name = WordUtils.capitalizeFully(field.getName().replaceAll("(?<=.)([A-Z])", " $1"));

		Config.Restriction restriction = field.getDeclaredAnnotation(Config.Restriction.class);
		Config.Min min = field.getDeclaredAnnotation(Config.Min.class);
		Config.Max max = field.getDeclaredAnnotation(Config.Max.class);

		String nl = "";
		Class<?> type = field.getType();
		if(!config.description().isEmpty()) {
			builder.comment(config.description());
			nl = "\n";
		}

		if (restriction != null) {
			StringBuilder arrStr = new StringBuilder("[");

			String[] values = restriction.value();
			int lineLength = 0;

			arrStr.append('[');
			for (int i = 0; i < values.length; i++) {
				String value = String.valueOf(values[i]);
				arrStr.append(value);
				lineLength += value.length();
				if (i == values.length - 1)
					arrStr.append(']');
				else {
					if (lineLength > 50) {
						arrStr.append(",\n ");
						lineLength = 0;
					} else {
						arrStr.append(", ");
						lineLength += 2;
					}
				}
			}

			builder.comment(nl + "Allowed values: " + arrStr.toString());
		}

		if (min != null || max != null) {
			NumberFormat format = DecimalFormat.getNumberInstance(Locale.ROOT);
			String minPart = min == null ? "(" : ((min.exclusive() ? "(" : "[") + format.format(min.value()));
			String maxPart = max == null ? ")" : (format.format(max.value()) + (max.exclusive() ? ")" : "]"));
			builder.comment(nl + "Allowed values: " + minPart + "," + maxPart);
		}
		
		boolean isStatic = Modifier.isStatic(field.getModifiers());
		Supplier<Object> supplier = () -> {
			try {
				return isStatic ? field.get(null) : field.get(object);
			} catch(IllegalAccessException e) {
				throw new RuntimeException(e);
			}
		};
		
		Object defaultValue = supplier.get();
		if(type == float.class)
			throw new IllegalArgumentException("Floats can't be used in config, use double instead. Offender: " + field);
		
		if(defaultValue instanceof IConfigType) {
			name = name.toLowerCase(Locale.ROOT).replaceAll(" ", "_");
			
			builder.push(name);
			serialize(builder, flagManager, callbacks, defaultValue);
			callbacks.add(() -> ((IConfigType) defaultValue).onReload(flagManager));
			builder.pop();
			
			return;
		}
		
		String flag = config.flag();
		boolean useFlag = object instanceof QuarkModule && !flag.isEmpty();
			
		ForgeConfigSpec.ConfigValue<?> value = (defaultValue instanceof List) ?
				builder.defineList(name, (List<?>) defaultValue, supplier, restrict(restriction, min, max)) :
				builder.defineObj(name, defaultValue, supplier, restrict(restriction, min, max));
				
		callbacks.add(() -> {
			try {
				Object setObj = value.get();
				if(isStatic)
					field.set(null, setObj);
				else field.set(object, setObj);
				
				if(useFlag)
					flagManager.putFlag((QuarkModule) object, flag, (boolean) setObj);
			} catch(IllegalAccessException e) {
				throw new RuntimeException(e);
			}
		});
	}

	private static Predicate<Object> restrict(Config.Restriction restriction, Config.Min min, Config.Max max) {
		String[] restrictions = restriction == null ? null : restriction.value();
		double minVal = min == null ? -Double.MAX_VALUE : min.value();
		double maxVal = max == null ? Double.MAX_VALUE : max.value();
		boolean minExclusive = min != null && min.exclusive();
		boolean maxExclusive = max != null && max.exclusive();

		return (o) -> restrict(o, minVal, minExclusive, maxVal, maxExclusive, restrictions);
	}

	private static boolean restrict(Object o, double minVal, boolean minExclusive, double maxVal, boolean maxExclusive, String[] restrictions) {
		if (o == null)
			return false;

		if (o instanceof Number) {
			double val = ((Number) o).doubleValue();
			if (minExclusive) {
				if (minVal >= val)
					return false;
			} else if (minVal > val)
				return false;

			if (maxExclusive) {
				if (maxVal <= val)
					return false;
			} else if (maxVal < val)
				return false;
		}

		if (o instanceof String && restrictions != null) {
			for (String check : restrictions) {
				if (o.equals(check))
					return true;
			}

			return false;
		}

		return true;
	}
}
