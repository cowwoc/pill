package org.pill;

/**
 * Indicates that the compiler has failed in an expected manner.
 * <p/>
 * @author Gili Tzabari
 */
public final class CompilationException extends Exception
{
	private static final long serialVersionUID = 0L;

	/**
	 * Creates a new CompilationException.
	 * <p/>
	 * @param cause the underlying cause
	 */
	public CompilationException(final Throwable cause)
	{
		super(cause);
	}

	/**
	 * Creates a new CompilationException.
	 * <p/>
	 * @param cause the underlying cause
	 * @param message the detail message
	 */
	public CompilationException(final String message, final Throwable cause)
	{
		super(message, cause);
	}

	/**
	 * Creates a new CompilationException.
	 * <p/>
	 * @param message the detail message
	 */
	public CompilationException(final String message)
	{
		super(message);
	}

	/**
	 * Creates a new CompilationException.
	 */
	public CompilationException()
	{
		super();
	}
}
