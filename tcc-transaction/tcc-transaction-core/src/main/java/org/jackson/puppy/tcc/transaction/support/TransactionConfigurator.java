package org.jackson.puppy.tcc.transaction.support;

import org.jackson.puppy.tcc.transaction.TransactionManager;
import org.jackson.puppy.tcc.transaction.TransactionRepository;
import org.jackson.puppy.tcc.transaction.recover.RecoverConfig;

/**
 * @author Kevin Hwang
 * @since 8/10/2018
 */
public interface TransactionConfigurator {

	TransactionManager getTransactionManager();

	TransactionRepository getTransactionRepository();

	RecoverConfig getRecoverConfig();
}
