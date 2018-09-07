package org.jackson.puppy.demo.dubbo.confirm.enums;


/**
 * @author Kevin Hwang
 * @since 8/10/2018
 */
public enum RetryMqMessageStatus {

	SENDING(1), SUCCEEDED(2), FAILED(3);

	private int status;

	RetryMqMessageStatus(int status) {
		this.status = status;
	}

	public int getStatus() {
		return status;
	}
}
