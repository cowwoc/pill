package org.pill.sql;

import com.google.inject.Inject;
import com.google.inject.servlet.RequestScoped;
import com.mysema.query.QueryException;
import com.mysema.query.sql.RelationalPath;
import com.mysema.query.sql.SQLQuery;
import com.mysema.query.sql.SQLSubQuery;
import com.mysema.query.sql.SQLTemplates;
import com.mysema.query.sql.dml.SQLDeleteClause;
import com.mysema.query.sql.dml.SQLInsertClause;
import com.mysema.query.sql.dml.SQLMergeClause;
import com.mysema.query.sql.dml.SQLUpdateClause;
import com.mysema.query.types.Expression;
import java.sql.Blob;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.SQLException;
import java.sql.SQLFeatureNotSupportedException;
import java.sql.SQLWarning;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Binds a database connection and dialect.
 * <p/>
 * @author Gili Tzabari
 */
@RequestScoped
public class Session implements AutoCloseable
{
	private final Connection connection;
	private final SQLTemplates dialect;
	private final Logger log = LoggerFactory.getLogger(Session.class);

	/**
	 * Creates a new Session.
	 * <p/>
	 * @param connection the database connection
	 * @param dialect the database dialect
	 */
	@Inject
	public Session(Connection connection, SQLTemplates dialect)
	{
		log.debug("Entering. connection: {}, dialect: {}, lockingOrder: {}", connection, dialect);
		this.connection = connection;
		this.dialect = dialect;
		log.debug("Returning");
	}

	/**
	 * Creates a new query.
	 * <p/>
	 * @param tables the tables to query
	 * @return a new query
	 */
	public SQLQuery query(Expression<?>... tables)
	{
		log.debug("Entering. tables: {}", (Object[]) tables);
		SQLQuery result = new SQLQuery(connection, dialect).from(tables);
		log.debug("Returning {}", result);
		return result;
	}

	/**
	 * Creates a new sub-query.
	 * <p/>
	 * @param tables the tables to query
	 * @return a new sub-query
	 */
	public SQLSubQuery subQuery(Expression<?>... tables)
	{
		log.debug("Entering. tables: {}", (Object[]) tables);
		SQLSubQuery result = new SQLSubQuery().from(tables);
		log.debug("Returning {}", result);
		return result;
	}

	/**
	 * Creates a new insert command.
	 * <p/>
	 * @param table the table to insert into
	 * @return the insert command
	 */
	public SQLInsertClause insert(RelationalPath<?> table)
	{
		log.debug("Entering. table: {}", table);
		SQLInsertClause result = new SQLInsertClause(connection, dialect, table);
		log.debug("Returning {}", result);
		return result;
	}

	/**
	 * Creates a new update command.
	 * <p/>
	 * @param table the table to update
	 * @return the update command
	 */
	public SQLUpdateClause update(RelationalPath<?> table)
	{
		log.debug("Entering. table: {}", table);
		SQLUpdateClause result = new SQLUpdateClause(connection, dialect, table);
		log.debug("Returning {}", result);
		return result;
	}

	/**
	 * Creates a new merge operation (update existing rows, insert rows that don't exist).
	 * <p/>
	 * @param table the table to merge
	 * @return the merge command
	 */
	public SQLMergeClause merge(RelationalPath<?> table)
	{
		log.debug("Entering. table: {}", table);
		SQLMergeClause result = new SQLMergeClause(connection, dialect, table);
		log.debug("Returning {}", result);
		return result;
	}

	/**
	 * Creates a new delete command.
	 * <p/>
	 * @param table the table to delete query
	 * @return the delete command
	 */
	public SQLDeleteClause delete(RelationalPath<?> table)
	{
		log.debug("Entering. table: {}", table);
		SQLDeleteClause result = new SQLDeleteClause(connection, dialect, table);
		log.debug("Returning {}", result);
		return result;
	}

