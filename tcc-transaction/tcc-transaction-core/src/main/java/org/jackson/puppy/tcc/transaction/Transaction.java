package org.jackson.puppy.tcc.transaction;


import org.jackson.puppy.tcc.transaction.api.TransactionContext;
import org.jackson.puppy.tcc.transaction.api.TransactionStatus;
import org.jackson.puppy.tcc.transaction.api.TransactionXid;
import org.jackson.puppy.tcc.transaction.common.TransactionType;

import javax.transaction.xa.Xid;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author Kevin Hwang
 * @since 8/10/2018
 */
public class Transaction implements Serializable {

	private static final long serialVersionUID = 6319729917297963275L;

	private TransactionXid xid;

	private TransactionStatus status;

	private TransactionType transactionType;

	private volatile int retriedCount = 0;

	private Date createTime = new Date();

	private Date lastUpdateTime = new Date();

	private long version = 1;

	private List<Participant> participants = new ArrayList<Participant>();

	private Map<String, Object> attachments = new ConcurrentHashMap<String, Object>();

	public Transaction() {

	}

	public Transaction(TransactionContext transactionContext) {
		this.xid = transactionContext.getXid();
		this.status = TransactionStatus.TRYING;
		this.transactionType = TransactionType.BRANCH;
	}

	public Transaction(TransactionType transactionType) {
		this.xid = new TransactionXid();
		this.status = TransactionStatus.TRYING;
		this.transactionType = transactionType;
	}

	public void enlistParticipant(Participant participant) {
		participants.add(participant);
	}


	public Xid getXid() {
		return xid.clone();
	}

	public TransactionStatus getStatus() {
		return status;
	}


	public List<Participant> getParticipants() {
		return participants;
	}

	public TransactionType getTransactionType() {
		return transactionType;
	}

	public void changeStatus(TransactionStatus status) {
		this.status = status;
	}


	public int getRetriedCount() {
		return retriedCount;
	}

	public void addRetriedCount() {
		this.retriedCount++;
	}

	public void resetRetriedCount(int retriedCount) {
		this.retriedCount = retriedCount;
	}

	public Map<String, Object> getAttachments() {
		return attachments;
	}

	public long getVersion() {
		return version;
	}

	public void setVersion(long version) {
		this.version = version;
	}

	public void updateVersion() {
		this.version++;
	}

	public Date getLastUpdateTime() {
		return lastUpdateTime;
	}

	public void setLastUpdateTime(Date date) {
		this.lastUpdateTime = date;
	}

	public Date getCreateTime() {
		return createTime;
	}

	public void updateTime() {
		this.lastUpdateTime = new Date();
	}


}
