package org.apache.ibatis.reflection.invoker;

import java.lang.reflect.Field;

public class SetFieldInvoker implements Invoker{
	private final Field field;

	public SetFieldInvoker(Field field) {
		this.field = field;
	}

	@Override
	public Object invoke(Object target, Object[] args) throws IllegalAccessException {
		try {
			// 直接给属性赋值就可以
			field.set(target, args[0]);
		} catch (IllegalAccessException e) {
			if (Reflector.canControlMemberAccessible()) {
				field.setAccessible(true);
				field.set(target, args[0]);
			} else {
				throw e;
			}
		}
		return null;
	}

	@Override
	public Class<?> getType() {
		return field.getType();
	}
}
