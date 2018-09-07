package org.jackson.puppy.tcc.transaction.repository;


import org.jackson.puppy.tcc.transaction.Transaction;
import org.jackson.puppy.tcc.transaction.api.TransactionStatus;
import org.jackson.puppy.tcc.transaction.api.exception.TransactionIOException;
import org.jackson.puppy.tcc.transaction.serializer.JdkSerializationSerializer;
import org.jackson.puppy.tcc.transaction.serializer.ObjectSerializer;
import org.jackson.puppy.tcc.transaction.utils.CollectionUtils;
import org.jackson.puppy.tcc.transaction.utils.StringUtils;

import javax.sql.DataSource;
import javax.transaction.xa.Xid;
import java.sql.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

/**
 * @author Kevin Hwang
 * @since 8/10/2018
 */
public class JdbcAbstractTransactionRepository extends AbstractTransactionCacheRepository {

	private String domain;

	private String tbSuffix;

	private DataSource dataSource;

	private ObjectSerializer serializer = new JdkSerializationSerializer();

	public String getDomain() {
		return domain;
	}

	public void setDomain(String domain) {
		this.domain = domain;
	}

	public String getTbSuffix() {
		return tbSuffix;
	}

	public void setTbSuffix(String tbSuffix) {
		this.tbSuffix = tbSuffix;
	}

	public void setSerializer(ObjectSerializer serializer) {
		this.serializer = serializer;
	}

	public DataSource getDataSource() {
		return dataSource;
	}

	public void setDataSource(DataSource dataSource) {
		this.dataSource = dataSource;
	}

	@Override
	protected int doCreate(Transaction transaction) {

		Connection connection = null;
		PreparedStatement stmt = null;

		try {
			connection = this.getConnection();

			StringBuilder builder = new StringBuilder()
					.append("INSERT INTO ")
					.append(getTableName())
					.append("(GLOBAL_TX_ID,BRANCH_QUALIFIER,TRANSACTION_TYPE,CONTENT,STATUS,RETRIED_COUNT,CREATE_TIME,LAST_UPDATE_TIME,VERSION")
					.append(StringUtils.isNotEmpty(domain) ? ",DOMAIN ) VALUES (?,?,?,?,?,?,?,?,?,?)" : ") VALUES (?,?,?,?,?,?,?,?,?)");

			stmt = connection.prepareStatement(builder.toString());

			stmt.setBytes(1, transaction.getXid().getGlobalTransactionId());
			stmt.setBytes(2, transaction.getXid().getBranchQualifier());
			stmt.setInt(3, transaction.getTransactionType().getId());
			stmt.setBytes(4, serializer.serialize(transaction));
			stmt.setInt(5, transaction.getStatus().getId());
			stmt.setInt(6, transaction.getRetriedCount());
			stmt.setTimestamp(7, new Timestamp(transaction.getCreateTime().getTime()));
			stmt.setTimestamp(8, new Timestamp(transaction.getLastUpdateTime().getTime()));
			stmt.setLong(9, transaction.getVersion());

			if (StringUtils.isNotEmpty(domain)) {
				stmt.setString(10, domain);
			}

			return stmt.executeUpdate();

		} catch (SQLException e) {
			throw new TransactionIOException(e);
		} finally {
			closeStatement(stmt);
			this.releaseConnection(connection);
		}
	}

	@Override
	protected int doUpdate(Transaction transaction) {
		Connection connection = null;
		PreparedStatement stmt = null;

		Date lastUpdateTime = transaction.getLastUpdateTime();
		long currentVersion = transaction.getVersion();

		transaction.updateTime();
		transaction.updateVersion();

		try {
			connection = this.getConnection();

			StringBuilder builder = new StringBuilder()
					.append("UPDATE ")
					.append(getTableName())
					.append(" SET ")
					.append("CONTENT = ?,STATUS = ?,LAST_UPDATE_TIME = ?, RETRIED_COUNT = ?,VERSION = VERSION+1 WHERE GLOBAL_TX_ID = ? AND BRANCH_QUALIFIER = ? AND VERSION = ?")
					.append(StringUtils.isNotEmpty(domain) ? " AND DOMAIN = ?" : "");

			stmt = connection.prepareStatement(builder.toString());

			stmt.setBytes(1, serializer.serialize(transaction));
			stmt.setInt(2, transaction.getStatus().getId());
			stmt.setTimestamp(3, new Timestamp(transaction.getLastUpdateTime().getTime()));

			stmt.setInt(4, transaction.getRetriedCount());
			stmt.setBytes(5, transaction.getXid().getGlobalTransactionId());
			stmt.setBytes(6, transaction.getXid().getBranchQualifier());
			stmt.setLong(7, currentVersion);

			if (StringUtils.isNotEmpty(domain)) {
				stmt.setString(8, domain);
			}

			return stmt.executeUpdate();

		} catch (Throwable e) {
			transaction.setLastUpdateTime(lastUpdateTime);
			transaction.setVersion(currentVersion);
			throw new TransactionIOException(e);
		} finally {
			closeStatement(stmt);
			this.releaseConnection(connection);
		}
	}

