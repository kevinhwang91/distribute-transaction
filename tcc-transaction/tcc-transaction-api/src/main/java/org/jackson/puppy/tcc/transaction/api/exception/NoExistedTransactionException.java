package org.jackson.puppy.tcc.transaction.api.exception;

/**
 * @author Kevin Hwang
 * @since 8/10/2018
 */
public class NoExistedTransactionException extends Exception {

	private static final long serialVersionUID = 9057099325847500023L;

	public NoExistedTransactionException() {
	}

	public NoExistedTransactionException(String message) {
		super(message);
	}

	public NoExistedTransactionException(String message, Throwable cause) {
		super(message, cause);
	}

	public NoExistedTransactionException(Throwable cause) {
		super(cause);
	}

	public NoExistedTransactionException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}
}
