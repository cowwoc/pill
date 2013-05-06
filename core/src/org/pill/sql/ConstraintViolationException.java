package org.pill.sql;

import com.mysema.query.QueryException;
import javax.annotation.Nullable;

/**
 * Thrown when an integrity constraint is violated.
 * <p/>
 * @author Gili Tzabari
 */
public class ConstraintViolationException extends QueryException
{
	private static final long serialVersionUID = 0L;

	/**
	 * Creates a new ConstraintViolationException.
	 * <p/>
	 * @param message the detail message. The detail message is saved for later retrieval by the
	 *                {@link #getMessage()} method.
	 * @param cause   the cause (which is saved for later retrieval by the {@link #getCause()}
	 *                method). (A <tt>null</tt> value is permitted, and indicates that the cause is
	 *                nonexistent or unknown.)
	 */
	public ConstraintViolationException(String message, @Nullable Throwable cause)
	{
		super(message, cause);
	}
}
