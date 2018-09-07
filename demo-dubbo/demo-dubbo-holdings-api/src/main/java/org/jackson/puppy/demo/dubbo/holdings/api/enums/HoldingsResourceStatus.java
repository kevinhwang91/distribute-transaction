package org.jackson.puppy.demo.dubbo.holdings.api.enums;


/**
 * @author Kevin Hwang
 * @since 8/10/2018
 */
public enum HoldingsResourceStatus {

	PENDING(1), CONFIRM(2);

	private int status;

	HoldingsResourceStatus(int status) {
		this.status = status;
	}

	public int getStatus() {
		return status;
	}
}
