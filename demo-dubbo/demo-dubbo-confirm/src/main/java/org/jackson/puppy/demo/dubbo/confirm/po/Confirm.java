package org.jackson.puppy.demo.dubbo.confirm.po;

import java.math.BigDecimal;

/**
 * @author Kevin Hwang
 * @since 8/10/2018
 */
public class Confirm {

	private Long id;

	private String orderNumber;

	private BigDecimal agencyFee;

	private BigDecimal unit;

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