	/**
	 * Sets this session's auto-commit mode to the given state. If a session is in auto-commit mode,
	 * then all its SQL statements will be executed and committed as individual transactions.
	 * Otherwise, its SQL statements are grouped into transactions that are terminated by a call to
	 * either the method
	 * <code>commit</code> or the method
	 * <code>rollback</code>. By default, new sessions are in auto-commit mode. <P> The commit occurs
	 * when the statement completes. The time when the statement completes depends on the type of SQL
	 * Statement: <ul> <li>For DML statements, such as Insert, Update or Delete, and DDL statements,
	 * the statement is complete as soon as it has finished executing. <li>For Select statements, the
	 * statement is complete when the associated result set is closed. <li>For
	 * <code>CallableStatement</code> objects or for statements that return multiple results, the
	 * statement is complete when all of the associated result sets have been closed, and all update
	 * counts and output parameters have been retrieved. </ul> <P> <B>NOTE:</B> If this method is
	 * called during a transaction and the auto-commit mode is changed, the transaction is committed.
	 * If
	 * <code>setAutoCommit</code> is called and the auto-commit mode is not changed, the call is a
	 * no-op.
	 * <p/>
	 * @param autoCommit <code>true</code> to enable auto-commit mode; <code>false</code> to disable
	 * it
	 * @throws QueryException if a database access error occurs, setAutoCommit(true) is called while
	 * participating in a distributed transaction, or this method is called on a closed session
	 * @see #isAutoCommit
	 */
	public void setAutoCommit(boolean autoCommit)
	{
		log.debug("Entering. autoCommit: {}", autoCommit);
		try
		{
			connection.setAutoCommit(autoCommit);
		}
		catch (SQLException e)
		{
			throw new QueryException(e);
		}
		log.debug("Returning");
	}

	/**
	 * Indicates if the current auto-commit mode for this
	 * <code>Session</code> object.
	 * <p/>
	 * @return the current state of this <code>Session</code> object's auto-commit mode
	 * @throws QueryException if a database access error occurs or this method is called on a closed
	 * session
	 * @see #setAutoCommit
	 */
	public boolean isAutoCommit()
	{
		log.debug("Entering");
		try
		{
			boolean result = connection.getAutoCommit();
			log.debug("Returning {}", result);
			return result;
		}
		catch (SQLException e)
		{
			throw new QueryException(e);
		}
	}

	/**
	 * Makes all changes made since the previous commit/rollback permanent and releases any database
	 * locks currently held by this
	 * <code>Session</code> object. This method should be used only when auto-commit mode has been
	 * disabled.
	 * <p/>
	 * @throws QueryException if a database access error occurs, this method is called while
	 * participating in a distributed transaction, if this method is called on a closed session or *
	 * the <code>Session</code> object is in auto-commit mode.
	 * @see #setAutoCommit
	 */
	public void commit()
	{
		log.debug("Entering");
		try
		{
			connection.commit();
		}
		catch (SQLException e)
		{
			throw new QueryException(e);
		}
		log.debug("Returning");
	}

	/**
	 * Undoes all changes made in the current transaction and releases any database locks currently
	 * held by the
	 * <code>Session</code> object. This method should be used only when auto-commit mode has been
	 * disabled.
	 * <p/>
	 * @throws QueryException if a database access error occurs, this method is called while
	 * participating in a distributed transaction, this method is called on a closed session or * *
	 * the <code>Session</code> object is in auto-commit mode
	 * @see #setAutoCommit
	 */
	public void rollback()
	{
		log.debug("Entering");
		try
		{
			connection.rollback();
		}
		catch (SQLException e)
		{
			throw new QueryException(e);
		}
		log.debug("Returning");
	}

	/**
	 * Releases the
	 * <code>Session</code> object's database and JDBC resources immediately instead of waiting for
	 * them to be automatically released. <P> Calling the method
	 * <code>close</code> on a
	 * <code>Session</code> object that is already closed is a no-op. <P> It is <b>strongly
	 * recommended</b> that an application explicitly commits or rolls back an active transaction
	 * prior to calling the
	 * <code>close</code> method. If the
	 * <code>close</code> method is called and there is an active transaction, the results are
	 * implementation-defined. <P>
	 * <p/>
	 * @throws QueryException if a database access error occurs
	 */
	@Override
	public void close()
	{
		log.debug("Entering");
		try
		{
			connection.close();
		}
		catch (SQLException e)
		{
			throw new QueryException(e);
		}
		log.debug("Returning");
	}

