package org.jackson.puppy.tcc.transaction.api.exception;

/**
 * @author Kevin Hwang
 * @since 8/10/2018
 */
public class CancellingException extends TccException {

	private static final long serialVersionUID = 4169545089500349066L;

	public CancellingException() {
	}

	public CancellingException(String message) {
		super(message);
	}

	public CancellingException(String message, Throwable cause) {
		super(message, cause);
	}

	public CancellingException(Throwable cause) {
		super(cause);
	}

	public CancellingException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}
}
