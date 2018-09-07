package org.jackson.puppy.tcc.transaction.api.exception;

/**
 * @author Kevin Hwang
 * @since 8/10/2018
 */
public class ConfirmingException extends TccException {

	private static final long serialVersionUID = -7711947629536364445L;

	public ConfirmingException() {
	}

	public ConfirmingException(String message) {
		super(message);
	}

	public ConfirmingException(String message, Throwable cause) {
		super(message, cause);
	}

	public ConfirmingException(Throwable cause) {
		super(cause);
	}

	public ConfirmingException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}
}
