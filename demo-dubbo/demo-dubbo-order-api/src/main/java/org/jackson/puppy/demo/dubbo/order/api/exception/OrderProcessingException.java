package org.jackson.puppy.demo.dubbo.order.api.exception;

/**
 * @author Kevin Hwang
 * @since 8/10/2018
 */
public class OrderProcessingException extends Exception {

	private static final long serialVersionUID = -2923323458727326311L;

	public OrderProcessingException() {
	}

	public OrderProcessingException(String message) {
		super(message);
	}

	public OrderProcessingException(String message, Throwable cause) {
		super(message, cause);
	}

	public OrderProcessingException(Throwable cause) {
		super(cause);
	}
}
