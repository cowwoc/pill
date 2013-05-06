package org.pill.sql;

import java.sql.Connection;

/**
 * Transaction isolation level.
 * 
 * @author Gili Tzabari
 * @see http://download.oracle.com/javase/tutorial/jdbc/basics/transactions.html
 */
public enum TransactionIsolation
{
	/**
	 * A constant indicating that transactions are not supported.
	 */
	NONE(Connection.TRANSACTION_NONE),
	/**
	 * Dirty reads, non-repeatable reads and phantom reads can occur. This level allows a row
	 * changed by one transaction to be read by another transaction before any changes in that row
	 * have been committed (a "dirty read"). If any of the changes are rolled back, the second
	 * transaction will have retrieved an invalid row.
	 */
	READ_UNCOMMITTED(Connection.TRANSACTION_READ_UNCOMMITTED),
	/**
	 * A constant indicating that dirty reads are prevented; non-repeatable reads and phantom reads
	 * can occur. This level only prohibits a transaction from reading a row with uncommitted changes
	 * in it. 
	 */
	READ_COMMITTED(Connection.TRANSACTION_READ_COMMITTED),
	/**
	 * A constant indicating that dirty reads and non-repeatable reads are prevented; phantom reads
	 * can occur. This level prohibits a transaction from reading a row with uncommitted changes in
	 * it, and it also prohibits the situation where one transaction reads a row, a second
	 * transaction alters the row, and the first transaction rereads the row, getting different
	 * values the second time (a "non-repeatable read"). 
	 */
	REPEATABLE_READ(Connection.TRANSACTION_REPEATABLE_READ),
	/**
	 * A constant indicating that dirty reads, non-repeatable reads and phantom reads are prevented.
	 * This level includes the prohibitions in TRANSACTION_REPEATABLE_READ and further prohibits the
	 * situation where one transaction reads all rows that satisfy a WHERE condition, a second
	 * transaction inserts a row that satisfies that WHERE condition, and the first transaction
	 * rereads for the same condition, retrieving the additional "phantom" row in the second read.
	 */
	SERIALIZABLE(Connection.TRANSACTION_SERIALIZABLE);
	private final int id;

	/**
	 * Creates a new transaction isolation level.
	 * 
	 * @param id the transaction isolation id
	 */
	private TransactionIsolation(int id)
	{
		this.id = id;
	}

	/**
	 * Returns the transaction isolation id.
	 * 
	 * @return the transaction isolation id
	 */
	public int getId()
	{
		return id;
	}

	/**
	 * Creates a TransactionIsolation from a java.sql.Connection.
	 *
	 * @param level the value returned by {@link java.sql.Connection#getTransactionIsolation()}
	 * @return the enum constant corresponding to the transaction isolation level
	 * @throws IllegalArgumentException if the specified value has
	 *         no corresponding enum constant
	 */
	public static TransactionIsolation fromConnection(int level)
	{
		switch (level)
		{
			case Connection.TRANSACTION_NONE:
				return TransactionIsolation.NONE;
			case Connection.TRANSACTION_READ_COMMITTED:
				return TransactionIsolation.READ_COMMITTED;
			case Connection.TRANSACTION_READ_UNCOMMITTED:
				return TransactionIsolation.READ_UNCOMMITTED;
			case Connection.TRANSACTION_REPEATABLE_READ:
				return TransactionIsolation.REPEATABLE_READ;
			case Connection.TRANSACTION_SERIALIZABLE:
				return TransactionIsolation.SERIALIZABLE;
			default:
				throw new IllegalArgumentException(String.valueOf(level));
		}
	}
}
