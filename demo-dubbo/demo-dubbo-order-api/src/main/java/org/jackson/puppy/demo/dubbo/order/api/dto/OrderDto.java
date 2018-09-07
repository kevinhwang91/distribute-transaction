package org.jackson.puppy.demo.dubbo.order.api.dto;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * @author Kevin Hwang
 * @since 8/10/2018
 */
public class OrderDto implements Serializable {

	private static final long serialVersionUID = 8898593908032088669L;

	private String orderNumber;

	private BigDecimal agencyFee;

	private BigDecimal unit;

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

	public BigDecimal getUnit() {
		return unit;
	}

	public void setUnit(BigDecimal unit) {
		this.unit = unit;
	}
}
