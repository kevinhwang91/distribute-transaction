package org.jackson.puppy.tcc.transaction;

import org.jackson.puppy.tcc.transaction.api.TransactionXid;

import java.util.Date;
import java.util.List;

/**
 * @author Kevin Hwang
 * @since 8/10/2018
 */
public interface TransactionRepository {

	int create(Transaction transaction);

	int update(Transaction transaction);

	int delete(Transaction transaction);

	Transaction findByXid(TransactionXid xid);

	List<Transaction> findAllUnmodifiedSince(Date date);
}
