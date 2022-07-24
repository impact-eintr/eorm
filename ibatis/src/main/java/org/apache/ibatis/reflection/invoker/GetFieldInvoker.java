package org.apache.ibatis.reflection.invoker;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;

public class GetFieldInvoker implements Invoker {
	private final Field field;

	public GetFieldInvoker(Field field)  {
		this.field = field;
	}

	@Override
	public Object invoke(Object target, Object[] args) throws IllegalAccessException, InvocationTargetException {
		try {
			return field.get(target);
		} catch (IllegalAccessException e) {
			if (Reflector.canControlMemberAccessible()) { // 如果属性的访问性可以修改
				// 将属性的可访问性修改为可访问
				field.setAccessible(true);
				// 再次通过反射获取目标属性的值
				return field.get(target);
			} else {
				throw e;
			}
		}
	}

	// 传入参数或者传出参数的类型(一个参数是入参，否则就是出参)
	@Override
	public Class<?> getType() {
		return field.getType();
	}
}
