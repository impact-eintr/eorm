package org.apache.ibatis.reflection.invoker;

import org.apache.ibatis.reflection.Reflector;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class MethodInvoker implements Invoker{
	private final Class<?> type;
	private final Method method;

	public MethodInvoker(Method method) {
		this.method = method;

		if (method.getParameterTypes().length == 1) {
			type = method.getParameterTypes()[0]; // 入参
		} else {
			type = method.getReturnType(); // 返回值
		}
	}

	// 方法执行函数
	@Override
	public Object invoke(Object target, Object[] args) throws IllegalAccessException, InvocationTargetException {
		try {
			return method.invoke(target, args);
		} catch (IllegalAccessException e) {
			if (Reflector.canControlMemberAccessible()) {
				method.setAccessible(true);
				return method.invoke(target, args);
			} else {
				throw e;
			}
		}
	}

	// 传入参数或者传出参数的类型(一个参数是入参，否则就是出参)
	@Override
	public Class<?> getType() {
		return type;
	}
}
