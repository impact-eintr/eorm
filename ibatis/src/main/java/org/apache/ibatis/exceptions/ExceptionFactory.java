package org.apache.ibatis.exceptions;

import java.beans.PersistenceDelegate;

public class ExceptionFactory {
	private ExceptionFactory() {}

	public static RuntimeException wrapException(String message, Exception e) {
		return new PersistenceException();
	}
}
