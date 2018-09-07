package org.jackson.puppy.tcc.transaction.repository;


import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import org.jackson.puppy.tcc.transaction.Transaction;
import org.jackson.puppy.tcc.transaction.TransactionRepository;
import org.jackson.puppy.tcc.transaction.api.TransactionXid;

import javax.transaction.xa.Xid;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * @author Kevin Hwang
 * @since 8/10/2018
 */
public abstract class AbstractTransactionCacheRepository implements TransactionRepository {

	private int expireDuration = 120;

	private Cache<Xid, Transaction> transactionXidTccTransactionCache;

	public AbstractTransactionCacheRepository() {
		transactionXidTccTransactionCache = Caffeine.newBuilder().expireAfterAccess(expireDuration, TimeUnit.SECONDS).maximumSize(1000).build();
	}

	@Override
	public int create(Transaction transaction) {
		int result = doCreate(transaction);
		if (result > 0) {
			putToCache(transaction);
		}
		return result;
	}

	@Override
	public int update(Transaction transaction) {
		int result = 0;

		try {
			result = doUpdate(transaction);
			if (result > 0) {
				putToCache(transaction);
			}
		} finally {
			if (result <= 0) {
				removeFromCache(transaction);
			}
		}

		return result;
	}

	@Override
	public int delete(Transaction transaction) {
		int result = 0;

		try {
			result = doDelete(transaction);

		} finally {
			removeFromCache(transaction);
		}
		return result;
	}

	@Override
	public Transaction findByXid(TransactionXid transactionXid) {
		Transaction transaction = findFromCache(transactionXid);

		if (transaction == null) {
			transaction = doFindOne(transactionXid);

			if (transaction != null) {
				putToCache(transaction);
			}
		}

		return transaction;
	}

	@Override
	public List<Transaction> findAllUnmodifiedSince(Date date) {

		List<Transaction> transactions = doFindAllUnmodifiedSince(date);

		for (Transaction transaction : transactions) {
			putToCache(transaction);
		}

		return transactions;
	}

	protected void putToCache(Transaction transaction) {
		transactionXidTccTransactionCache.put(transaction.getXid(), transaction);
	}

	protected void removeFromCache(Transaction transaction) {
		transactionXidTccTransactionCache.invalidate(transaction.getXid());
	}

	protected Transaction findFromCache(TransactionXid transactionXid) {
		return transactionXidTccTransactionCache.getIfPresent(transactionXid);
	}

	public void setExpireDuration(int durationInSeconds) {
		this.expireDuration = durationInSeconds;
	}

	protected abstract int doCreate(Transaction transaction);

	protected abstract int doUpdate(Transaction transaction);

	protected abstract int doDelete(Transaction transaction);

	protected abstract Transaction doFindOne(Xid xid);

	protected abstract List<Transaction> doFindAllUnmodifiedSince(Date date);
}
