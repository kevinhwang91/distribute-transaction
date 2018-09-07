package org.jackson.puppy.tcc.transaction.recover;

import org.jackson.puppy.tcc.transaction.Transaction;
import org.jackson.puppy.tcc.transaction.TransactionManager;
import org.jackson.puppy.tcc.transaction.TransactionRepository;
import org.jackson.puppy.tcc.transaction.api.TransactionStatus;
import org.jackson.puppy.tcc.transaction.common.TransactionType;
import org.jackson.puppy.tcc.transaction.support.TransactionConfigurator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Calendar;
import java.util.Date;
import java.util.List;

/**
 * @author Kevin Hwang
 * @since 8/10/2018
 */
public class TransactionRecovery {

	private final Logger logger = LoggerFactory.getLogger(getClass());

	private TransactionConfigurator transactionConfigurator;

	public void startRecover() {

		List<Transaction> transactions = loadErrorTransactions();

		recoverErrorTransactions(transactions);
	}

	private List<Transaction> loadErrorTransactions() {

		long currentTimeInMillis = Calendar.getInstance().getTimeInMillis();

		TransactionRepository transactionRepository = transactionConfigurator.getTransactionRepository();
		RecoverConfig recoverConfig = transactionConfigurator.getRecoverConfig();

		return transactionRepository.findAllUnmodifiedSince(new Date(currentTimeInMillis - recoverConfig.getRecoverDuration() * 1000));
	}

	private void recoverErrorTransactions(List<Transaction> transactions) {

		for (Transaction transaction : transactions) {

			RecoverConfig recoverConfig = transactionConfigurator.getRecoverConfig();
			if (transaction.getRetriedCount() > recoverConfig.getMaxRetryCount()) {
				logger.error("Recover failed with max retry count,will not try again. txid: {}, status: {}, retried count: {}.", transaction.getXid(), transaction.getStatus().getId(), transaction.getRetriedCount());
				continue;
			}

			if (transaction.getTransactionType().equals(TransactionType.BRANCH)
					&& (transaction.getCreateTime().getTime() +
					recoverConfig.getMaxRetryCount() *
							recoverConfig.getRecoverDuration() * 1000
					> System.currentTimeMillis())) {
				continue;
			}

			try {
				transaction.addRetriedCount();

				TransactionRepository transactionRepository = transactionConfigurator.getTransactionRepository();
				TransactionManager transactionManager = transactionConfigurator.getTransactionManager();

				if (transaction.getStatus().equals(TransactionStatus.CONFIRMING)) {

					transaction.changeStatus(TransactionStatus.CONFIRMING);
					transactionRepository.update(transaction);
					transactionManager.commit0(transaction);
					transactionRepository.delete(transaction);

				} else if (transaction.getStatus().equals(TransactionStatus.CANCELLING)
						|| transaction.getTransactionType().equals(TransactionType.ROOT)) {

					transaction.changeStatus(TransactionStatus.CANCELLING);
					transactionRepository.update(transaction);
					transactionManager.rollback0(transaction);
					transactionRepository.delete(transaction);
				}

			} catch (Throwable throwable) {
				logger.error("Recover failed, txid: {}, status: {}, retried count: {}.", transaction.getXid(), transaction.getStatus().getId(), transaction.getRetriedCount(), throwable);
			}
		}
	}

	public void setTransactionConfigurator(TransactionConfigurator transactionConfigurator) {
		this.transactionConfigurator = transactionConfigurator;
	}
}
