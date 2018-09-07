package org.jackson.puppy.tcc.transaction.common;

/**
 * @author Kevin Hwang
 * @since 8/10/2018
 */
public enum TransactionType {

	ROOT(1),
	BRANCH(2);

	int id;

	TransactionType(int id) {
		this.id = id;
	}

	public static TransactionType valueOf(int id) {
		switch (id) {
			case 1:
				return ROOT;
			case 2:
				return BRANCH;
			default:
				return null;
		}
	}

	public int getId() {
		return id;
	}

}
