package org.apache.ibatis.reflection.wrapper;

import org.apache.ibatis.reflection.*;
import org.apache.ibatis.reflection.factory.ObjectFactory;
import org.apache.ibatis.reflection.invoker.Invoker;
import org.apache.ibatis.reflection.property.PropertyTokenizer;

import java.security.InvalidKeyException;
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
		if (prop.getIndex() != null) {
			Object collection = resolveCollection(prop, object);
			setCollectionValue(prop, collection, value);
		} else {
			setBeanProperty(prop, object, value);
		}
	}
	// 找到指定属性值
	public String findProperty(String name, boolean useCameCaseMapping) {
		return metaClass.findProperty(name, useCameCaseMapping);
	}
	// 获得getter列表
	public String[] getGetterNames() {
		return metaClass.getGetterNames();
	}
	// 获得setter列表
	public String[] getSetterNames() {
		return metaClass.getSetterNames();
	}
	// 获得getter的类型
	public Class<?> getGetterType(String name) {
		PropertyTokenizer prop = new PropertyTokenizer(name);
		if (prop.hasNext()) {
			MetaObject metaValue = metaObject.metaObjectForProperty(prop.getIndexedName());
			if (metaValue == SystemMetaObject.NULL_META_OBJECT) {
				return metaClass.getGetterType(name);
			} else {
				return metaValue.getGetterType(prop.getChildren());
			}
		} else {
			return metaClass.getGetterType(name);
		}
	}
	// 获得getter的类型
	public Class<?> getSetterType(String name) {
		PropertyTokenizer prop = new PropertyTokenizer(name);
		if (prop.hasNext()) {
			MetaObject metaValue = metaObject.metaObjectForProperty(prop.getIndexedName());
			if (metaValue == SystemMetaObject.NULL_META_OBJECT) {
				return metaClass.getSetterType(name);
			} else {
				return metaValue.getSetterType(prop.getChildren());
			}
		} else {
			return metaClass.getSetterType(name);
		}
	}
	// 查看指定属性是否有setter
	public boolean hasSetter(String name) {
		PropertyTokenizer prop = new PropertyTokenizer(name);
		if (prop.hasNext()) {
			if (metaClass.hasSetter(prop.getIndexedName())) {
				MetaObject metaValue = metaObject.metaObjectForProperty(prop.getIndexedName());
				if (metaValue == SystemMetaObject.NULL_META_OBJECT) {
					return metaClass.hasSetter(name);
				} else {
					return metaValue.hasSetter(prop.getChildren());
				}
			} else {
				return false;
			}
		} else {
			return metaClass.hasSetter(name);
		}
	}
	// 查看指定属性是否有getter
	public boolean hasGetter(String name) {
		PropertyTokenizer prop = new PropertyTokenizer(name);
		if (prop.hasNext()) {
			if (metaClass.hasGetter(prop.getIndexedName())) {
				MetaObject metaValue = metaObject.metaObjectForProperty(prop.getIndexedName());
				if (metaValue == SystemMetaObject.NULL_META_OBJECT) {
					return metaClass.hasGetter(name);
				} else {
					return metaValue.hasGetter(prop.getChildren());
				}
			} else {
				return false;
			}
		} else {
			return metaClass.hasGetter(name);
		}
	}

	@Override
	// 生成一个属性的实例
	public MetaObject instantiatePropertyValue(String name, PropertyTokenizer prop,
																						 ObjectFactory objectFactory) {

		MetaObject metaValue;
		Class<?> type = getSetterType(prop.getName());
		try {
			Object newObject = objectFactory.create(type);
			metaValue = MetaObject.forObject(newObject, metaObject.getObjectFactory(), metaObject.getObjectWrapperFactory(), metaObject.getReflectorFactory());
			set(prop, newObject);
		} catch (Exception e) {
			throw new ReflectionException("Cannot set value of property '" + name + "' because '" + name + "' is null and cannot be instantiated on instance of " + type.getName() + ". Cause:" + e.toString(), e);
		}
		return metaValue;
	}

	// 通过调用getter方法 获取对象属性
	private Object getBeanProperty(PropertyTokenizer prop, Object object) {
		try {
			Invoker method = metaClass.getGetInvoker(prop.getName());
			try {
				return method.invoke(object, NO_ARGUEMENTS);
			} catch (Throwable t) {
				throw ExceptionUtil.unwrapThrowable(t);
			}
		} catch (Throwable t) {
			throw new ReflectionException("Could not get property '" + prop.getName() + "' from " + object.getClass() + ".  Cause: " + t.toString(), t);
		}
	}

	private void setBeanProperty(PropertyTokenizer prop, Object object, Object value) {
		try {
			Invoker method = metaClass.getSetInvoker(prop.getName());
			Object[] params = {value};
			try {
				method.invoke(object, params);
			} catch (Throwable t) {
				throw ExceptionUtil.unwrapThrowable(t);
			}
		} catch (Throwable t) {
			throw new ReflectionException("Could not set property '" + prop.getName() + "' of '" + object.getClass() + "' with value '" + value + "' Cause: " + t.toString(), t);
		}
	}

	// 判断是否是集合
	public boolean isCollection() {
		return false;
	}
	// 添加元素
	public void add(Object element) {
		throw new UnsupportedOperationException();
	}
	// 添加全部元素
	public <E> void addAll(List<E> element) {
		throw new UnsupportedOperationException();
	}
}
