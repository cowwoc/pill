package org.pill;

import com.google.common.base.Preconditions;
import java.net.URI;
import java.util.Objects;

/**
 * A dependency is a reference to a Release.
 * <p/>
 * @author Gili Tzabari
 */
public final class Dependency
{
	private final URI uri;
	private final Module module;
	private final String version;
	private final DependencyType type;

	/**
	 * Creates a new Dependency.
	 * <p/>
	 * @param uri the dependency identifier
	 * @param module the module name of the dependency
	 * @param version the version number of the dependency
	 * @param type the dependency type
	 * @throws NullPointerException if uri, module, version or type are null
	 */
	public Dependency(URI uri, Module module, String version, DependencyType type)
	{
		Preconditions.checkNotNull(uri, "uri may not be null");
		Preconditions.checkNotNull(module, "module may not be null");
		Preconditions.checkNotNull(version, "version may not be null");
		Preconditions.checkNotNull(type, "type may not be null");

		this.uri = uri;
		this.module = module;
		this.version = version;
		this.type = type;
	}

	/**
	 * @return the resource identifier
	 */
	public URI getUri()
	{
		return uri;
	}

	/**
	 * @return the dependency's module name
	 */
	public Module getModule()
	{
		return module;
	}

	/**
	 * @return the dependency's version number
	 */
	public String getVersion()
	{
		return version;
	}

	/**
	 * @return the dependency type
	 */
	public DependencyType getType()
	{
		return type;
	}

	@Override
	public boolean equals(Object o)
	{
		if (!(o instanceof Dependency))
			return false;
		Dependency other = (Dependency) o;
		return uri.equals(other.getUri()) && this.module.equals(other.getModule())
			&& this.version.equals(other.getVersion()) && this.type.equals(other.getType());
	}

	@Override
	public int hashCode()
	{
		return uri.hashCode() + 31 * Objects.hash(this.module, this.version, this.type);
	}

	@Override
	public String toString()
	{
		return new ToJsonString(Dependency.class, this).toString();
	}
}
