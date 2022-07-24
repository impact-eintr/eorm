package org.apache.ibatis.reflection.property;

import java.lang.reflect.Field;

// 属性拷贝器
public final class PropertyCopier { // A final class is simply a class that can't be extended.
	private PropertyCopier() {
	}

	public static void copyBeanProperties(Class<?> type, Object srcBean, Object dstBean) {
		Class<?> parent = type;
		while (parent != null) {
			final Field[] fields = parent.getDeclaredFields();
			// 循环遍历属性进行拷贝
			for (Field field : fields) {
				try {
					try {
						field.set(dstBean, field.get(srcBean));
					} catch (IllegalAccessException e) {
						if (Reflector.canControlMemberAccessible()) {
							field.setAccessible(true);
							field.set(dstBean, field.get(srcBean));
						} else {
							throw e;
						}
					}
				} catch (Exception e) {
					// Nothing useful to do, will only fail on final fields, which will be ignored.
				}
			}
			parent = parent.getSuperclass();
		}
	}
}
