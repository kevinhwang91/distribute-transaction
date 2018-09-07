package org.jackson.puppy.tcc.transaction.api.exception;

/**
 * @author Kevin Hwang
 * @since 8/10/2018
 */
public class TccException extends RuntimeException {

	private static final long serialVersionUID = -3469449582189300232L;

	public TccException() {
	}

	public TccException(String message) {
		super(message);
	}

	public TccException(String message, Throwable cause) {
		super(message, cause);
	}

	public TccException(Throwable cause) {
		super(cause);
	}

	public TccException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}
}
