package org.jackson.puppy.demo.dubbo.confirm.po;

import java.util.Date;

/**
 * @author Kevin Hwang
 * @since 8/10/2018
 */
public class RetryMqMessage {

	private String id;

	private byte[] message;

	private Integer status;

	private Date createdDate;

	private Integer retry;

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public byte[] getMessage() {
		return message;
	}

	public void setMessage(byte[] message) {
		this.message = message;
	}

	public Integer getStatus() {
		return status;
	}

	public void setStatus(Integer status) {
		this.status = status;
	}

	public Date getCreatedDate() {
		return createdDate;
	}

	public void setCreatedDate(Date createdDate) {
		this.createdDate = createdDate;
	}

	public Integer getRetry() {
		return retry;
	}

	public void setRetry(Integer retry) {
		this.retry = retry;
	}
}
