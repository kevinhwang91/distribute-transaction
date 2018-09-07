package org.jackson.puppy.tcc.transaction.api.exception;

/**
 * @author Kevin Hwang
 * @since 8/10/2018
 */
public class TransactionIOException extends RuntimeException {

	private static final long serialVersionUID = -425494379681690849L;

	public TransactionIOException() {
	}

	public TransactionIOException(String message) {
		super(message);
	}

	public TransactionIOException(String message, Throwable cause) {
		super(message, cause);
	}

	public TransactionIOException(Throwable cause) {
		super(cause);
	}

	public TransactionIOException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}
}