	/**
	 * Indicates whether this
	 * <code>Session</code> object has been closed. A session is closed if the method
	 * <code>close</code> has been called on it or if certain fatal errors have occurred. This method
	 * is guaranteed to return
	 * <code>true</code> only when it is called after the method
	 * <code>Session.close</code> has been called. <P> This method generally cannot be called to
	 * determine whether a connection to a database is valid or invalid. A typical client can
	 * determine that a connection is invalid by catching any exceptions that might be thrown when an
	 * operation is attempted.
	 * <p/>
	 * @return <code>true</code> if this <code>Session</code> object is closed; <code>false</code> if
	 * it is still open
	 * @throws QueryException if a database access error occurs
	 */
	public boolean isClosed()
	{
		log.debug("Entering");
		try
		{
			boolean result = connection.isClosed();
			log.debug("Returning {}", result);
			return result;
		}
		catch (SQLException e)
		{
			throw new QueryException(e);
		}
	}

	/**
	 * Puts this session in read-only mode as a hint to the driver to enable database optimizations.
	 * <p/>
	 * <P><B>Note:</B> This method cannot be called during a transaction.
	 * <p/>
	 * @param readOnly <code>true</code> enables read-only mode; <code>false</code> disables it
	 * @throws QueryException if a database access error occurs, this method is called on a closed
	 * session or this method is called during a transaction
	 */
	public void setReadOnly(boolean readOnly)
	{
		log.debug("Entering. readOnly: {}", readOnly);
		try
		{
			connection.setReadOnly(readOnly);
		}
		catch (SQLException e)
		{
			throw new QueryException(e);
		}
		log.debug("Returning");
	}

	/**
	 * Retrieves whether this
	 * <code>Session</code> object is in read-only mode.
	 * <p/>
	 * @return <code>true</code> if this <code>Session</code> object is read-only; <code>false</code>
	 * otherwise
	 * @throws QueryException SQLException if a database access error occurs or this method is called
	 * on a closed session
	 */
	public boolean isReadOnly()
	{
		log.debug("Entering");
		try
		{
			boolean result = connection.isReadOnly();
			log.debug("Returning {}", result);
			return result;
		}
		catch (SQLException e)
		{
			throw new QueryException(e);
		}
	}

	/**
	 * Sets the given catalog name in order to query a subspace of this
	 * <code>Session</code> object's database in which to work. <P> If the driver does not support
	 * catalogs, it will silently ignore this request.
	 * <p/>
	 * @param catalog the name of a catalog (subspace in this <code>Session</code> object's database)
	 * in which to work
	 * @throws QueryException if a database access error occurs or this method is called on a closed
	 * session
	 * @see #getCatalog
	 */
	public void setCatalog(String catalog)
	{
		log.debug("Entering. catalog: {}", catalog);
		try
		{
			connection.setCatalog(catalog);
		}
		catch (SQLException e)
		{
			throw new QueryException(e);
		}
		log.debug("Returning");
	}

	/**
	 * Retrieves this
	 * <code>Session</code> object's current catalog name.
	 * <p/>
	 * @return the current catalog name or <code>null</code> if there is none
	 * @throws QueryException if a database access error occurs or this method is called on a closed
	 * session
	 * @see #setCatalog
	 */
	public String getCatalog()
	{
		log.debug("Entering");
		try
		{
			String result = connection.getCatalog();
			log.debug("Returning {}", result);
			return result;
		}
		catch (SQLException e)
		{
			throw new QueryException(e);
		}
	}

	/**
	 * Attempts to change the transaction isolation level for this
	 * <code>Session</code> object to the one given. <P> <B>Note:</B> If this method is called during
	 * a transaction, the result is implementation-defined.
	 * <p/>
	 * @param level the transaction isolation level. (Note that <code>TransactionIsolation.NONE</code>
	 * cannot be used because it specifies that transactions are not supported.)
	 * @throws QueryException if a database access error occurs, this method is called on a closed
	 * session
	 * @see DatabaseMetaData#supportsTransactionIsolationLevel
	 * @see #getTransactionIsolation
	 */
	public void setTransactionIsolation(TransactionIsolation level)
	{
		log.debug("Entering. level: {}", level);
		try
		{
			connection.setTransactionIsolation(level.getId());
		}
		catch (SQLException e)
		{
			throw new QueryException(e);
		}
		log.debug("Returning");
	}

	/**
	 * Retrieves this
	 * <code>Session</code> object's current transaction isolation level.
	 * <p/>
	 * @return the current transaction isolation level
	 * @throws QueryException if a database access error occurs or this method is called on a closed
	 * session
	 * @see #setTransactionIsolation
	 */
	public TransactionIsolation getTransactionIsolation()
	{
		log.debug("Entering");
		try
		{
			TransactionIsolation result = TransactionIsolation.fromConnection(connection.
				getTransactionIsolation());
			log.debug("Returning {}", result);
			return result;
		}
		catch (SQLException e)
		{
			throw new QueryException(e);
		}
	}

