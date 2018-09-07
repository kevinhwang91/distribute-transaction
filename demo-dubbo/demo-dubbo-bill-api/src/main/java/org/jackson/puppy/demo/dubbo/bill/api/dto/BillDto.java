package org.jackson.puppy.demo.dubbo.bill.api.dto;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

/**
 * @author Kevin Hwang
 * @since 8/10/2018
 */
public class BillDto implements Serializable {

	private static final long serialVersionUID = -8738179636598913792L;

	private String orderNumber;

	private BigDecimal agencyFee;

	private Date date;

	public static long getSerialVersionUID() {
		return serialVersionUID;
	}

	public String getOrderNumber() {
		return orderNumber;
	}

	public void setOrderNumber(String orderNumber) {
		this.orderNumber = orderNumber;
	}

	public BigDecimal getAgencyFee() {
		return agencyFee;
	}

	public void setAgencyFee(BigDecimal agencyFee) {
		this.agencyFee = agencyFee;
	}

	public Date getDate() {
		return date;
	}

	public void setDate(Date date) {
		this.date = date;
	}
}
