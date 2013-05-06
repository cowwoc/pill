package org.pill;

/**
 * Thrown if an entity was not found in a repository.
 * <p/>
 * @author Gili Tzabari
 */
public class EntityNotFoundException extends Exception
{
	private static final long serialVersionUID = 1L;

	/**
	 * Constructs a new EntityNotFoundException exception with the specified detail message.
	 * <p/>
	 * @param message the detail message
	 */
	public EntityNotFoundException(String message)
	{
		super(message);
	}
}
