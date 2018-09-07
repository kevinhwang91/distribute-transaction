package org.jackson.puppy.tcc.transaction;

import org.jackson.puppy.tcc.transaction.api.TransactionContext;
import org.jackson.puppy.tcc.transaction.api.TransactionContextEditor;
import org.jackson.puppy.tcc.transaction.api.exception.TccException;
import org.jackson.puppy.tcc.transaction.support.FactoryBuilder;
import org.jackson.puppy.tcc.transaction.utils.StringUtils;

import java.io.Serializable;
import java.lang.reflect.Method;

/**
 * @author Kevin Hwang
 * @since 8/10/2018
 */
public class Terminator implements Serializable {

	private static final long serialVersionUID = 6635286036433093396L;

	public Terminator() {

	}

	public Object invoke(TransactionContext transactionContext, InvocationContext invocationContext, Class<? extends TransactionContextEditor> transactionContextEditorClass) {


		if (StringUtils.isNotEmpty(invocationContext.getMethodName())) {

			try {

				Object target = FactoryBuilder.factoryOf(invocationContext.getTargetClass()).getInstance();

				Method method = target.getClass().getMethod(invocationContext.getMethodName(), invocationContext.getParameterTypes());

				FactoryBuilder.factoryOf(transactionContextEditorClass).getInstance().set(transactionContext, target, method, invocationContext.getArgs());

				return method.invoke(target, invocationContext.getArgs());

			} catch (Exception e) {
				throw new TccException(e);
			}
		}
		return null;
	}
}
