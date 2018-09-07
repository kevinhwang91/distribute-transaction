package org.jackson.puppy.demo.dubbo.bill.api.enums;

/**
 * @author Kevin Hwang
 * @since 8/10/2018
 */
public enum BillStatus {

	PENDING(1), CONFIRMED(2);

	private int status;

	BillStatus(int status) {
		this.status = status;
	}

	public int getStatus() {
		return status;
	}
}
