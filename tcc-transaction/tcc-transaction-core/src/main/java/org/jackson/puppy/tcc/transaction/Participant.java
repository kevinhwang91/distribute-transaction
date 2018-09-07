package org.jackson.puppy.tcc.transaction;

import org.jackson.puppy.tcc.transaction.api.TransactionContext;
import org.jackson.puppy.tcc.transaction.api.TransactionContextEditor;
import org.jackson.puppy.tcc.transaction.api.TransactionStatus;
import org.jackson.puppy.tcc.transaction.api.TransactionXid;

import java.io.Serializable;

/**
 * @author Kevin Hwang
 * @since 8/10/2018
 */
public class Participant implements Serializable {

	private static final long serialVersionUID = 3200160593010877205L;

	private Class<? extends TransactionContextEditor> transactionContextEditorClass;

	private TransactionXid xid;

	private InvocationContext confirmInvocationContext;

	private InvocationContext cancelInvocationContext;

	private Terminator terminator = new Terminator();

	public Participant() {

	}

	public Participant(TransactionXid xid, InvocationContext confirmInvocationContext, InvocationContext cancelInvocationContext, Class<? extends TransactionContextEditor> transactionContextEditorClass) {
		this.xid = xid;
		this.confirmInvocationContext = confirmInvocationContext;
		this.cancelInvocationContext = cancelInvocationContext;
		this.transactionContextEditorClass = transactionContextEditorClass;
	}

	public Participant(InvocationContext confirmInvocationContext, InvocationContext cancelInvocationContext, Class<? extends TransactionContextEditor> transactionContextEditorClass) {
		this.confirmInvocationContext = confirmInvocationContext;
		this.cancelInvocationContext = cancelInvocationContext;
		this.transactionContextEditorClass = transactionContextEditorClass;
	}

	public void rollback() {
		terminator.invoke(new TransactionContext(xid, TransactionStatus.CANCELLING.getId()), cancelInvocationContext, transactionContextEditorClass);
	}

	public void commit() {
		terminator.invoke(new TransactionContext(xid, TransactionStatus.CONFIRMING.getId()), confirmInvocationContext, transactionContextEditorClass);
	}

	public Terminator getTerminator() {
		return terminator;
	}

	public TransactionXid getXid() {
		return xid;
	}

	public void setXid(TransactionXid xid) {
		this.xid = xid;
	}

	public InvocationContext getConfirmInvocationContext() {
		return confirmInvocationContext;
	}

	public InvocationContext getCancelInvocationContext() {
		return cancelInvocationContext;
	}

}
