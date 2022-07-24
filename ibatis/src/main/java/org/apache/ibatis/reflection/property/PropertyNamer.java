package org.apache.ibatis.reflection.property;

import java.util.Locale;

public final class PropertyNamer {
	private PropertyNamer() {}

	// 将方法名转化为属性名
	public static String methodToProperty(String name) {
		if (name.startsWith("is")) {
			name = name.substring(2);
		} else if (name.startsWith("get") || name.startsWith("set")) {
			name = name.substring(3);
		} else {
			throw new ReflectionException("Error parsing property name '" + name + "'. Didn't start with 'is', 'get' or 'set'.");
		}

		// 将方法名中属性的大小写修改正确
		if (name.length() == 1 || (name.length() > 1 && !Character.isUpperCase(name.charAt(1)))) {
			name = name.substring(0, 1).toLowerCase(Locale.ENGLISH) + name.substring(1);
		}
		return name;
	}

	public static boolean isProperty(String name) {
		return isGetter(name) || isSetter(name);
	}

	public static boolean isGetter(String name) {
		return (name.startsWith("get") && name.length() > 3) ||
						(name.startsWith("is") && name.length() > 2);
	}

	public static boolean isSetter(String name) {
		return name.startsWith("set") && name.length() > 3;
	}
}
