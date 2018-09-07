package org.jackson.puppy.tcc.transaction;

import org.jackson.puppy.tcc.transaction.api.TransactionContext;
import org.jackson.puppy.tcc.transaction.api.TransactionStatus;
import org.jackson.puppy.tcc.transaction.api.exception.CancellingException;
import org.jackson.puppy.tcc.transaction.api.exception.ConfirmingException;
import org.jackson.puppy.tcc.transaction.api.exception.NoExistedTransactionException;
import org.jackson.puppy.tcc.transaction.api.exception.TccException;
import org.jackson.puppy.tcc.transaction.common.TransactionType;
import org.jackson.puppy.tcc.transaction.utils.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Deque;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

/**
 * @author Kevin Hwang
 * @since 8/10/2018
 */
public class TransactionManager {

	private static final ThreadLocal<Deque<Transaction>> CURRENT = new ThreadLocal<>();
	private final Logger logger = LoggerFactory.getLogger(getClass());
	private TransactionRepository transactionRepository;

	private Executor tccExecutor;

	public void setTransactionRepository(TransactionRepository transactionRepository) {
		this.transactionRepository = transactionRepository;
	}

	public void setTccExecutor(Executor tccExecutor) {
		this.tccExecutor = tccExecutor;
	}

	public Transaction begin() {

		Transaction transaction = new Transaction(TransactionType.ROOT);
		transactionRepository.create(transaction);
		registerTransaction(transaction);
		return transaction;
	}

	public Transaction propagationNewBegin(TransactionContext transactionContext) {

		Transaction transaction = new Transaction(transactionContext);
		transactionRepository.create(transaction);

		registerTransaction(transaction);
		return transaction;
	}

	public Transaction propagationExistBegin(TransactionContext transactionContext) throws NoExistedTransactionException {
		Transaction transaction = transactionRepository.findByXid(transactionContext.getXid());
		if (transaction != null) {
			transaction.changeStatus(TransactionStatus.valueOf(transactionContext.getStatus()));
			registerTransaction(transaction);
			return transaction;
		} else {
			throw new NoExistedTransactionException();
		}
	}

	public void commit() {

		Transaction transaction = getCurrentTransaction();

		transaction.changeStatus(TransactionStatus.CONFIRMING);

		transactionRepository.update(transaction);

		try {
			commit0(transaction);
			transactionRepository.delete(transaction);
		} catch (Throwable commitException) {
			logger.error("Tcc transaction confirm failed.", commitException);
			throw new ConfirmingException(commitException);
		}
	}

	public Transaction getCurrentTransaction() {
		if (isTransactionActive()) {
			return CURRENT.get().peek();
		}
		return null;
	}

	public boolean isTransactionActive() {
		Deque<Transaction> transactions = CURRENT.get();
		return !CollectionUtils.isEmpty(transactions);
	}

	public void rollback() {

		Transaction transaction = getCurrentTransaction();
		transaction.changeStatus(TransactionStatus.CANCELLING);

		transactionRepository.update(transaction);

		try {
			rollback0(transaction);
			transactionRepository.delete(transaction);
		} catch (Throwable rollbackException) {
			logger.error("Tcc transaction rollback failed.", rollbackException);
			throw new CancellingException(rollbackException);
		}
	}

	private void registerTransaction(Transaction transaction) {

		if (CURRENT.get() == null) {
			CURRENT.set(new LinkedList<>());
		}

		CURRENT.get().push(transaction);
	}

	public void cleanAfterCompletion(Transaction transaction) {
		if (isTransactionActive() && transaction != null) {
			Transaction currentTransaction = getCurrentTransaction();
			if (currentTransaction == transaction) {
				CURRENT.get().pop();
			} else {
				throw new TccException("Illegal transaction when clean after completion");
			}
		}
	}


	public void enlistParticipant(Participant participant) {
		Transaction transaction = this.getCurrentTransaction();
		transaction.enlistParticipant(participant);
		transactionRepository.update(transaction);
	}

	public void commit0(Transaction transaction) {
		List<Participant> participants = transaction.getParticipants();

		if (tccExecutor == null || participants.size() == 1) {
			participants.forEach(Participant::commit);
		} else {
			CompletableFuture[] completableFutures =
					participants
							.stream()
							.map(participant -> CompletableFuture.runAsync(participant::commit, tccExecutor))
							.toArray(CompletableFuture[]::new);
			CompletableFuture.allOf(completableFutures).join();
		}
	}

	public void rollback0(Transaction transaction) {
		List<Participant> participants = transaction.getParticipants();

		if (tccExecutor == null || participants.size() == 1) {
			participants.forEach(Participant::rollback);
		} else {
			CompletableFuture[] completableFutures =
					participants
							.stream()
							.map(participant -> CompletableFuture.runAsync(participant::rollback, tccExecutor))
							.toArray(CompletableFuture[]::new);
			CompletableFuture.allOf(completableFutures).join();
		}
	}
}
