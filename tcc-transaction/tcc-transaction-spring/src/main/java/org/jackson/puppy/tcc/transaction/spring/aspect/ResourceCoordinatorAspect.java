package org.jackson.puppy.tcc.transaction.spring.aspect;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.jackson.puppy.tcc.transaction.interceptor.ResourceCoordinatorInterceptor;
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
@Order(Ordered.HIGHEST_PRECEDENCE + 1)
public class ResourceCoordinatorAspect {

	private final TransactionConfigurator transactionConfigurator;
	private ResourceCoordinatorInterceptor resourceCoordinatorInterceptor;

	@Autowired
	public ResourceCoordinatorAspect(TransactionConfigurator transactionConfigurator) {
		this.transactionConfigurator = transactionConfigurator;
	}

	@Around("@annotation(org.jackson.puppy.tcc.transaction.api.TccTransactional)")
	public Object interceptTransactionContextMethod(ProceedingJoinPoint pjp) throws Throwable {
		return resourceCoordinatorInterceptor.interceptTransactionContextMethod(pjp);
	}

	@PostConstruct
	private void init() {
		ResourceCoordinatorInterceptor resourceCoordinatorInterceptor = new ResourceCoordinatorInterceptor();
		resourceCoordinatorInterceptor.setTransactionManager(transactionConfigurator.getTransactionManager());
		this.resourceCoordinatorInterceptor = resourceCoordinatorInterceptor;
	}
}
