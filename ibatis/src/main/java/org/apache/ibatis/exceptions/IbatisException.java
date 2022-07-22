package org.apache.ibatis.exceptions;

public class IbatisException extends RuntimeException {
	// 序列化标志
	private static final long serialVersionUID = 3880206998166270511L;

	public IbatisException() {
		super();
	}

	public IbatisException(String message) {
		super(message);
	}

	public IbatisException(String message, Throwable cause) {
		super(message, cause);
	}

	public IbatisException(Throwable cause) {
		super(cause);
	}
}
