package org.apache.ibatis.reflection.factory;

import java.util.List;
import java.util.Properties;

// 对象工厂
public interface ObjectFactory {
	// 设置工厂的属性
	default void setProperties(Properties properties) {
		// NOP
	}

	// 传入一个类型 采用无参构造函数生成实例
	<T> T create(Class<T> type);

	// 传入一个目标类型、一个参数类型列表、一个参数值列表，根据参数列表找到相应的含参构造方法生成这个类型的实例
	<T> T create(Class<T> type, List<Class<?>> constructorArgTypes, List<Object> constructorArgs);

	// 判断这个类是否是集合类
	<T> boolean isCollection(Class<T> type);
}
