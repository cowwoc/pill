package org.pill;

import com.google.common.base.Preconditions;
import java.net.URI;

/**
 * A software module.
 * <p/>
 * @author Gili Tzabari
 */
public final class Module
{
	private final String name;

	/**
	 * Creates a new Module.
	 * <p/>
	 * @param name the module name
	 * @throws NullPointerException if name is null
	 * @throws IllegalArgumentException if name is an empty string
	 */
	public Module(String name)
	{
		Preconditions.checkNotNull(name, "name may not be null");
		Preconditions.checkArgument(!name.isEmpty(), "name may not be empty");

		this.name = name;
	}

	/**
	 * @return the module name
	 */
	public String getName()
	{
		return name;
	}

	@Override
	public boolean equals(Object o)
	{
		if (!(o instanceof Module))
			return false;
		Module other = (Module) o;
		return super.equals(o) && name.equals(other.getName());
	}

	@Override
	public int hashCode()
	{
		return super.hashCode() + 31 * this.name.hashCode();
	}

	@Override
	public String toString()
	{
		return new ToJsonString(Module.class, this).toString();
	}
}
