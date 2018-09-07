package org.jackson.puppy.demo.dubbo.holdings.api.dto;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

/**
 * @author Kevin Hwang
 * @since 8/10/2018
 */
public class HoldingsDto implements Serializable {

	private static final long serialVersionUID = -4513592463864419021L;

	private String accountNumber;

	private BigDecimal unit;

	private String orderNumber;

	private Date date;

	public static long getSerialVersionUID() {
		return serialVersionUID;
	}

	public String getAccountNumber() {
		return accountNumber;
	}

	public void setAccountNumber(String accountNumber) {
		this.accountNumber = accountNumber;
	}

	public BigDecimal getUnit() {
		return unit;
	}

	public void setUnit(BigDecimal unit) {
		this.unit = unit;
	}

	public String getOrderNumber() {
		return orderNumber;
	}

	public void setOrderNumber(String orderNumber) {
		this.orderNumber = orderNumber;
	}

	public Date getDate() {
		return date;
	}

	public void setDate(Date date) {
		this.date = date;
	}
}
