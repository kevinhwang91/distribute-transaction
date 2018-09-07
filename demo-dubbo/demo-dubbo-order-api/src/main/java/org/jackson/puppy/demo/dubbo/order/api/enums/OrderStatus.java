package org.jackson.puppy.demo.dubbo.order.api.enums;


/**
 * @author Kevin Hwang
 * @since 8/10/2018
 */
public enum OrderStatus {

	PAID(1), RECEIVING(2), RECEIVED(3);

	private int status;

	OrderStatus(int status) {
		this.status = status;
	}

	public int getStatus() {
		return status;
	}
}
