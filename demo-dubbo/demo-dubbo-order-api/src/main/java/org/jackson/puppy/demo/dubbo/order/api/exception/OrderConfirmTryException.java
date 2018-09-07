package org.jackson.puppy.demo.dubbo.order.api.exception;

/**
 * @author Kevin Hwang
 * @since 8/10/2018
 */
public class OrderConfirmTryException extends Exception {

	private static final long serialVersionUID = 8012192036759822027L;

	public OrderConfirmTryException() {
	}

	public OrderConfirmTryException(String message) {
		super(message);
	}

	public OrderConfirmTryException(String message, Throwable cause) {
		super(message, cause);
	}

	public OrderConfirmTryException(Throwable cause) {
		super(cause);
	}

	public OrderConfirmTryException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}
}

