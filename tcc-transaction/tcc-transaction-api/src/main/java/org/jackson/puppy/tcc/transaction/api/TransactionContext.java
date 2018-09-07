package org.jackson.puppy.tcc.transaction.api;

import java.io.Serializable;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author Kevin Hwang
 * @since 8/10/2018
 */
public class TransactionContext implements Serializable {

	private static final long serialVersionUID = -8199390103169700387L;
	private TransactionXid xid;

	private int status;

	private Map<String, String> attachments = new ConcurrentHashMap<>();

	public TransactionContext() {

	}

	public TransactionContext(TransactionXid xid, int status) {
		this.xid = xid;
		this.status = status;
	}

	public TransactionXid getXid() {
		return xid.clone();
	}

	public void setXid(TransactionXid xid) {
		this.xid = xid;
	}

	public Map<String, String> getAttachments() {
		return attachments;
	}

	public void setAttachments(Map<String, String> attachments) {
		this.attachments = attachments;
	}

	public int getStatus() {
		return status;
	}

	public void setStatus(int status) {
		this.status = status;
	}


}
