package org.jackson.puppy.tcc.transaction.api;

/**
 * @author Kevin Hwang
 * @since 8/10/2018
 */
public enum Propagation {
	REQUIRED(0),
	SUPPORTS(1),
	MANDATORY(2),
	REQUIRES_NEW(3);

	private final int value;

	private Propagation(int value) {
		this.value = value;
	}

	public int value() {
		return this.value;
	}
}