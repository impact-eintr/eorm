package org.apache.ibatis.reflection.wrapper;

import org.apache.ibatis.reflection.property.PropertyTokenizer;

public interface ObjectWrapper {
	// get一个属性的值
	Object get(PropertyTokenizer prop);
	// set一个属性的值
	void set(PropertyTokenizer prop, Object value);
	// 找到指定属性值
	String findProperty(String name, boolean useCameCaseMapping);
	// 获得getter列表

	// 获得setter列表

	// 获得getter的类型

	// 获得getter的类型

	// 查看指定属性是否有setter

	// 查看指定属性是否有getter

	// 生成一个属性的实例

	// 判断是否是集合

	// 添加元素

	// 添加全部元素

}
