package org.jackson.puppy.tcc.transaction.interceptor;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.reflect.MethodSignature;
import org.jackson.puppy.tcc.transaction.Transaction;
import org.jackson.puppy.tcc.transaction.TransactionManager;
import org.jackson.puppy.tcc.transaction.api.*;
import org.jackson.puppy.tcc.transaction.api.exception.NoExistedTransactionException;
import org.jackson.puppy.tcc.transaction.api.exception.TccException;
import org.jackson.puppy.tcc.transaction.common.MethodType;
import org.jackson.puppy.tcc.transaction.support.FactoryBuilder;
import org.jackson.puppy.tcc.transaction.utils.ReflectionUtils;
import org.jackson.puppy.tcc.transaction.utils.TccTransactionMethodUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Method;
import java.util.Set;

/**
 * @author Kevin Hwang
 * @since 8/10/2018
 */
public class TccTransactionInterceptor {

	private final Logger logger = LoggerFactory.getLogger(getClass());

	private TransactionManager transactionManager;

	private Set<Class<? extends Exception>> delayCancelExceptions;

	public void setTransactionManager(TransactionManager transactionManager) {
		this.transactionManager = transactionManager;
	}

	public void setDelayCancelExceptions(Set<Class<? extends Exception>> delayCancelExceptions) {
		this.delayCancelExceptions = delayCancelExceptions;
	}

	public Object interceptTccTransactionMethod(ProceedingJoinPoint pjp) throws Throwable {

		Method method = TccTransactionMethodUtils.getTccTransactionMethod(pjp);

		TccTransactional tccTransactional = method.getAnnotation(TccTransactional.class);
		Propagation propagation = tccTransactional.propagation();

		TransactionContextEditor contextEditor = FactoryBuilder.factoryOf(tccTransactional.transactionContextEditor()).getInstance();
		TransactionContext transactionContext = contextEditor.get(pjp.getTarget(), method, pjp.getArgs());

		boolean isTransactionActive = transactionManager.isTransactionActive();

		if (propagation.equals(Propagation.MANDATORY) && !isTransactionActive && transactionContext == null) {
			throw new TccException("no active tccTransactional transaction while propagation is mandatory for method " + method.getName());
		}

		MethodType methodType = TccTransactionMethodUtils.calculateMethodType(propagation, isTransactionActive, transactionContext);

		try {
			switch (methodType) {
				case ROOT:
					return rootMethodProceed(pjp);
				case PROVIDER:
					return providerMethodProceed(pjp, transactionContext);
				default:
					return pjp.proceed();
			}
		} finally {
			contextEditor.clear();
		}
	}


	private Object rootMethodProceed(ProceedingJoinPoint pjp) throws Throwable {

		Object returnValue;

		Transaction transaction = null;

		try {

			transaction = transactionManager.begin();

			try {
				returnValue = pjp.proceed();
			} catch (Throwable ex) {

				if (isDelayCancelException(ex)) {

				} else {
					logger.info("Tcc transaction trying failed. transaction txid: {}.", transaction.getXid(), ex);

					transactionManager.rollback();
				}

				throw ex;
			}

			transactionManager.commit();

		} finally {
			transactionManager.cleanAfterCompletion(transaction);
		}

		return returnValue;
	}

	private Object providerMethodProceed(ProceedingJoinPoint pjp, TransactionContext transactionContext) throws Throwable {

		Transaction transaction = null;
		try {
			switch (TransactionStatus.valueOf(transactionContext.getStatus())) {
				case TRYING:
					transaction = transactionManager.propagationNewBegin(transactionContext);
					return pjp.proceed();
				case CONFIRMING:
					try {
						transaction = transactionManager.propagationExistBegin(transactionContext);
						transactionManager.commit();
					} catch (NoExistedTransactionException exception) {
						//the transaction has been commit,ignore it.
					}
					break;
				case CANCELLING:

					try {
						transaction = transactionManager.propagationExistBegin(transactionContext);
						transactionManager.rollback();
					} catch (NoExistedTransactionException exception) {
						//the transaction has been rollback,ignore it.
					}
					break;
				default:
			}

		} finally {
			transactionManager.cleanAfterCompletion(transaction);
		}

		Method method = ((MethodSignature) (pjp.getSignature())).getMethod();

		return ReflectionUtils.getNullValue(method.getReturnType());
	}

	private boolean isDelayCancelException(Throwable throwable) {

		if (delayCancelExceptions != null) {
			for (Class delayCancelException : delayCancelExceptions) {

				if (delayCancelException.isAssignableFrom(throwable.getClass())) {
					return true;
				}
			}
		}

		return false;
	}

}
