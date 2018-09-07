package org.jackson.puppy.tcc.transaction.api;

import java.lang.reflect.Method;

/**
 * @author Kevin Hwang
 * @since 8/10/2018
 */
public interface TransactionContextEditor {

	TransactionContext get(Object target, Method method, Object[] args);

	void set(TransactionContext transactionContext, Object target, Method method, Object[] args);

	void clear();
}
