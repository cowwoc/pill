package org.pill;

/**
 * Thrown if an entity already exists in a repository.
 * <p/>
 * @author Gili Tzabari
 */
public class EntityExistsException extends Exception
{
	private static final long serialVersionUID = 1L;

	/**
	 * Constructs a new EntityExistsException exception with the specified detail message.
	 * <p/>
	 * @param message the detail message
	 */
	public EntityExistsException(String message)
	{
		super(message);
	}

	/**
	 * Constructs a new EntityExistsException exception with the specified detail message.
	 * <p/>
	 * @param message the detail message
	 * @param cause the cause (which is saved for later retrieval by the Exception.getCause() method).
	 * (A null value is permitted, and indicates that the cause is nonexistent or unknown.)
	 */
	public EntityExistsException(String message, Throwable cause)
	{
		super(message, cause);
	}
}
