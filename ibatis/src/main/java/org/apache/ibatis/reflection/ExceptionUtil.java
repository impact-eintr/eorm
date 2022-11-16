package org.apache.ibatis.reflection;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.UndeclaredThrowableException;

public class ExceptionUtil {
	private ExceptionUtil() {}

	public static Throwable unwrapThrowable(Throwable wrapped) {
		// 该变量用来存放拆包得到的异常
		Throwable unwrapped = wrapped;
		while (true) {
			if (unwrapped instanceof InvocationTargetException) {
				unwrapped = ((InvocationTargetException)unwrapped).getTargetException(); // 获取内部异常
			} else if (unwrapped instanceof UndeclaredThrowableException) {
				unwrapped = ((UndeclaredThrowableException)unwrapped).getUndeclaredThrowable();
			} else {
				return unwrapped; // 该异常无需拆包
			}
		}
	}
}
