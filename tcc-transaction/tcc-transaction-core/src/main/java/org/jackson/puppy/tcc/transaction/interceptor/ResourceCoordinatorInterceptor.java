package org.jackson.puppy.tcc.transaction.interceptor;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.reflect.MethodSignature;
import org.jackson.puppy.tcc.transaction.InvocationContext;
import org.jackson.puppy.tcc.transaction.Participant;
import org.jackson.puppy.tcc.transaction.Transaction;
import org.jackson.puppy.tcc.transaction.TransactionManager;
import org.jackson.puppy.tcc.transaction.api.*;
import org.jackson.puppy.tcc.transaction.support.FactoryBuilder;
import org.jackson.puppy.tcc.transaction.utils.ReflectionUtils;
import org.jackson.puppy.tcc.transaction.utils.TccTransactionMethodUtils;

import java.lang.reflect.Method;

/**
 * @author Kevin Hwang
 * @since 8/10/2018
 */
public class ResourceCoordinatorInterceptor {

	private TransactionManager transactionManager;


	public void setTransactionManager(TransactionManager transactionManager) {
		this.transactionManager = transactionManager;
	}

	public Object interceptTransactionContextMethod(ProceedingJoinPoint pjp) throws Throwable {

		Transaction transaction = transactionManager.getCurrentTransaction();

		if (transaction != null) {

			switch (transaction.getStatus()) {
				case TRYING:
					enlistParticipant(pjp);
					break;
				case CONFIRMING:
					break;
				case CANCELLING:
					break;
				default:
			}
		}

		return pjp.proceed(pjp.getArgs());
	}

	private void enlistParticipant(ProceedingJoinPoint pjp) {

		Method method = TccTransactionMethodUtils.getTccTransactionMethod(pjp);
		if (method == null) {
			throw new RuntimeException(String.format("join point not found method, point is : %s", pjp.getSignature().getName()));
		}
		TccTransactional tccTransactional = method.getAnnotation(TccTransactional.class);

		String confirmMethodName = tccTransactional.confirmMethod();
		String cancelMethodName = tccTransactional.cancelMethod();

		Transaction transaction = transactionManager.getCurrentTransaction();

		TransactionXid xid;
		TransactionContextEditor contextEditor = FactoryBuilder.factoryOf(tccTransactional.transactionContextEditor()).getInstance();
		TransactionContext transactionContext = contextEditor.get(pjp.getTarget(), method, pjp.getArgs());
		if (transactionContext == null) {
			xid = new TransactionXid(transaction.getXid().getGlobalTransactionId());
			contextEditor.set(new TransactionContext(xid, TransactionStatus.TRYING.getId()), pjp.getTarget(), ((MethodSignature) pjp.getSignature()).getMethod(), pjp.getArgs());
		} else {
			xid = transactionContext.getXid();
		}

		Class targetClass = ReflectionUtils.getDeclaringType(pjp.getTarget().getClass(), method.getName(), method.getParameterTypes());

		InvocationContext confirmInvocation = new InvocationContext(targetClass,
				confirmMethodName,
				method.getParameterTypes(), pjp.getArgs());

		InvocationContext cancelInvocation = new InvocationContext(targetClass,
				cancelMethodName,
				method.getParameterTypes(), pjp.getArgs());

		Participant participant =
				new Participant(
						xid,
						confirmInvocation,
						cancelInvocation,
						tccTransactional.transactionContextEditor());

		transactionManager.enlistParticipant(participant);

	}


}
