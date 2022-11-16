package org.apache.ibatis.reflection.wrapper;

import org.apache.ibatis.reflection.MetaObject;
import org.apache.ibatis.reflection.factory.ObjectFactory;
import org.apache.ibatis.reflection.property.PropertyTokenizer;

import java.util.List;

public interface ObjectWrapper {
	// get一个属性的值
	Object get(PropertyTokenizer prop);
	// set一个属性的值
	void set(PropertyTokenizer prop, Object value);
	// 找到指定属性值
	String findProperty(String name, boolean useCameCaseMapping);
	// 获得getter列表
	String[] getGetterNames();
	// 获得setter列表
	String[] getSetterNames();
	// 获得getter的类型
	Class<?> getGetterType(String name);
	// 获得getter的类型
	Class<?> getSetterType(String name);
	// 查看指定属性是否有setter
	boolean hasSetter(String name);
	// 查看指定属性是否有getter
	boolean hasGetter(String name);
	// 生成一个属性的实例
	MetaObject instantiateProPertyValue(String name, PropertyTokenizer prop,
																			ObjectFactory objectFactory);
	// 判断是否是集合
	boolean isCollection();
	// 添加元素
	void add(Object element);
	// 添加全部元素
	<E> void addAll(List<E> element);
}
