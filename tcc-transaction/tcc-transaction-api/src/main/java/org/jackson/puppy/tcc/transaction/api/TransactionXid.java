package org.jackson.puppy.tcc.transaction.api;


import javax.transaction.xa.Xid;
import java.io.Serializable;
import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.UUID;

/**
 * @author Kevin Hwang
 * @since 8/10/2018
 */
public class TransactionXid implements Xid, Serializable {

	private static final long serialVersionUID = -4896558055705907797L;

	private int formatId = 1;

	private byte[] globalTransactionId;

	private byte[] branchQualifier;

	public TransactionXid() {
		globalTransactionId = uuidToByteArray(UUID.randomUUID());
		branchQualifier = uuidToByteArray(UUID.randomUUID());
	}

	public TransactionXid(byte[] globalTransactionId) {
		this.globalTransactionId = globalTransactionId;
		branchQualifier = uuidToByteArray(UUID.randomUUID());
	}

	public TransactionXid(byte[] globalTransactionId, byte[] branchQualifier) {
		this.globalTransactionId = globalTransactionId;
		this.branchQualifier = branchQualifier;
	}

	public static byte[] uuidToByteArray(UUID uuid) {
		ByteBuffer bb = ByteBuffer.wrap(new byte[16]);
		bb.putLong(uuid.getMostSignificantBits());
		bb.putLong(uuid.getLeastSignificantBits());
		return bb.array();
	}

	public static UUID byteArrayToUUID(byte[] bytes) {
		ByteBuffer bb = ByteBuffer.wrap(bytes);
		long firstLong = bb.getLong();
		long secondLong = bb.getLong();
		return new UUID(firstLong, secondLong);
	}

	@Override
	public int getFormatId() {
		return formatId;
	}

	@Override
	public byte[] getGlobalTransactionId() {
		return globalTransactionId;
	}

	public void setGlobalTransactionId(byte[] globalTransactionId) {
		this.globalTransactionId = globalTransactionId;
	}

	@Override
	public byte[] getBranchQualifier() {
		return branchQualifier;
	}

	public void setBranchQualifier(byte[] branchQualifier) {
		this.branchQualifier = branchQualifier;
	}

	@Override
	public String toString() {

		StringBuilder stringBuilder = new StringBuilder();
		stringBuilder.append("globalTransactionId:").append(UUID.nameUUIDFromBytes(globalTransactionId).toString());
		stringBuilder.append(",").append("branchQualifier:").append(UUID.nameUUIDFromBytes(branchQualifier).toString());

		return stringBuilder.toString();
	}

	@Override
	public TransactionXid clone() {

		byte[] cloneGlobalTransactionId = null;
		byte[] cloneBranchQualifier = null;

		if (globalTransactionId != null) {
			cloneGlobalTransactionId = new byte[globalTransactionId.length];
			System.arraycopy(globalTransactionId, 0, cloneGlobalTransactionId, 0, globalTransactionId.length);
		}

		if (branchQualifier != null) {
			cloneBranchQualifier = new byte[branchQualifier.length];
			System.arraycopy(branchQualifier, 0, cloneBranchQualifier, 0, branchQualifier.length);
		}

		return new TransactionXid(cloneGlobalTransactionId, cloneBranchQualifier);
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + this.getFormatId();
		result = prime * result + Arrays.hashCode(branchQualifier);
		result = prime * result + Arrays.hashCode(globalTransactionId);
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		} else if (obj == null) {
			return false;
		} else if (getClass() != obj.getClass()) {
			return false;
		}
		TransactionXid other = (TransactionXid) obj;
		if (this.getFormatId() != other.getFormatId()) {
			return false;
		} else if (!Arrays.equals(branchQualifier, other.branchQualifier)) {
			return false;
		} else if (!Arrays.equals(globalTransactionId, other.globalTransactionId)) {
			return false;
		}
		return true;
	}
}


