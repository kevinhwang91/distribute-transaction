package org.jackson.puppy.demo.dubbo.holdings.api.exception;

/**
 * @author Kevin Hwang
 * @since 8/10/2018
 */
public class OptimisticLockTimeoutException extends Exception {

	private static final long serialVersionUID = 8266160495946397791L;

	public OptimisticLockTimeoutException() {
	}

	public OptimisticLockTimeoutException(String message) {
		super(message);
	}

	public OptimisticLockTimeoutException(String message, Throwable cause) {
		super(message, cause);
	}

	public OptimisticLockTimeoutException(Throwable cause) {
		super(cause);
	}
}
