package org.apache.ibatis.exceptions;

public class PersistenceException extends IbatisException {
	private static final long serialVersionUID = -3880206998166270511L;

	public PersistenceException() {
		super();
	}

	public PersistenceException(String message) {
		super(message);
	}

	public PersistenceException(String message, Throwable cause) {
		super(message, cause);
	}

	public PersistenceException(Throwable cause) {
		super(cause);
	}
}
