package org.apache.ibatis.reflection.property;

import org.apache.ibatis.reflection.Reflector;

import java.lang.reflect.Field;

// 属性拷贝器
public final class PropertyCopier { // A final class is simply a class that can't be extended.
	private PropertyCopier() {
	}

	public static void copyBeanProperties(Class<?> type, Object srcBean, Object dstBean) {
		Class<?> parent = type;
		while (parent != null) { // 直到parent为Object的null超类
			// getDeclareFields()
			// 从局部变量表中获取this指针 也就是那个Class实例对象的指针
			// this指针中Extra()是持有该实例的那个类 这一步是在classLoad时完成的
			// 调用这个类的GetFields可以获取所有属性[]Field
			// Field就是一些Name Descriptor Slots...
			// 然后加载java/lang/Reflect/Field类
			// 用上面的[]Field遍历构造Reflect.Field的实例并装载到数组中
			// 注意在构造时Reflect.Field.extra被设置为了Field
			// 最终返回的就是Reflect.Field的一个数组
			final Field[] fields = parent.getDeclaredFields();
			// 循环遍历属性进行拷贝
			for (Field field : fields) {
				try {
					try {
						// field 是 java.lang.reflect.Field
						// 这里我猜测是field.Extra()获取了Field
						// 然后传入dstBean
						// 调用dstBean.SetRefVar(Field.name, Field.desc, value)
						// 这个函数将找到dstBean的对应实例字段 然后修改对应的内存
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
