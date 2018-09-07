package org.jackson.puppy.tcc.transaction.spring.support;

import org.jackson.puppy.tcc.transaction.TransactionManager;
import org.jackson.puppy.tcc.transaction.TransactionRepository;
import org.jackson.puppy.tcc.transaction.recover.RecoverConfig;
import org.jackson.puppy.tcc.transaction.spring.recover.DefaultRecoverConfig;
import org.jackson.puppy.tcc.transaction.support.TransactionConfigurator;

/**
 * @author Kevin Hwang
 * @since 8/10/2018
 */
public class SpringTccTransactionConfig implements TransactionConfigurator {

	private TransactionRepository transactionRepository;

	private TransactionManager transactionManager;

	private RecoverConfig recoverConfig = DefaultRecoverConfig.INSTANCE;

	@Override
	public TransactionManager getTransactionManager() {
		return transactionManager;
	}

	public void setTransactionManager(TransactionManager transactionManager) {
		this.transactionManager = transactionManager;
	}

	@Override
	public TransactionRepository getTransactionRepository() {
		return transactionRepository;
	}

	public void setTransactionRepository(TransactionRepository transactionRepository) {
		this.transactionRepository = transactionRepository;
	}

	@Override
	public RecoverConfig getRecoverConfig() {
		return recoverConfig;
	}

	public void setRecoverConfig(RecoverConfig recoverConfig) {
		this.recoverConfig = recoverConfig;
	}
}
