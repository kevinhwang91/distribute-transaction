package org.jackson.puppy.demo.dubbo.holdings.po;

import java.util.Date;

/**
 * @author Kevin Hwang
 * @since 8/10/2018
 */
public class HoldingsResource {

	private Long id;

	private String orderNumber;

	private Integer status;

	private Date createdDate;

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getOrderNumber() {
		return orderNumber;
	}

	public void setOrderNumber(String orderNumber) {
		this.orderNumber = orderNumber;
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
}