	@Override
	protected int doDelete(Transaction transaction) {
		Connection connection = null;
		PreparedStatement stmt = null;

		try {
			connection = this.getConnection();

			StringBuilder builder = new StringBuilder()
					.append("DELETE FROM ")
					.append(getTableName())
					.append(" WHERE GLOBAL_TX_ID = ? AND BRANCH_QUALIFIER = ?")
					.append(StringUtils.isNotEmpty(domain) ? " AND DOMAIN = ?" : "");

			stmt = connection.prepareStatement(builder.toString());

			stmt.setBytes(1, transaction.getXid().getGlobalTransactionId());
			stmt.setBytes(2, transaction.getXid().getBranchQualifier());

			if (StringUtils.isNotEmpty(domain)) {
				stmt.setString(3, domain);
			}

			return stmt.executeUpdate();

		} catch (SQLException e) {
			throw new TransactionIOException(e);
		} finally {
			closeStatement(stmt);
			this.releaseConnection(connection);
		}
	}

	@Override
	protected Transaction doFindOne(Xid xid) {

		List<Transaction> transactions = doFind(Collections.singletonList(xid));

		if (!CollectionUtils.isEmpty(transactions)) {
			return transactions.get(0);
		}
		return null;
	}

	@Override
	protected List<Transaction> doFindAllUnmodifiedSince(Date date) {

		List<Transaction> transactions = new ArrayList<>();

		Connection connection = null;
		PreparedStatement stmt = null;

		try {
			connection = this.getConnection();

			StringBuilder builder = new StringBuilder()
					.append("SELECT GLOBAL_TX_ID, BRANCH_QUALIFIER, CONTENT,STATUS,TRANSACTION_TYPE,CREATE_TIME,LAST_UPDATE_TIME,RETRIED_COUNT,VERSION")
					.append(StringUtils.isNotEmpty(domain) ? ",DOMAIN" : "")
					.append("  FROM ").append(getTableName()).append(" WHERE LAST_UPDATE_TIME < ?")
					.append(StringUtils.isNotEmpty(domain) ? " AND DOMAIN = ?" : "");

			stmt = connection.prepareStatement(builder.toString());

			stmt.setTimestamp(1, new Timestamp(date.getTime()));

			if (StringUtils.isNotEmpty(domain)) {
				stmt.setString(2, domain);
			}

			ResultSet resultSet = stmt.executeQuery();

			this.constructTransactions(resultSet, transactions);
		} catch (Throwable e) {
			throw new TransactionIOException(e);
		} finally {
			closeStatement(stmt);
			this.releaseConnection(connection);
		}

		return transactions;
	}

	protected List<Transaction> doFind(List<Xid> xids) {

		List<Transaction> transactions = new ArrayList<Transaction>();

		if (CollectionUtils.isEmpty(xids)) {
			return transactions;
		}

		Connection connection = null;
		PreparedStatement stmt = null;

		try {
			connection = this.getConnection();

			StringBuilder builder = new StringBuilder()
					.append("SELECT GLOBAL_TX_ID, BRANCH_QUALIFIER, CONTENT,STATUS,TRANSACTION_TYPE,CREATE_TIME,LAST_UPDATE_TIME,RETRIED_COUNT,VERSION")
					.append(StringUtils.isNotEmpty(domain) ? ",DOMAIN" : "")
					.append(" FROM ").append(getTableName()).append(" WHERE");

			if (!CollectionUtils.isEmpty(xids)) {
				for (Xid xid : xids) {
					builder.append(" ( GLOBAL_TX_ID = ? AND BRANCH_QUALIFIER = ? ) OR");
				}

				builder.delete(builder.length() - 2, builder.length());
			}

			builder.append(StringUtils.isNotEmpty(domain) ? " AND DOMAIN = ?" : "");

			stmt = connection.prepareStatement(builder.toString());

			int i = 0;

			for (Xid xid : xids) {
				stmt.setBytes(++i, xid.getGlobalTransactionId());
				stmt.setBytes(++i, xid.getBranchQualifier());
			}

			if (StringUtils.isNotEmpty(domain)) {
				stmt.setString(++i, domain);
			}

			ResultSet resultSet = stmt.executeQuery();

			this.constructTransactions(resultSet, transactions);
		} catch (Throwable e) {
			throw new TransactionIOException(e);
		} finally {
			closeStatement(stmt);
			this.releaseConnection(connection);
		}

		return transactions;
	}

	protected void constructTransactions(ResultSet resultSet, List<Transaction> transactions) throws SQLException {
		while (resultSet.next()) {
			byte[] transactionBytes = resultSet.getBytes(3);
			Transaction transaction = (Transaction) serializer.deserialize(transactionBytes);
			transaction.changeStatus(TransactionStatus.valueOf(resultSet.getInt(4)));
			transaction.setLastUpdateTime(resultSet.getDate(7));
			transaction.setVersion(resultSet.getLong(9));
			transaction.resetRetriedCount(resultSet.getInt(8));
			transactions.add(transaction);
		}
	}


	protected Connection getConnection() {
		try {
			return this.dataSource.getConnection();
		} catch (SQLException e) {
			throw new TransactionIOException(e);
		}
	}

	protected void releaseConnection(Connection con) {
		try {
			if (con != null && !con.isClosed()) {
				con.close();
			}
		} catch (SQLException e) {
			throw new TransactionIOException(e);
		}
	}

	private void closeStatement(Statement stmt) {
		try {
			if (stmt != null && !stmt.isClosed()) {
				stmt.close();
			}
		} catch (Exception ex) {
			throw new TransactionIOException(ex);
		}
	}

	private String getTableName() {
		return StringUtils.isNotEmpty(tbSuffix) ? "TCC_TRANSACTION" + tbSuffix : "TCC_TRANSACTION";
	}
}
