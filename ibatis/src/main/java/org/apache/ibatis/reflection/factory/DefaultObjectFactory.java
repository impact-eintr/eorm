package org.apache.ibatis.reflection.factory;

import java.io.Serializable;
import java.lang.reflect.Constructor;
import java.util.*;
import java.util.stream.Collectors;

public class DefaultObjectFactory implements ObjectFactory, Serializable {
	private static final long serialVersionUID = -8855120656740914948L;

	public <T> T create(Class<T> type) {
		return create(type, null, null);
	}

	public <T> T create(Class<T> type, List<Class<?>> constructorArgTypes, List<Object> constructorArgs){
		Class<?> classToCreate = resolveInterface(type);
		// 创建类型实例
		return (T) instantiateClass(classToCreate, constructorArgTypes, constructorArgs);
	}


	private  <T> T instantiateClass(Class<T> type, List<Class<?>> constructorArgTypes, List<Object> constructorArgs) {
		try {
			Constructor<T> constructor;
			if (constructorArgTypes == null || constructorArgs == null) { // 参数类型列表为空或者参数列表为空
				constructor = type.getDeclaredConstructor();
				try {
					return constructor.newInstance();
				} catch (IllegalAccessException e) {
					// 如果发生异常 则修改构造函数的访问属性后再次尝试
					if (Reflector.canControlMemberAccessible()) {
						constructor.setAccessible(true);
						return constructor.newInstance();
					} else {
						throw e;
					}
				}
			}

			// 根据如惨类型查找对应的构造器
			constructor = type.getDeclaredConstructor(constructorArgTypes.toArray(new Class[constructorArgTypes.size()]));
			try {
				// 采用有参构造函数创建实例
				return constructor.newInstance(constructorArgs.toArray(new Object[constructorArgs.size()]));
			} catch (IllegalAccessException e) {
				if (Reflector.canControlMemberAccessible()) {
					// 如果发生异常，则修改构造函数的访问属性后再次尝试
					constructor.setAccessible(true);
					return constructor.newInstance(constructorArgs.toArray(new Object[constructorArgs.size()]));
				} else {
					throw e;
				}
			}
		} catch (Exception e) {
			// 收集所有的参数类型与参数值
			String argTypes = Optional.ofNullable(constructorArgTypes).
							orElseGet(Collections::emptyList).stream().
							map(String::valueOf).collect(Collectors.joining(","));
			String argValues = Optional.ofNullable(constructorArgs).
							orElseGet(Collections::emptyList).stream().
							map(String::valueOf).collect(Collectors.joining(","));
			throw new ReflectionException("Error instantiating " + type + "with invalid types (" + argTypes + ") or values ("+ argValues+"), Cause: " + e, e);
		}
	}

		// 判断要创建的目标对象的类型 即如果传入的是接口则给出它的一种实现
	protected Class<?> resolveInterface(Class<?> type) {
		Class<?> classToCreate;
		if (type == List.class || type == Collection.class || type == Iterable.class) {
			classToCreate = ArrayList.class;
		} else if (type == Map.class) {
			classToCreate = HashMap.class;
		} else if (type == SortedSet.class) {
			classToCreate = TreeSet.class;
		} else if (type == Set.class) {
			classToCreate = HashSet.class;
		} else {
			classToCreate = type;
		}
		return classToCreate;
	}


	public <T> boolean isCollection(Class<T> type) {
		return Collection.class.isAssignableFrom(type);
	}
}