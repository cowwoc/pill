package org.pill;

import com.google.common.collect.Iterators;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * A ClassLoader that limits what classes may be loaded by the parent classloader. This is useful
 * for loading plugins, where one needs to isolate the libraries used by the plugins from the main
 * application.
 * <p/>
 * @author Gili Tzabari
 */
public class LocalClassLoader extends AppendableClassLoader
{
	private final Set<String> allowedPackages = new HashSet<>();
	private final Set<String> allowedResources = new HashSet<>();
	private final Set<URL> excludeLocalResources = new HashSet<>();

	/**
	 * Creates a new LocalClassLoader.
	 * <p/>
	 * @param parent the parent classpath
	 */
	public LocalClassLoader(ClassLoader parent)
	{
		super(parent);
	}

	/**
	 * Creates a new LocalClassLoader that delegates to the system classloader.
	 */
	public LocalClassLoader()
	{
	}

	/**
	 * Returns the package prefixes (e.g. {@code "java.lang."}) that may be loaded from the parent
	 * classloader.
	 * <p/>
	 * @return a mutable Set
	 */
	@SuppressWarnings("ReturnOfCollectionOrArrayField")
	public Set<String> allowedPackages()
	{
		return allowedPackages;
	}

	/**
	 * Returns the path prefixes (e.g. {@code "java/lang/"}) that may be loaded from the parent
	 * classloader.
	 * <p/>
	 * @return a mutable Set
	 */
	@SuppressWarnings("ReturnOfCollectionOrArrayField")
	public Set<String> allowedResources()
	{
		return allowedResources;
	}

	/**
	 * Returns the URL prefixes (e.g. {@code "file:///java/lang/"}) of resources that should be
	 * suppressed if returned by the current classloader. Note this does not affect results returned
	 * by the parent classloader.
	 * <p/>
	 * @return a mutable Set
	 */
	@SuppressWarnings("ReturnOfCollectionOrArrayField")
	public Set<URL> excludeLocalResources()
	{
		return excludeLocalResources;
	}

	@Override
	public Enumeration<URL> getResources(String name) throws IOException
	{
		// Delegate to the parent classloader if allowed
		for (String prefix : allowedResources)
		{
			if (name.startsWith(prefix))
				return filterResources(super.getResources(name));
		}
		return filterResources(findResources(name));
	}

	/**
	 * Filters results returned by {@code getResources(String)};
	 * <p/>
	 * @param resources the return value of {@code getResources(String)}.
	 * @return the resource, or null if no match was found
	 */
	private Enumeration<URL> filterResources(Enumeration<URL> resources)
	{
		if (!resources.hasMoreElements() || excludeLocalResources.isEmpty())
			return resources;
		List<URL> result = new ArrayList<>();
		while (resources.hasMoreElements())
		{
			URL resource = resources.nextElement();
			String resourceAsString = resource.toString();
			boolean suppress = false;
			for (URL prefix : excludeLocalResources)
			{
				if (resourceAsString.startsWith(prefix.toString()))
				{
					suppress = true;
					break;
				}
			}
			if (!suppress)
				result.add(resource);
		}
		return Iterators.asEnumeration(result.iterator());
	}

	@Override
	public URL getResource(String name)
	{
		// Delegate to the parent classloader if allowed
		for (String prefix : allowedResources)
		{
			if (name.startsWith(prefix))
				return filterResource(super.getResource(name));
		}
		return filterResource(findResource(name));
	}

	/**
	 * Filters results returned by {@code getResource(String)};
	 * <p/>
	 * @param resource the return value of {@code getResource(String)}.
	 * @return the resource, or null if no match was found
	 */
	private URL filterResource(URL resource)
	{
		if (resource != null && !excludeLocalResources.isEmpty())
		{
			String resultAsString = resource.toString();
			for (URL prefix : excludeLocalResources)
			{
				if (resultAsString.startsWith(prefix.toString()))
					return null;
			}
		}
		return resource;
	}

	@Override
	protected Class<?> loadClass(String name, boolean resolve) throws ClassNotFoundException
	{
		// Delegate to the parent classloader if allowed
		for (String prefix : allowedPackages)
		{
			if (name.startsWith(prefix))
				return super.loadClass(name, resolve);
		}

		synchronized (getClassLoadingLock(name))
		{
			// First, check if the class has already been loaded
			Class<?> result = findLoadedClass(name);

			if (result == null)
			{
				// Attempt to find the class
				result = findClass(name);
			}
			if (resolve)
				resolveClass(result);
			return result;
		}
	}
}