	/**
	 * Retrieves the first warning reported by calls on this
	 * <code>Session</code> object. If there is more than one warning, subsequent warnings will be
	 * chained to the first one and can be retrieved by calling the method
	 * <code>SQLWarning.getNextWarning</code> on the warning that was retrieved previously. <P> This
	 * method may not be called on a closed session; doing so will cause a
	 * <code>QueryException</code> to be thrown.
	 * <p/>
	 * <P><B>Note:</B> Subsequent warnings will be chained to this SQLWarning.
	 * <p/>
	 * @return the first <code>SQLWarning</code> object or <code>null</code> if there are none
	 * @throws QueryException if a database access error occurs or this method is called on a closed
	 * session
	 * @see SQLWarning
	 */
	public SQLWarning getWarnings()
	{
		log.debug("Entering");
		try
		{
			SQLWarning result = connection.getWarnings();
			log.debug("Returning {}", result);
			return result;
		}
		catch (SQLException e)
		{
			throw new QueryException(e);
		}
	}

	/**
	 * Clears all warnings reported for this
	 * <code>Session</code> object. After a call to this method, the method
	 * <code>getWarnings</code> returns
	 * <code>null</code> until a new warning is reported for this
	 * <code>Session</code> object.
	 * <p/>
	 * @throws QueryException SQLException if a database access error occurs or this method is called
	 * on a closed session
	 */
	public void clearWarnings()
	{
		log.debug("Entering");
		try
		{
			connection.clearWarnings();
		}
		catch (SQLException e)
		{
			throw new QueryException(e);
		}
		log.debug("Returning");
	}

	/**
	 * Returns true if the session has not been closed and is still valid. The driver shall submit a
	 * query on the session or use some other mechanism that positively verifies the session is still
	 * valid when this method is called. <p> The query submitted by the driver to validate the session
	 * shall be executed in the context of the current transaction.
	 * <p/>
	 * @param timeout The time in seconds to wait for the database operation used to validate the
	 * session to complete. If the timeout period expires before the operation completes, this method
	 * returns false. A value of 0 indicates a timeout is not applied to the database operation.
	 * @return true if the session is valid, false otherwise
	 * @throws QueryException if the value supplied for <code>timeout</code> is less then 0
	 * @since 1.6
	 * @see java.sql.DatabaseMetaData#getClientInfoProperties
	 */
	public boolean isValid(int timeout)
	{
		log.debug("Entering. timeout: {}", timeout);
		try
		{
			boolean result = connection.isValid(timeout);
			log.debug("Returning {}", result);
			return result;
		}
		catch (SQLException e)
		{
			throw new QueryException(e);
		}
	}

	/**
	 * Constructs an object that implements the
	 * <code>Blob</code> interface. The object returned initially contains no data. The
	 * <code>setBinaryStream</code> and
	 * <code>setBytes</code> methods of the
	 * <code>Blob</code> interface may be used to add data to the
	 * <code>Blob</code>.
	 * <p/>
	 * @return An object that implements the <code>Blob</code> interface
	 * @throws QueryException wraps the following exceptions that occur as a result of a database
	 * error. {@link SQLException} if an object that implements * * * * * * * the <code>Blob</code>
	 * interface can not be constructed, this method is * called on a closed connection or a database
	 * access error occurs. * {@link SQLFeatureNotSupportedException} if the JDBC driver does not *
	 * support this data type
	 * <p/>
	 * @since 1.6
	 */
	public Blob createBlob()
	{
		log.debug("Entering");
		try
		{
			Blob result = connection.createBlob();
			log.debug("Returning {}", result);
			return result;
		}
		catch (SQLException e)
		{
			throw new QueryException(e);
		}
	}

	/**
	 * @return the URL for this DBMS or null if it cannot be generated
	 * @throws QueryException if a database error occurs
	 */
	public String getDatabaseUrl()
	{
		log.debug("Entering");
		try
		{
			String result = connection.getMetaData().getURL();
			log.debug("Returning {}", result);
			return result;
		}
		catch (SQLException e)
		{
			throw new QueryException(e);
		}
	}
}
