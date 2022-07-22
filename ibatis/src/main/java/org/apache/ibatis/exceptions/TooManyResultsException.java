package org.apache.ibatis.exceptions;

public class TooManyResultsException extends PersistenceException {
	private static final long serialVersionUID = 8935197089745865786L;

	public TooManyResultsException() {
		super();
	}

	public TooManyResultsException(String message) {
		super(message);
	}

	public TooManyResultsException(String message, Throwable cause) {
		super(message, cause);
	}

	public TooManyResultsException(Throwable cause) {
		super(cause);
	}
}
