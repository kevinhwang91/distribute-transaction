package org.jackson.puppy.tcc.transaction.spring.aspect;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.jackson.puppy.tcc.transaction.TransactionManager;
import org.jackson.puppy.tcc.transaction.interceptor.TccTransactionInterceptor;
import org.jackson.puppy.tcc.transaction.support.TransactionConfigurator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;

/**
 * @author Kevin Hwang
 * @since 8/10/2018
 */
@Aspect
@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class TccTransactionAspect {

	private final TransactionConfigurator transactionConfigurator;
	private TccTransactionInterceptor tccTransactionInterceptor;

	@Autowired
	public TccTransactionAspect(TransactionConfigurator transactionConfigurator) {
		this.transactionConfigurator = transactionConfigurator;
	}

	@Around("@annotation(org.jackson.puppy.tcc.transaction.api.TccTransactional)")
	public Object interceptTccTransactionMethod(ProceedingJoinPoint pjp) throws Throwable {
		return tccTransactionInterceptor.interceptTccTransactionMethod(pjp);
	}

	@PostConstruct
	private void init() {
		TransactionManager transactionManager = transactionConfigurator.getTransactionManager();
		TccTransactionInterceptor tccTransactionInterceptor = new TccTransactionInterceptor();
		tccTransactionInterceptor.setTransactionManager(transactionManager);
		tccTransactionInterceptor.setDelayCancelExceptions(transactionConfigurator.getRecoverConfig().getDelayCancelExceptions());
		this.tccTransactionInterceptor = tccTransactionInterceptor;
	}

}
