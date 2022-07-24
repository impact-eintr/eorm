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

	// 传入一个类型 采用无参构造函数生成实例
	<T> T create(Class<T> type, List<Class<?>> constructorArgTypes, List<Object> constructorArgs);

	<T> boolean isCollection(Class<T> type);
}
