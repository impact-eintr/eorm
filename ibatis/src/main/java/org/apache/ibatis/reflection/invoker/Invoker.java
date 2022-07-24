package org.apache.ibatis.reflection.invoker;

import java.lang.reflect.InvocationTargetException;

public interface Invoker {
	// 方法执行调用器
	Object invoke(Object target, Object[] args) throws IllegalAccessException, InvocationTargetException;
	// 传入参数或者传出参数的类型(一个参数是入参，否则就是出参)
	Class<?> getType();
}
