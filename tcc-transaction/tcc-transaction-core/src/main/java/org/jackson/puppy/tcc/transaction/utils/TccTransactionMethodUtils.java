package org.jackson.puppy.tcc.transaction.utils;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.reflect.MethodSignature;
import org.jackson.puppy.tcc.transaction.api.Propagation;
import org.jackson.puppy.tcc.transaction.api.TccTransactional;
import org.jackson.puppy.tcc.transaction.api.TransactionContext;
import org.jackson.puppy.tcc.transaction.common.MethodType;

import java.lang.reflect.Method;

/**
 * @author Kevin Hwang
 * @since 8/10/2018
 */
public class TccTransactionMethodUtils {

	public static Method getTccTransactionMethod(ProceedingJoinPoint pjp) {
		Method method = ((MethodSignature) (pjp.getSignature())).getMethod();

		if (method.getAnnotation(TccTransactional.class) == null) {
			try {
				method = pjp.getTarget().getClass().getMethod(method.getName(), method.getParameterTypes());
			} catch (NoSuchMethodException e) {
				return null;
			}
		}
		return method;
	}

	public static MethodType calculateMethodType(Propagation propagation, boolean isTransactionActive, TransactionContext transactionContext) {

		if ((propagation.equals(Propagation.REQUIRED) && !isTransactionActive && transactionContext == null) ||
				propagation.equals(Propagation.REQUIRES_NEW)) {
			return MethodType.ROOT;
		} else if ((propagation.equals(Propagation.REQUIRED) || propagation.equals(Propagation.MANDATORY)) && !isTransactionActive && transactionContext != null) {
			return MethodType.PROVIDER;
		} else {
			return MethodType.NORMAL;
		}
	}

	public static MethodType calculateMethodType(TransactionContext transactionContext, boolean isTcc) {

		if (transactionContext == null && isTcc) {
			//isRootTransactionMethod
			return MethodType.ROOT;
		} else if (transactionContext == null) {
			//isSoaConsumer
			return MethodType.CONSUMER;
		} else if (isTcc) {
			//isSoaProvider
			return MethodType.PROVIDER;
		} else {
			return MethodType.NORMAL;
		}
	}

	public static int getTransactionContextParamPosition(Class<?>[] parameterTypes) {

		int position = -1;

		for (int i = 0; i < parameterTypes.length; i++) {
			if (parameterTypes[i].equals(org.jackson.puppy.tcc.transaction.api.TransactionContext.class)) {
				position = i;
				break;
			}
		}
		return position;
	}

	public static TransactionContext getTransactionContextFromArgs(Object[] args) {

		TransactionContext transactionContext = null;

		for (Object arg : args) {
			if (arg != null && org.jackson.puppy.tcc.transaction.api.TransactionContext.class.isAssignableFrom(arg.getClass())) {

				transactionContext = (org.jackson.puppy.tcc.transaction.api.TransactionContext) arg;
			}
		}

		return transactionContext;
	}
}
