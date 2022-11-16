package org.apache.ibatis.reflection.wrapper;

import org.apache.ibatis.reflection.MetaClass;
import org.apache.ibatis.reflection.MetaObject;
import org.apache.ibatis.reflection.factory.ObjectFactory;
import org.apache.ibatis.reflection.property.PropertyTokenizer;

import java.util.List;

public class BeanWrapper extends BaseWrapper {
	// 被包装的对象
	private final Object object;
	// 被包装对象所属类的元类
	private final MetaClass metaClass;

	public BeanWrapper(MetaObject metaObject, Object object) {
		super(metaObject);
		this.object = object;
		this.metaClass = MetaClass.forClass(object.getClass(), metaObject.getReflectorFactory());
	}

	// get一个属性的值
	public Object get(PropertyTokenizer prop) {
		if (prop.getIndex() != null) {
			Object collection = resolveCollection(prop, object);
			return getCollectionValue(prop, collection);
		} else {
			return getBeanProperty(prop, object);
		}
	}
	// set一个属性的值
	public void set(PropertyTokenizer prop, Object value) {

	}
	// 找到指定属性值
	public String findProperty(String name, boolean useCameCaseMapping) {

	}
	// 获得getter列表
	public String[] getGetterNames() {

	}
	// 获得setter列表
	public String[] getSetterNames() {

	}
	// 获得getter的类型
	public Class<?> getGetterType(String name) {

	}
	// 获得getter的类型
	public Class<?> getSetterType(String name) {

	}
	// 查看指定属性是否有setter
	public boolean hasSetter(String name) {

	}
	// 查看指定属性是否有getter
	public boolean hasGetter(String name) {

	}
	// 生成一个属性的实例
	public MetaObject instantiateProPertyValue(String name, PropertyTokenizer prop,
																						 ObjectFactory objectFactory) {

	}

	// 通过调用getter方法 获取对象属性
	private Object getBeanProperty(PropertyTokenizer prop, Object object) {

	}

	private void setBeanProperty(PropertyTokenizer prop, Object object, Object value) {

	}

	// 判断是否是集合
	public boolean isCollection() {

	}
	// 添加元素
	public void add(Object element) {

	}
	// 添加全部元素
	public <E> void addAll(List<E> element) {

	}
}
