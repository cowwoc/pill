package org.pill.sql;

import java.sql.SQLException;
import javax.annotation.Nullable;
import org.pill.sql.SQLState.Category;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * SQLException helper methods.
 * <p/>
 * @author Gili Tzabari
 */
public class SQLExceptions
{
	/**
	 * Returns the deepest exception cause of type SQLException.
	 * <p/>
	 * @param t a Throwable
	 * @return null if there is no SQLException cause
	 */
	public static SQLException getCause(Throwable t)
	{
		Logger log = LoggerFactory.getLogger(SQLExceptions.class);
		log.debug("Entering. t: {}", t);
		SQLException result = null;
		Throwable current = t;
		do
		{
			current = current.getCause();
			if (current instanceof SQLException)
				result = (SQLException) current;
		}
		while (current != null);
		log.debug("Returning {}", result);
		return result;
	}

	/**
	 * Returns the deepest constraint violation exception.
	 * <p/>
	 * @param t a Throwable
	 * @return null if the Throwable was not caused by a constraint violation
	 */
	@Nullable
	public static ConstraintViolationException getConstraintViolation(Throwable t)
	{
		Logger log = LoggerFactory.getLogger(SQLExceptions.class);
		log.debug("Entering. t: {}", t);
		SQLException cause = getCause(t);
		ConstraintViolationException result;
		try
		{
			if (cause != null && cause.getSQLState() != null && Category.fromCode(cause.getSQLState().
				substring(0, 2)) == Category.INTEGRITY_CONSTRAINT_VIOLATION)
			{
				result = new ConstraintViolationException(cause.getMessage(), cause);
			}
			else
				result = null;
		}
		catch (IllegalArgumentException e)
		{
			throw new IllegalArgumentException("SQLCode: " + cause.getSQLState(), e);
		}
		log.debug("Returning {}", result);
		return result;
	}
}
