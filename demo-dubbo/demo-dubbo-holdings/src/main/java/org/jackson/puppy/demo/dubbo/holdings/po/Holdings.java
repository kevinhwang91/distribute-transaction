package org.jackson.puppy.demo.dubbo.holdings.po;

import java.math.BigDecimal;

/**
 * @author Kevin Hwang
 * @since 8/10/2018
 */
public class Holdings {

	private Long id;

	private String accountNumber;

	private BigDecimal unit;

	private BigDecimal freezeUnit;

	private Integer version;

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
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

	public BigDecimal getFreezeUnit() {
		return freezeUnit;
	}

	public void setFreezeUnit(BigDecimal freezeUnit) {
		this.freezeUnit = freezeUnit;
	}

	public Integer getVersion() {
		return version;
	}

	public void setVersion(Integer version) {
		this.version = version;
	}
}
